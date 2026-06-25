package com.backend;

import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Schedules;
import com.backend.model.entity.Users;
import com.backend.utils.PasswordUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * 测试数据工厂。
 * <p>
 * 集中构造各测试类共用的 DTO 与实体，避免重复造数据，保证测试输入一致。
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    /** 构造合法的患者注册请求（用户名 test_patient，密码 123456） */
    public static UserRegisterRequest validPatientRegisterRequest() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("test_patient");
        request.setRealName("测试患者");
        request.setPhone("13800138000");
        request.setEmail("patient@test.com");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setRole("patient");
        return request;
    }

    /** 构造合法的医生注册请求 */
    public static UserRegisterRequest validDoctorRegisterRequest() {
        UserRegisterRequest request = validPatientRegisterRequest();
        request.setUsername("test_doctor");
        request.setRealName("测试医生");
        request.setEmail("doctor@test.com");
        request.setRole("doctor");
        return request;
    }

    /** 构造登录请求 */
    public static UserLoginRequest loginRequest(String username, String password) {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    /** 构造已激活的患者用户（含 BCrypt 加密密码） */
    public static Users activePatientUser() {
        return Users.builder()
                .userId(1001)
                .username("test_patient")
                .password(PasswordUtils.encrypt("123456"))
                .realName("测试患者")
                .phone("13800138000")
                .email("patient@test.com")
                .role("patient")
                .status("active")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /** 构造患者档案（余额 100 元，关联 userId=1001） */
    public static Patients patientProfile() {
        return Patients.builder()
                .patientId(2001)
                .userId(1001)
                .idCard("110101199001011234")
                .balance(new BigDecimal("100.00"))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /** 构造可用排班（5 个号源，指定星期几） */
    public static Schedules activeSchedule(int dayOfWeek) {
        return Schedules.builder()
                .scheduleId(3001)
                .doctorId(4001)
                .dayOfWeek(String.valueOf(dayOfWeek))
                .startTime(Time.valueOf("09:00:00"))
                .endTime(Time.valueOf("12:00:00"))
                .availableSlots(5)
                .status("active")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /** 构造待确认状态的预约记录 */
    public static Appointments pendingAppointment(LocalDate date) {
        return Appointments.builder()
                .patientId(2001)
                .doctorId(4001)
                .scheduleId(3001)
                .appointmentDate(Date.valueOf(date))
                .appointmentTime(Time.valueOf("09:00:00"))
                .status("pending")
                .symptoms("头痛")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
