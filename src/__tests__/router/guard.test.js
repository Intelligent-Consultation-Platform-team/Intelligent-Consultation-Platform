import { createRouter, createMemoryHistory } from 'vue-router'

const SESSION_KEY = 'demo_session'

// 每次测试动态导入 router 以避免 localStorage 在模块加载时被读取的问题
async function createTestRouter() {
  const mod = await import('../../router/index.js')
  return mod.default
}

function setSession(data) {
  localStorage.setItem(SESSION_KEY, JSON.stringify({ ...data, loginAt: Date.now() }))
}

describe('router beforeEach 守卫', () => {
  let router

  beforeEach(async () => {
    localStorage.clear()
    // 导入前清空模块缓存
    vi.resetModules()
    router = await createTestRouter()
    // 使用 memory history 避免浏览器地址栏干扰
    router.options.history = createMemoryHistory()
  })

  describe('公开路由', () => {
    it('未登录时可访问 /auth/login', async () => {
      await router.push('/auth/login')
      expect(router.currentRoute.value.path).toBe('/auth/login')
    })

    it('未登录时可访问 /auth/register', async () => {
      await router.push('/auth/register')
      expect(router.currentRoute.value.path).toBe('/auth/register')
    })
  })

  describe('受保护路由,未登录', () => {
    it('/home 重定向到 /auth/login', async () => {
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/auth/login')
    })

    it('/manage/notice 重定向到 /auth/login', async () => {
      await router.push('/manage/notice')
      expect(router.currentRoute.value.path).toBe('/auth/login')
    })

    it('/appoint/book 重定向到 /auth/login', async () => {
      await router.push('/appoint/book')
      expect(router.currentRoute.value.path).toBe('/auth/login')
    })
  })

  describe('已登录访问认证页', () => {
    it('已登录时访问 /auth/login 重定向到 /home', async () => {
      setSession({ username: 'test', role: 'patient', token: 'x', expiresIn: 3600 })
      // 重新创建 router 以读取新的 localStorage
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/auth/login')
      expect(router.currentRoute.value.path).toBe('/home')
    })

    it('已登录时访问 /auth/register 重定向到 /home', async () => {
      setSession({ username: 'test', role: 'patient', token: 'x', expiresIn: 3600 })
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/auth/register')
      expect(router.currentRoute.value.path).toBe('/home')
    })
  })

  describe('角色权限', () => {
    it('patient 可访问 /appoint/book', async () => {
      setSession({ username: 'p', role: 'patient', token: 'x', expiresIn: 3600 })
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/appoint/book')
      expect(router.currentRoute.value.path).toBe('/appoint/book')
    })

    it('patient 访问 admin 专属路由重定向到 /home', async () => {
      setSession({ username: 'p', role: 'patient', token: 'x', expiresIn: 3600 })
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/manage/notice')
      expect(router.currentRoute.value.path).toBe('/home')
    })

    it('admin 可访问 /user/admin', async () => {
      setSession({ username: 'a', role: 'admin', token: 'x', expiresIn: 3600 })
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/user/admin')
      expect(router.currentRoute.value.path).toBe('/user/admin')
    })

    it('doctor 访问 admin 专属路由重定向到 /home', async () => {
      setSession({ username: 'd', role: 'doctor', token: 'x', expiresIn: 3600 })
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/manage/notice')
      expect(router.currentRoute.value.path).toBe('/home')
    })
  })

  describe('会话过期', () => {
    it('过期会话重定向到 /auth/login', async () => {
      const expiredSession = {
        username: 'test',
        role: 'patient',
        token: 'x',
        expiresIn: 1,
        loginAt: Date.now() - 5000,
      }
      localStorage.setItem(SESSION_KEY, JSON.stringify(expiredSession))
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/auth/login')
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    })
  })

  describe('损坏的 session JSON', () => {
    it('损坏数据被清除并重定向', async () => {
      localStorage.setItem(SESSION_KEY, '{bad json')
      vi.resetModules()
      router = await createTestRouter()
      router.options.history = createMemoryHistory()
      await router.push('/home')
      expect(router.currentRoute.value.path).toBe('/auth/login')
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    })
  })
})
