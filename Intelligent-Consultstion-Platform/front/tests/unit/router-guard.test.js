/* @vitest-environment jsdom */
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

/**
 * 路由守卫测试。
 * <p>
 * 覆盖未登录跳转、角色权限控制、登录页回跳和公共路由放行。
 */
describe('router guard', () => {
  let guard

  beforeEach(async () => {
    installStorageMock()
    localStorage.clear()
    vi.spyOn(Date, 'now').mockReturnValue(new Date('2026-06-26T10:00:00Z').getTime())
    vi.resetModules()
    vi.doMock('vue-router', () => ({
      createWebHistory: () => ({}),
      createRouter: (config) => ({
        ...config,
        beforeEach: (fn) => {
          guard = fn
        },
        getRoutes: () => config.routes,
      }),
    }))
    await import('../../src/router/index.js')
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.doUnmock('vue-router')
  })

  function installStorageMock() {
    const store = new Map()
    Object.defineProperty(globalThis, 'localStorage', {
      configurable: true,
      value: {
        getItem: (key) => (store.has(key) ? store.get(key) : null),
        setItem: (key, value) => store.set(key, String(value)),
        removeItem: (key) => store.delete(key),
        clear: () => store.clear(),
      },
    })
  }

  function toRoute(path) {
    const routes = [
      { path: '/manage', meta: { roles: ['admin'] } },
      { path: '/appoint/visit', meta: { roles: ['doctor'] } },
      { path: '/auth/login', meta: { public: true } },
      { path: '/manage/notice', meta: { roles: ['admin'] } },
    ]
    const matched = routes.filter(route => path.startsWith(route.path))
    return { path, meta: {}, matched: matched.map(route => ({ meta: route.meta || {} })) }
  }

  /** 验证未登录访问受保护页面会跳转到登录页。 */
  it('redirects anonymous users to login', () => {
    expect(guard(toRoute('/manage'))).toBe('/auth/login')
  })

  /** 验证 patient 访问医生页面时会被重定向回首页。 */
  it('redirects unauthorized role to home', () => {
    localStorage.setItem('demo_session', JSON.stringify({ role: 'patient', loginAt: Date.now(), expiresIn: 3600 }))
    expect(guard(toRoute('/appoint/visit'))).toBe('/home')
  })

  /** 验证已登录用户访问登录页会自动回到首页。 */
  it('redirects logged in users away from auth pages', () => {
    localStorage.setItem('demo_session', JSON.stringify({ role: 'patient', loginAt: Date.now(), expiresIn: 3600 }))
    expect(guard(toRoute('/auth/login'))).toBe('/home')
  })

  /** 验证管理员可以访问管理页。 */
  it('allows admin access to manage pages', () => {
    localStorage.setItem('demo_session', JSON.stringify({ role: 'admin', loginAt: Date.now(), expiresIn: 3600 }))
    expect(guard(toRoute('/manage/notice'))).toBe(true)
  })
})
