import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { api } from '../../utils/api'
import PatientAccount from '../../views/user/PatientAccount.vue'

vi.mock('../../utils/api', () => ({
  api: {
    patientAccount: {
      recharge: vi.fn(),
      payment: vi.fn(),
    },
  },
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    },
  }
})

describe('PatientAccount.vue', () => {
  function mountComponent() {
    return mount(PatientAccount, {
      global: {
        stubs: { Plus: { template: '<span>plus</span>' } },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('初始状态', () => {
    it('显示初始余额 1000', async () => {
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.balance).toBe(1000)
    })

    it('加载 3 条交易记录', async () => {
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.transactions.length).toBe(3)
    })
  })

  describe('充值', () => {
    it('打开充值对话框并重置表单', async () => {
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.rechargeForm.amount = 999
      wrapper.vm.handleRecharge()
      expect(wrapper.vm.rechargeDialogVisible).toBe(true)
      expect(wrapper.vm.rechargeForm.amount).toBe('')
    })

    it('充值成功后更新余额', async () => {
      api.patientAccount.recharge.mockResolvedValue({})
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.rechargeForm.amount = 200
      wrapper.vm.rechargeForm.paymentMethod = 'alipay'
      wrapper.vm.rechargeFormRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleRechargeSubmit()
      expect(api.patientAccount.recharge).toHaveBeenCalledWith({
        patientId: 1,
        amount: 200,
        paymentMethod: 'alipay',
      })
      expect(wrapper.vm.balance).toBe(1200)
      expect(ElMessage.success).toHaveBeenCalledWith('充值成功')
    })

    it('充值失败显示错误', async () => {
      api.patientAccount.recharge.mockRejectedValue(new Error('充值失败'))
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.rechargeFormRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleRechargeSubmit()
      expect(ElMessage.error).toHaveBeenCalledWith('充值失败')
    })
  })

  describe('缴费', () => {
    it('余额不足时拒绝缴费', async () => {
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.paymentForm.amount = 2000
      wrapper.vm.paymentForm.consultationId = 1
      wrapper.vm.paymentFormRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handlePaymentSubmit()
      expect(ElMessage.error).toHaveBeenCalledWith('账户余额不足')
      expect(api.patientAccount.payment).not.toHaveBeenCalled()
    })

    it('缴费成功后扣减余额', async () => {
      api.patientAccount.payment.mockResolvedValue({})
      const wrapper = mountComponent()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.paymentForm.amount = 100
      wrapper.vm.paymentForm.consultationId = 1
      wrapper.vm.paymentForm.paymentMethod = 'wechat'
      wrapper.vm.paymentFormRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handlePaymentSubmit()
      expect(api.patientAccount.payment).toHaveBeenCalledWith({
        patientId: 1,
        consultationId: 1,
        amount: 100,
        paymentMethod: 'wechat',
      })
      expect(wrapper.vm.balance).toBe(900)
      expect(ElMessage.success).toHaveBeenCalledWith('缴费成功')
    })
  })
})
