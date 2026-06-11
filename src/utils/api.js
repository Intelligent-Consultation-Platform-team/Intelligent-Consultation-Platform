import { request } from './request'

const API_BASE_URL = ''

export const api = {
  // 认证相关
  auth: {
    // 注册
    register: (data) => request(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      withAuth: false,
      data
    }),
    // 登录
    login: (data) => request(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      withAuth: false,
      data
    })
  },
  
  // 科室管理
  departments: {
    // 获取科室列表
    getList: (params) => request(`${API_BASE_URL}/departments`, {
      params
    }),
    // 获取科室详情
    getInfo: (id) => request(`${API_BASE_URL}/departments/${id}`),
    // 新增科室
    create: (data) => request(`${API_BASE_URL}/departments`, {
      method: 'POST',
      data
    }),
    // 更新科室
    update: (data) => request(`${API_BASE_URL}/departments`, {
      method: 'PUT',
      data
    }),
    // 删除科室
    remove: (id) => request(`${API_BASE_URL}/departments/${id}`, {
      method: 'DELETE'
    })
  },
  
  // 用户管理
  users: {
    // 获取用户列表
    getList: (params) => request(`${API_BASE_URL}/users/list`, {
      params
    }),
    // 分页查询用户
    getPage: (params) => request(`${API_BASE_URL}/users/page`, {
      params
    }),
    // 保存用户
    save: (data) => request(`${API_BASE_URL}/users/save`, {
      method: 'POST',
      data
    }),
    // 更新用户
    update: (data) => request(`${API_BASE_URL}/users/update`, {
      method: 'PUT',
      data
    }),
    // 删除用户
    remove: (id) => request(`${API_BASE_URL}/users/remove/${id}`, {
      method: 'DELETE'
    }),
    // 获取用户详情
    getInfo: (id) => request(`${API_BASE_URL}/users/getInfo/${id}`),
    // 重置密码
    resetPassword: (data) => request(`${API_BASE_URL}/users/resetPassword`, {
      method: 'POST',
      data
    })
  },
  
  // 医生管理
  doctors: {
    // 获取医生列表
    getList: (params) => request(`${API_BASE_URL}/doctors`, {
      params
    }),
    // 获取医生详情
    getInfo: (id) => request(`${API_BASE_URL}/doctors/${id}`),
    // 新增医生
    create: (data) => request(`${API_BASE_URL}/doctors`, {
      method: 'POST',
      data
    }),
    // 更新医生
    update: (data) => request(`${API_BASE_URL}/doctors`, {
      method: 'PUT',
      data
    }),
    // 删除医生
    remove: (id) => request(`${API_BASE_URL}/doctors/${id}`, {
      method: 'DELETE'
    })
  },
  
  // 排班管理
  schedules: {
    // 获取医生排班
    getList: (params) => request(`${API_BASE_URL}/schedules`, {
      params
    }),
    // 获取排班详情
    getInfo: (id) => request(`${API_BASE_URL}/schedules/${id}`),
    // 新增排班
    create: (data) => request(`${API_BASE_URL}/schedules`, {
      method: 'POST',
      data
    }),
    // 更新排班
    update: (data) => request(`${API_BASE_URL}/schedules`, {
      method: 'PUT',
      data
    }),
    // 删除排班
    remove: (id) => request(`${API_BASE_URL}/schedules/${id}`, {
      method: 'DELETE'
    })
  },
  
  // 预约挂号
  appointment: {
    // 创建预约
    create: (data) => request(`${API_BASE_URL}/appointment`, {
      method: 'POST',
      data
    }),
    // 分页查询所有预约（管理员/医生用）
    getPage: (params) => request(`${API_BASE_URL}/appointment`, {
      params
    }),
    // 获取患者预约列表
    getPatientList: (patientId) => request(`${API_BASE_URL}/appointment/patient/${patientId}`),
    // 获取医生出诊列表
    getDoctorList: () => request(`${API_BASE_URL}/appointment/doctor`),
    // 获取预约详情
    getInfo: (id) => request(`${API_BASE_URL}/appointment/${id}`),
    // 取消预约
    cancel: (appointmentId) => request(`${API_BASE_URL}/appointment/${appointmentId}/cancel`, {
      method: 'PUT'
    }),
    // 患者签到
    confirm: (appointmentId) => request(`${API_BASE_URL}/appointment/${appointmentId}/confirm`, {
      method: 'PUT'
    }),
    // 完成就诊
    complete: (appointmentId) => request(`${API_BASE_URL}/appointment/${appointmentId}/complete`, {
      method: 'PUT'
    })
  },

  // 就诊记录
  consultation: {
    // 获取就诊记录
    getList: (params) => request(`${API_BASE_URL}/consultation`, {
      params
    }),
    // 获取就诊记录详情
    getInfo: (id) => request(`${API_BASE_URL}/consultation/${id}`),
    // 医生填写病历（问诊）
    create: (data) => request(`${API_BASE_URL}/consultation`, {
      method: 'POST',
      data
    })
  },

  // 住院管理
  hospitalization: {
    // 住院登记
    create: (data) => request(`${API_BASE_URL}/hospitalization`, {
      method: 'POST',
      data
    })
  },

  // 系统公告
  notice: {
    // 获取公告列表
    getList: (params) => request(`${API_BASE_URL}/notice`, {
      params
    }),
    // 获取公告详情
    getInfo: (id) => request(`${API_BASE_URL}/notice/${id}`),
    // 新增公告
    create: (data) => request(`${API_BASE_URL}/notice`, {
      method: 'POST',
      data
    }),
    // 更新公告
    update: (data) => request(`${API_BASE_URL}/notice`, {
      method: 'PUT',
      data
    }),
    // 删除公告
    remove: (id) => request(`${API_BASE_URL}/notice/${id}`, {
      method: 'DELETE'
    })
  },

  // 患者账户
  patient: {
    // 获取余额
    getBalance: (patientId) => request(`${API_BASE_URL}/patient/${patientId}/balance`),
    // 充值
    recharge: (patientId, data) => request(`${API_BASE_URL}/patient/${patientId}/recharge`, {
      method: 'POST',
      data
    }),
    // 缴费
    payment: (patientId, data) => request(`${API_BASE_URL}/patient/${patientId}/payment`, {
      method: 'POST',
      data
    })
  }
}