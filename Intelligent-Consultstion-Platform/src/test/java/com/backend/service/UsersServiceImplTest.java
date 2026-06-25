package com.backend.service;

import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AdminsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.model.entity.Users;
import com.backend.service.impl.UsersServiceImpl;
import com.backend.utils.JwtUtils;
import com.backend.utils.PasswordUtils;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static com.backend.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试。
 * <p>
 * 测试功能：用户注册、登录、重置密码、逻辑删除等业务逻辑与参数校验。
 * Mock 所有 Mapper，不依赖数据库。
 * 对应生产类：{@link UsersServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock
    private UsersMapper usersMapper;
    @Mock
    private DoctorsMapper doctorsMapper;
    @Mock
    private PatientsMapper patientsMapper;
    @Mock
    private AdminsMapper adminsMapper;
    @Mock
    private JwtUtils jwtUtils;

    private UsersServiceImpl usersService;

    @BeforeEach
    void setUp() {
        usersService = new UsersServiceImpl();
        ReflectionTestUtils.setField(usersService, "mapper", usersMapper);
        ReflectionTestUtils.setField(usersService, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(usersService, "doctorsMapper", doctorsMapper);
        ReflectionTestUtils.setField(usersService, "patientsMapper", patientsMapper);
        ReflectionTestUtils.setField(usersService, "adminsMapper", adminsMapper);
    }

    /** 验证患者注册成功：写入 users 表并创建 patients 记录，不创建 doctors 记录 */
    @Test
    void userRegister_patient_success() {
        when(usersMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
        when(usersMapper.insert(any(Users.class), anyBoolean())).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setUserId(1001);
            return 1;
        });

        Long userId = usersService.userRegister(validPatientRegisterRequest());

        assertEquals(1001L, userId);
        verify(patientsMapper).insert(any());
        verify(doctorsMapper, never()).insert(any());
    }

    /** 验证邮箱格式不合法时抛出 EMAIL_FORMAT_ERROR */
    @Test
    void userRegister_invalidEmail_throwsException() {
        UserRegisterRequest request = validPatientRegisterRequest();
        request.setEmail("invalid-email");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userRegister(request));

        assertEquals(ErrorCode.EMAIL_FORMAT_ERROR.getCode(), ex.getCode());
    }

    /** 验证用户名已存在时抛出 USERNAME_EXISTS */
    @Test
    void userRegister_duplicateUsername_throwsException() {
        when(usersMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userRegister(validPatientRegisterRequest()));

        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), ex.getCode());
    }

    /** 验证两次密码不一致时抛出 PARAMS_ERROR */
    @Test
    void userRegister_passwordMismatch_throwsException() {
        UserRegisterRequest request = validPatientRegisterRequest();
        request.setConfirmPassword("654321");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userRegister(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    /** 验证手机号格式不合法时抛出 PARAMS_ERROR */
    @Test
    void userRegister_invalidPhone_throwsException() {
        UserRegisterRequest request = validPatientRegisterRequest();
        request.setPhone("12345");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userRegister(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    /** 验证使用用户名登录成功，返回 JWT token 与用户信息 */
    @Test
    void userLogin_withUsername_success() {
        Users user = activePatientUser();
        when(usersMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(user);
        when(jwtUtils.generateToken(1001, "test_patient", "patient")).thenReturn("token-abc");
        when(jwtUtils.getExpirationTime()).thenReturn(7200L);

        Map<String, Object> result = usersService.userLogin(loginRequest("test_patient", "123456"));

        assertEquals("token-abc", result.get("token"));
        assertEquals("Bearer", result.get("tokenType"));
        assertNotNull(result.get("user"));
    }

    /** 验证使用手机号登录成功（支持手机号作为登录账号） */
    @Test
    void userLogin_withPhone_success() {
        Users user = activePatientUser();
        when(usersMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(user);
        when(jwtUtils.generateToken(anyInt(), anyString(), anyString())).thenReturn("token-abc");
        when(jwtUtils.getExpirationTime()).thenReturn(7200L);

        Map<String, Object> result = usersService.userLogin(loginRequest("13800138000", "123456"));

        assertEquals("token-abc", result.get("token"));
    }

    /** 验证密码错误时抛出 USER_OR_PASSWORD_ERROR（不暴露具体是用户名还是密码错误） */
    @Test
    void userLogin_wrongPassword_throwsException() {
        Users user = activePatientUser();
        when(usersMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userLogin(loginRequest("test_patient", "wrong")));

        assertEquals(ErrorCode.USER_OR_PASSWORD_ERROR.getCode(), ex.getCode());
    }

    /** 验证用户不存在时抛出 USER_OR_PASSWORD_ERROR */
    @Test
    void userLogin_userNotFound_throwsException() {
        when(usersMapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersService.userLogin(loginRequest("unknown", "123456")));

        assertEquals(ErrorCode.USER_OR_PASSWORD_ERROR.getCode(), ex.getCode());
    }

    /** 验证重置密码为默认 123456，并以 BCrypt 加密存储 */
    @Test
    void resetPassword_success() {
        when(usersMapper.update(any(Users.class), anyBoolean())).thenReturn(1);

        boolean result = usersService.resetPassword(1001);

        assertTrue(result);
        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersMapper).update(captor.capture(), eq(true));
        assertTrue(PasswordUtils.verify("123456", captor.getValue().getPassword()));
    }

    /** 验证 userId 为空时重置密码返回 false */
    @Test
    void resetPassword_nullUserId_returnsFalse() {
        assertFalse(usersService.resetPassword(null));
    }

    /** 验证逻辑删除用户：将 status 设为 inactive */
    @Test
    void deleteUser_setsInactive() {
        when(usersMapper.update(any(Users.class), anyBoolean())).thenReturn(1);

        assertTrue(usersService.deleteUser(1001));

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersMapper).update(captor.capture(), eq(true));
        assertEquals("inactive", captor.getValue().getStatus());
    }
}
