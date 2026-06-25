package com.backend.service;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.SchedulesMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Consultations;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Schedules;
import com.backend.service.impl.AppointmentsServiceImpl;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static com.backend.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 预约挂号服务单元测试。
 * <p>
 * 测试功能：预约创建、取消、签到、接诊及预约状态流转（pending → confirmed → processing）。
 * 覆盖号源扣减/恢复、排班星期匹配、重复预约防重等业务规则。
 * 对应生产类：{@link AppointmentsServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class AppointmentsServiceImplTest {

    @Mock
    private AppointmentsMapper appointmentsMapper;
    @Mock
    private UsersMapper usersMapper;
    @Mock
    private PatientsMapper patientsMapper;
    @Mock
    private DoctorsMapper doctorsMapper;
    @Mock
    private SchedulesMapper schedulesMapper;
    @Mock
    private DepartmentsMapper departmentsMapper;
    @Mock
    private ConsultationsMapper consultationsMapper;

    private AppointmentsServiceImpl appointmentsService;

    @BeforeEach
    void setUp() {
        appointmentsService = new AppointmentsServiceImpl();
        ReflectionTestUtils.setField(appointmentsService, "mapper", appointmentsMapper);
        ReflectionTestUtils.setField(appointmentsService, "usersMapper", usersMapper);
        ReflectionTestUtils.setField(appointmentsService, "patientsMapper", patientsMapper);
        ReflectionTestUtils.setField(appointmentsService, "doctorsMapper", doctorsMapper);
        ReflectionTestUtils.setField(appointmentsService, "schedulesMapper", schedulesMapper);
        ReflectionTestUtils.setField(appointmentsService, "departmentsMapper", departmentsMapper);
        ReflectionTestUtils.setField(appointmentsService, "consultationsMapper", consultationsMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /** 验证患者创建预约成功：状态为 pending，扣减号源，自动关联医生 ID */
    @Test
    void createAppointment_success() {
        LocalDate appointmentDate = nextWeekday(DayOfWeek.MONDAY);
        Schedules schedule = activeSchedule(DayOfWeek.MONDAY.getValue());
        Patients patient = patientProfile();

        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
        when(schedulesMapper.selectOneById(3001)).thenReturn(schedule);
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);
        when(appointmentsMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
        when(appointmentsMapper.insert(any(Appointments.class), anyBoolean())).thenReturn(1);

        Appointments request = pendingAppointment(appointmentDate);
        Appointments result = appointmentsService.createAppointment(request);

        assertEquals("pending", result.getStatus());
        assertEquals(4001, result.getDoctorId());
        verify(schedulesMapper).updateByQuery(any(Schedules.class), any(QueryWrapper.class));
    }

    /** 验证号源为 0 时无法预约，抛出 OPERATION_ERROR */
    @Test
    void createAppointment_noAvailableSlots_throwsException() {
        LocalDate appointmentDate = nextWeekday(DayOfWeek.MONDAY);
        Schedules schedule = activeSchedule(DayOfWeek.MONDAY.getValue());
        schedule.setAvailableSlots(0);

        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
        when(schedulesMapper.selectOneById(3001)).thenReturn(schedule);

        Appointments request = pendingAppointment(appointmentDate);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentsService.createAppointment(request));

        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), ex.getCode());
    }

    /** 验证预约日期与排班星期不匹配时拒绝，抛出 PARAMS_ERROR */
    @Test
    void createAppointment_dayOfWeekMismatch_throwsException() {
        LocalDate tuesday = nextWeekday(DayOfWeek.TUESDAY);
        Schedules schedule = activeSchedule(DayOfWeek.MONDAY.getValue());

        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
        when(schedulesMapper.selectOneById(3001)).thenReturn(schedule);

        Appointments request = pendingAppointment(tuesday);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentsService.createAppointment(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    /** 验证同一患者同一排班同一日期重复预约时拒绝，抛出 OPERATION_ERROR */
    @Test
    void createAppointment_duplicateBooking_throwsException() {
        LocalDate appointmentDate = nextWeekday(DayOfWeek.MONDAY);
        Schedules schedule = activeSchedule(DayOfWeek.MONDAY.getValue());
        Patients patient = patientProfile();

        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
        when(schedulesMapper.selectOneById(3001)).thenReturn(schedule);
        when(patientsMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(patient);
        when(appointmentsMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

        Appointments request = pendingAppointment(appointmentDate);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentsService.createAppointment(request));

        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), ex.getCode());
    }

    /** 验证取消预约：状态变为 cancelled，并恢复排班号源 */
    @Test
    void cancelAppointment_success_restoresSlot() {
        Appointments apt = pendingAppointment(LocalDate.now().plusDays(1));
        apt.setAppointmentId(5001);
        Schedules schedule = activeSchedule(DayOfWeek.MONDAY.getValue());
        schedule.setAvailableSlots(4);

        when(appointmentsMapper.selectOneById(5001)).thenReturn(apt);
        when(schedulesMapper.selectOneById(3001)).thenReturn(schedule);
        when(appointmentsMapper.update(any(Appointments.class), anyBoolean())).thenReturn(1);

        appointmentsService.cancelAppointment(5001);

        ArgumentCaptor<Appointments> captor = ArgumentCaptor.forClass(Appointments.class);
        verify(appointmentsMapper).update(captor.capture(), eq(true));
        assertEquals("cancelled", captor.getValue().getStatus());
        verify(schedulesMapper).updateByQuery(any(Schedules.class), any(QueryWrapper.class));
    }

    /** 验证已取消的预约不能再次取消，抛出 OPERATION_ERROR */
    @Test
    void cancelAppointment_alreadyCancelled_throwsException() {
        Appointments apt = pendingAppointment(LocalDate.now().plusDays(1));
        apt.setAppointmentId(5001);
        apt.setStatus("cancelled");

        when(appointmentsMapper.selectOneById(5001)).thenReturn(apt);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentsService.cancelAppointment(5001));

        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), ex.getCode());
    }

    /** 验证患者签到：pending → confirmed */
    @Test
    void confirmAppointment_pendingToConfirmed() {
        Appointments apt = pendingAppointment(LocalDate.now().plusDays(1));
        apt.setAppointmentId(5001);

        when(appointmentsMapper.selectOneById(5001)).thenReturn(apt);
        when(appointmentsMapper.update(any(Appointments.class), anyBoolean())).thenReturn(1);

        appointmentsService.confirmAppointment(5001);

        ArgumentCaptor<Appointments> captor = ArgumentCaptor.forClass(Appointments.class);
        verify(appointmentsMapper).update(captor.capture(), eq(true));
        assertEquals("confirmed", captor.getValue().getStatus());
    }

    /** 验证医生接诊：confirmed → processing，并自动创建就诊记录 */
    @Test
    void processAppointment_createsConsultation() {
        Appointments apt = pendingAppointment(LocalDate.now().plusDays(1));
        apt.setAppointmentId(5001);
        apt.setStatus("confirmed");

        when(appointmentsMapper.selectOneById(5001)).thenReturn(apt);
        when(appointmentsMapper.update(any(Appointments.class), anyBoolean())).thenReturn(1);

        appointmentsService.processAppointment(5001);

        verify(consultationsMapper).insert(any(Consultations.class));
        ArgumentCaptor<Appointments> captor = ArgumentCaptor.forClass(Appointments.class);
        verify(appointmentsMapper).update(captor.capture(), eq(true));
        assertEquals("processing", captor.getValue().getStatus());
    }

    /** 验证非 pending/confirmed 状态不能接诊，抛出 OPERATION_ERROR */
    @Test
    void processAppointment_invalidStatus_throwsException() {
        Appointments apt = pendingAppointment(LocalDate.now().plusDays(1));
        apt.setAppointmentId(5001);
        apt.setStatus("processing");

        when(appointmentsMapper.selectOneById(5001)).thenReturn(apt);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentsService.processAppointment(5001));

        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), ex.getCode());
    }

    /** 辅助方法：找到下一个指定星期几的日期 */
    private LocalDate nextWeekday(DayOfWeek dayOfWeek) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}
