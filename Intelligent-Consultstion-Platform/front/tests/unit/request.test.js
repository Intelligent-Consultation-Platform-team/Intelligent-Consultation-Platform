/* @vitest-environment jsdom */
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { request } from '../../src/utils/request.js'

/**
 * 请求封装测试。
 * <p>
 * 覆盖鉴权头、错误分支、超时、401/403 处理和响应解包逻辑。
 */
describe('request utils', () => {
  const originalLocation = window.location

  beforeEach(() => {
    installStorageMock()
    localStorage.clear()
    vi.stubGlobal('fetch', vi.fn())
    window.location = { href: '' }
    localStorage.setItem('demo_session', JSON.stringify({ token: 'token-1', tokenType: 'Bearer' }))
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    window.location = originalLocation
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

  /** 验证默认请求会自动携带 Authorization 头。 */
  it('adds auth header by default', async () => {
    fetch.mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { ok: true } }), { status: 200 }))
    await request('/api/test')
    expect(fetch).toHaveBeenCalled()
    const [, init] = fetch.mock.calls[0]
    expect(init.headers.Authorization).toBe('Bearer token-1')
  })

  /** 验证 withAuth=false 时不会携带 token。 */
  it('omits auth header when disabled', async () => {
    fetch.mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { ok: true } }), { status: 200 }))
    await request('/api/test', { withAuth: false })
    const [, init] = fetch.mock.calls[0]
    expect(init.headers.Authorization).toBeUndefined()
  })

  /** 验证后端返回 code!=0 时会抛出业务错误信息。 */
  it('throws wrapped api error message', async () => {
    fetch.mockResolvedValueOnce(new Response(JSON.stringify({ code: 40001, message: '参数错误' }), { status: 200 }))
    await expect(request('/api/test')).rejects.toThrow('参数错误')
  })

  /** 验证 401 响应会清理会话并跳转登录页。 */
  it('handles 401 by clearing session and redirecting', async () => {
    fetch.mockResolvedValueOnce(new Response('', { status: 401 }))
    await expect(request('/api/test')).rejects.toThrow('登录已过期，请重新登录')
    expect(localStorage.getItem('demo_session')).toBeNull()
    expect(window.location.href).toBe('/auth/login')
  })

  /** 验证 403 响应会直接抛出无权限错误。 */
  it('throws 403 access error', async () => {
    fetch.mockResolvedValueOnce(new Response('', { status: 403 }))
    await expect(request('/api/test')).rejects.toThrow('暂无权限访问该资源')
  })
})
