package com.backend.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.RechargeRecordsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Consultations;
import com.backend.model.entity.Patients;
import com.backend.model.entity.PaymentRecords;
import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.Users;
import com.backend.service.impl.PatientAccountServiceImpl;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.backend.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 患者账户服务单元测试。
 * <p>
 * 测试功能：账户充值、就诊缴费、余额查询、患者姓名搜索。
 * 覆盖余额不足、缴费后预约状态变为 completed 等支付流程。
 * 对应生产类：{@link PatientAccountServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class PatientAccountServiceImplTest {

    @Mock
    private PatientsMapper patientsMapper;
    @Mock
    private RechargeRecordsMapper rechargeRecordsMapper;
    @Mock
    private PaymentRecordsMapper paymentRecordsMapper;
    @Mock
    private ConsultationsMapper consultationsMapper;
    @Mock
    private AppointmentsMapper appointmentsMapper;
    @Mock
    private DoctorsMapper doctorsMapper;
    @Mock
    private DepartmentsMapper departmentsMapper;
    @Mock
    private UsersMapper usersMapper;

    private PatientAccountServiceImpl patientAccountService;

    @BeforeEach
    void setUp() {
        patientAccountService = new PatientAccountServiceImpl();
        ReflectionTestUtils.setField(patientAccountService, "patientsMapper", patientsMapper);
        ReflectionTestUtils.setField(patientAccountService, "rechargeRecordsMapper", rechargeRecordsMapper);
        ReflectionTestUtils.setField(patientAccountService, "paymentRecordsMapper", paymentRecordsMapper);
        ReflectionTestUtils.setField(patientAccountService, "consultationsMapper", consultationsMapper);
        ReflectionTestUtils.setField(patientAccountService, "appointmentsMapper", appointmentsMapper);
        ReflectionTestUtils.setField(patientAccountService, "doctorsMapper", doctorsMapper);
        ReflectionTestUtils.setField(patientAccountService, "departmentsMapper", departmentsMapper);
        ReflectionTestUtils.setField(patientAccountService, "usersMapper", usersMapper);
    }

    /** 验证充值成功：余额增加，写入充值记录 */
    @Test
    void recharge_success() {
        Patients patient = patientProfile();
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);
        when(rechargeRecordsMapper.insert(any(RechargeRecords.class))).thenReturn(1);
        when(patientsMapper.update(any(Patients.class))).thenReturn(1);

        RechargeRecords record = patientAccountService.recharge(1001, new BigDecimal("50.00"), "alipay");

        assertEquals(new BigDecimal("50.00"), record.getAmount());
        ArgumentCaptor<Patients> captor = ArgumentCaptor.forClass(Patients.class);
        verify(patientsMapper).update(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getBalance());
    }

    /** 验证充值金额 ≤ 0 时抛出 PARAMS_ERROR */
    @Test
    void recharge_invalidAmount_throwsException() {
        Patients patient = patientProfile();
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> patientAccountService.recharge(1001, BigDecimal.ZERO, "alipay"));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    /** 验证缴费成功：扣减余额、更新缴费记录为 paid、关联预约变为 completed */
    @Test
    void payment_success_updatesBalanceAndAppointment() {
        Patients patient = patientProfile();
        PaymentRecords unpaid = PaymentRecords.builder()
                .paymentId(6001)
                .patientId(2001)
                .consultationId(7001)
                .amount(new BigDecimal("50.00"))
                .status("unpaid")
                .build();
        Consultations consultation = Consultations.builder()
                .consultationId(7001)
                .appointmentId(5001)
                .build();
        Appointments appointment = Appointments.builder()
                .appointmentId(5001)
                .status("unpaid")
                .build();

        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);
        when(paymentRecordsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(unpaid);
        when(consultationsMapper.selectOneById(7001)).thenReturn(consultation);
        when(appointmentsMapper.selectOneById(5001)).thenReturn(appointment);

        PaymentRecords result = patientAccountService.payment(1001, 7001, new BigDecimal("50.00"), "account");

        assertEquals("paid", result.getStatus());
        ArgumentCaptor<Patients> patientCaptor = ArgumentCaptor.forClass(Patients.class);
        verify(patientsMapper).update(patientCaptor.capture());
        assertEquals(new BigDecimal("50.00"), patientCaptor.getValue().getBalance());

        ArgumentCaptor<Appointments> aptCaptor = ArgumentCaptor.forClass(Appointments.class);
        verify(appointmentsMapper).update(aptCaptor.capture());
        assertEquals("completed", aptCaptor.getValue().getStatus());
    }

    /** 验证余额不足时无法缴费，抛出 OPERATION_ERROR */
    @Test
    void payment_insufficientBalance_throwsException() {
        Patients patient = patientProfile();
        patient.setBalance(new BigDecimal("10.00"));
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> patientAccountService.payment(1001, 7001, new BigDecimal("50.00"), "account"));

        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), ex.getCode());
    }

    /** 验证查询患者账户余额 */
    @Test
    void getBalance_returnsPatientBalance() {
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patientProfile());

        assertEquals(new BigDecimal("100.00"), patientAccountService.getBalance(1001));
    }

    /** 验证患者不存在时余额返回 0 */
    @Test
    void getBalance_patientNotFound_returnsZero() {
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        assertEquals(BigDecimal.ZERO, patientAccountService.getBalance(9999));
    }

    /** 验证按姓名模糊搜索患者（用于代挂号等场景） */
    @Test
    void searchPatients_byName() {
        Users user = activePatientUser();
        when(usersMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(user));
        when(patientsMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(patientProfile()));

        List<Map<String, Object>> results = patientAccountService.searchPatients("测试");

        assertEquals(1, results.size());
        assertEquals("测试患者", results.get(0).get("patientName"));
    }

    /** 验证搜索关键词为空时返回空列表 */
    @Test
    void searchPatients_emptyName_returnsEmptyList() {
        assertTrue(patientAccountService.searchPatients("").isEmpty());
        assertTrue(patientAccountService.searchPatients(null).isEmpty());
    }
}
