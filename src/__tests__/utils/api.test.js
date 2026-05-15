import { api } from '../../utils/api'

vi.mock('../../utils/request', () => ({
  request: vi.fn(),
}))

const { request } = await import('../../utils/request')

beforeEach(() => {
  request.mockClear()
})

describe('api', () => {
  describe('auth', () => {
    it('register 传递正确的参数', () => {
      api.auth.register({ username: 'u', password: 'p' })
      expect(request).toHaveBeenCalledWith('/auth/register', {
        method: 'POST',
        withAuth: false,
        data: { username: 'u', password: 'p' },
      })
    })

    it('login 传递正确的参数', () => {
      api.auth.login({ username: 'u', password: 'p' })
      expect(request).toHaveBeenCalledWith('/auth/login', {
        method: 'POST',
        withAuth: false,
        data: { username: 'u', password: 'p' },
      })
    })
  })

  describe('departments', () => {
    it('getList 调用 /departments', () => {
      api.departments.getList()
      expect(request).toHaveBeenCalledWith('/departments')
    })
  })

  describe('doctors', () => {
    it('getList 传递 params', () => {
      api.doctors.getList({ deptId: 1 })
      expect(request).toHaveBeenCalledWith('/doctors', { params: { deptId: 1 } })
    })

    it('getList 无参调用', () => {
      api.doctors.getList()
      expect(request).toHaveBeenCalledWith('/doctors', { params: undefined })
    })
  })

  describe('schedules', () => {
    it('getList 传递 params', () => {
      api.schedules.getList({ date: '2026-01-01', deptId: 2 })
      expect(request).toHaveBeenCalledWith('/schedules', {
        params: { date: '2026-01-01', deptId: 2 },
      })
    })
  })

  describe('appointments', () => {
    it('create 发送 POST 请求', () => {
      api.appointments.create({ doctorId: 1, symptoms: '头疼' })
      expect(request).toHaveBeenCalledWith('/appointments', {
        method: 'POST',
        data: { doctorId: 1, symptoms: '头疼' },
      })
    })

    it('getPatientList 调用 /appointments/patient', () => {
      api.appointments.getPatientList()
      expect(request).toHaveBeenCalledWith('/appointments/patient')
    })
  })

  describe('consultations', () => {
    it('getList 传递 params', () => {
      api.consultations.getList({ page: 1, pageSize: 10 })
      expect(request).toHaveBeenCalledWith('/consultations', {
        params: { page: 1, pageSize: 10 },
      })
    })
  })

  describe('hospitalizations', () => {
    it('create 发送 POST 请求', () => {
      api.hospitalizations.create({ patientId: 1, reason: '发热' })
      expect(request).toHaveBeenCalledWith('/hospitalizations', {
        method: 'POST',
        data: { patientId: 1, reason: '发热' },
      })
    })
  })

  describe('notices', () => {
    it('getList 调用 /notices', () => {
      api.notices.getList()
      expect(request).toHaveBeenCalledWith('/notices')
    })
  })

  describe('patientAccount', () => {
    it('recharge 发送 POST 请求', () => {
      api.patientAccount.recharge({ patientId: 1, amount: 100, paymentMethod: 'alipay' })
      expect(request).toHaveBeenCalledWith('/recharge', {
        method: 'POST',
        data: { patientId: 1, amount: 100, paymentMethod: 'alipay' },
      })
    })

    it('payment 发送 POST 请求', () => {
      api.patientAccount.payment({ patientId: 1, amount: 50, paymentMethod: 'wechat' })
      expect(request).toHaveBeenCalledWith('/payment', {
        method: 'POST',
        data: { patientId: 1, amount: 50, paymentMethod: 'wechat' },
      })
    })
  })
})
