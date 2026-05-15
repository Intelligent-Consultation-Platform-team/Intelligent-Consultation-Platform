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
    getList: () => request(`${API_BASE_URL}/departments`)
  },
  
  // 医生管理
  doctors: {
    // 获取医生列表
    getList: (params) => request(`${API_BASE_URL}/doctors`, {
      params
    })
  },
  
  // 排班管理
  schedules: {
    // 获取医生排班
    getList: (params) => request(`${API_BASE_URL}/schedules`, {
      params
    })
  },
  
  // 预约挂号
  appointments: {
    // 创建预约
    create: (data) => request(`${API_BASE_URL}/appointments`, {
      method: 'POST',
      data
    }),
    // 获取患者预约列表
    getPatientList: () => request(`${API_BASE_URL}/appointments/patient`)
  },
  
  // 就诊记录
  consultations: {
    // 获取就诊记录
    getList: (params) => request(`${API_BASE_URL}/consultations`, {
      params
    })
  },
  
  // 住院管理
  hospitalizations: {
    // 住院登记
    create: (data) => request(`${API_BASE_URL}/hospitalizations`, {
      method: 'POST',
      data
    })
  },
  
  // 系统公告
  notices: {
    // 获取公告列表
    getList: () => request(`${API_BASE_URL}/notices`)
  },
  
  // 患者账户
  patientAccount: {
    // 充值
    recharge: (data) => request(`${API_BASE_URL}/recharge`, {
      method: 'POST',
      data
    }),
    // 缴费
    payment: (data) => request(`${API_BASE_URL}/payment`, {
      method: 'POST',
      data
    })
  }
}