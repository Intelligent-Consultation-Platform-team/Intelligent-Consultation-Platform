import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { api } from '../../utils/api'
import { getSession, setSession, clearSession, isSessionExpired } from '../../utils/session'
import App from '../../App.vue'

vi.mock('../../utils/api', () => ({
  api: {
    auth: {
      login: vi.fn(),
      register: vi.fn(),
    },
  },
}))

vi.mock('../../utils/session', () => ({
  getSession: vi.fn(),
  setSession: vi.fn(),
  clearSession: vi.fn(),
  isSessionExpired: vi.fn(() => false),
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

const mockPush = vi.fn()
const mockReplace = vi.fn()
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush, replace: mockReplace }),
    useRoute: () => ({ path: '/auth/login' }),
  }
})

describe('App.vue', () => {
  function mountApp() {
    return mount(App)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    getSession.mockReturnValue(null)
    isSessionExpired.mockReturnValue(false)
  })

  describe('初始渲染（未认证）', () => {
    it('未认证时显示 auth-screen', () => {
      getSession.mockReturnValue(null)
      const wrapper = mountApp()
      expect(wrapper.find('.auth-screen').exists()).toBe(true)
    })
  })

  describe('一键填充', () => {
    it('填充演示账号', () => {
      getSession.mockReturnValue(null)
      const wrapper = mountApp()
      wrapper.vm.fillDemo()
      expect(wrapper.vm.formModel.username).toBe('zhangsan')
      expect(wrapper.vm.formModel.password).toBe('123456')
    })
  })

  describe('登录', () => {
    it('登录成功后保存会话并跳转', async () => {
      getSession.mockReturnValue(null)
      api.auth.login.mockResolvedValue({
        token: 'tok123',
        user: { username: 'testuser', role: 'patient', realName: '测试' },
        expiresIn: 3600,
      })
      const wrapper = mountApp()
      wrapper.vm.formModel.username = 'testuser'
      wrapper.vm.formModel.password = '123456'
      wrapper.vm.formRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleSubmit()

      expect(api.auth.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: '123456',
      })
      expect(setSession).toHaveBeenCalledWith(expect.objectContaining({
        username: 'testuser',
        role: 'patient',
        token: 'tok123',
      }))
      expect(wrapper.vm.isAuthenticated).toBe(true)
      expect(ElMessage.success).toHaveBeenCalledWith('登录成功')
    })

    it('医生跳转到 /appoint/visit', async () => {
      getSession.mockReturnValue(null)
      api.auth.login.mockResolvedValue({
        token: 'tok',
        user: { username: 'doc', role: 'doctor' },
        expiresIn: 3600,
      })
      const wrapper = mountApp()
      wrapper.vm.formModel.username = 'doc'
      wrapper.vm.formModel.password = '123456'
      wrapper.vm.formRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleSubmit()
      expect(mockReplace).toHaveBeenCalledWith('/appoint/visit')
    })

    it('管理员跳转到 /home', async () => {
      getSession.mockReturnValue(null)
      api.auth.login.mockResolvedValue({
        token: 'tok',
        user: { username: 'admin', role: 'admin' },
        expiresIn: 3600,
      })
      const wrapper = mountApp()
      wrapper.vm.formModel.username = 'admin'
      wrapper.vm.formModel.password = '123456'
      wrapper.vm.formRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleSubmit()
      expect(mockReplace).toHaveBeenCalledWith('/home')
    })

    it('登录失败显示错误', async () => {
      getSession.mockReturnValue(null)
      api.auth.login.mockRejectedValue(new Error('用户名或密码错误'))
      const wrapper = mountApp()
      wrapper.vm.formModel.username = 'test'
      wrapper.vm.formModel.password = 'wrong'
      wrapper.vm.formRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleSubmit()
      expect(ElMessage.error).toHaveBeenCalledWith('用户名或密码错误')
    })
  })

  describe('注册', () => {
    it('注册成功切换到登录 tab', async () => {
      getSession.mockReturnValue(null)
      api.auth.register.mockResolvedValue({})
      const wrapper = mountApp()
      wrapper.vm.activeTab = 'register'
      wrapper.vm.formModel.username = 'newuser'
      wrapper.vm.formModel.realName = '新用户'
      wrapper.vm.formModel.phone = '13800138000'
      wrapper.vm.formModel.email = 'new@test.com'
      wrapper.vm.formModel.password = '123456'
      wrapper.vm.formModel.confirmPassword = '123456'
      wrapper.vm.formModel.role = 'patient'
      wrapper.vm.formRef = { validate: () => Promise.resolve() }
      await wrapper.vm.handleSubmit()

      expect(api.auth.register).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('注册成功，请登录')
      expect(wrapper.vm.activeTab).toBe('login')
    })
  })

  describe('会话恢复', () => {
    it('有效会话恢复认证状态', () => {
      getSession.mockReturnValue({
        username: 'saved', realName: '已保存', role: 'doctor', token: 'savedTok',
      })
      isSessionExpired.mockReturnValue(false)
      const wrapper = mountApp()
      expect(wrapper.vm.isAuthenticated).toBe(true)
      expect(wrapper.vm.currentUser.role).toBe('doctor')
    })

    it('无会话保持未认证', () => {
      getSession.mockReturnValue(null)
      const wrapper = mountApp()
      expect(wrapper.vm.isAuthenticated).toBe(false)
    })
  })

  describe('登出', () => {
    it('清除会话并跳转到登录页', () => {
      getSession.mockReturnValue({ username: 'u', role: 'patient' })
      isSessionExpired.mockReturnValue(false)
      const wrapper = mountApp()
      wrapper.vm.logout()
      expect(clearSession).toHaveBeenCalled()
      expect(wrapper.vm.isAuthenticated).toBe(false)
      expect(mockReplace).toHaveBeenCalledWith('/auth/login')
    })
  })

  describe('角色菜单过滤', () => {
    it('patient 角色过滤后只显示首页和预约', () => {
      getSession.mockReturnValue({ username: 'p', role: 'patient' })
      isSessionExpired.mockReturnValue(false)
      const wrapper = mountApp()
      const menu = wrapper.vm.filteredMenu
      const labels = menu.flatMap(item =>
        item.children ? [item.label, ...item.children.map(c => c.label)] : [item.label]
      )
      expect(labels).toContain('系统首页')
      expect(labels).toContain('预约挂号')
      expect(labels).not.toContain('信息管理')
      expect(labels).not.toContain('用户管理')
    })

    it('admin 角色看到全部菜单', () => {
      getSession.mockReturnValue({ username: 'a', role: 'admin' })
      isSessionExpired.mockReturnValue(false)
      const wrapper = mountApp()
      const menu = wrapper.vm.filteredMenu
      const labels = menu.flatMap(item =>
        item.children ? [item.label, ...item.children.map(c => c.label)] : [item.label]
      )
      expect(labels).toContain('信息管理')
      expect(labels).toContain('用户管理')
      expect(labels).toContain('预约就诊')
    })
  })

  describe('normalizeRole', () => {
    it('未知角色默认转为 patient', () => {
      getSession.mockReturnValue(null)
      const wrapper = mountApp()
      expect(wrapper.vm.normalizeRole('unknown')).toBe('patient')
      expect(wrapper.vm.normalizeRole('admin')).toBe('admin')
    })
  })
})
