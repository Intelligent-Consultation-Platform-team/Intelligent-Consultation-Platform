import { request, buildQuery } from '../../utils/request'

const SESSION_KEY = 'demo_session'

describe('request', () => {
  let mockFetch

  beforeEach(() => {
    mockFetch = vi.fn()
    global.fetch = mockFetch
    localStorage.clear()
  })

  describe('buildQuery', () => {
    it('空 params 返回空字符串', () => {
      expect(buildQuery()).toBe('')
      expect(buildQuery({})).toBe('')
    })

    it('正常 params 构建 query string', () => {
      expect(buildQuery({ a: 1, b: 'x' })).toBe('?a=1&b=x')
    })

    it('跳过 null/undefined/空字符串', () => {
      expect(buildQuery({ a: 1, b: null, c: '', d: undefined })).toBe('?a=1')
    })
  })

  describe('GET 请求', () => {
    it('构建不带 body 的 GET 请求', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: { id: 1 } }),
      })
      await request('/api/test')
      expect(mockFetch).toHaveBeenCalledWith('/api/test', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
        body: undefined,
      })
    })

    it('拼接 query string', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: [] }),
      })
      await request('/api/test', { params: { a: 1, b: 'x' } })
      expect(mockFetch).toHaveBeenCalledWith('/api/test?a=1&b=x', expect.anything())
    })
  })

  describe('POST 请求', () => {
    it('发送 JSON body', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: { ok: true } }),
      })
      await request('/api/test', { method: 'POST', data: { x: 1 } })
      expect(mockFetch).toHaveBeenCalledWith('/api/test', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ x: 1 }),
      })
    })
  })

  describe('Authorization 头', () => {
    it('session 中有 token 时附加 Authorization 头', async () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'tok123' }))
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test')
      expect(mockFetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Bearer tok123' }),
      }))
    })

    it('自定义 tokenType', async () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'x', tokenType: 'Custom' }))
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test')
      expect(mockFetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Custom x' }),
      }))
    })

    it('withAuth 为 false 时不附加 Authorization 头', async () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'tok123' }))
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test', { withAuth: false })
      const call = mockFetch.mock.calls[0][1]
      expect(call.headers.Authorization).toBeUndefined()
    })

    it('无 token 时不附加 Authorization 头', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test')
      const call = mockFetch.mock.calls[0][1]
      expect(call.headers.Authorization).toBeUndefined()
    })
  })

  describe('合并自定义 headers', () => {
    it('合并自定义 headers', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test', { headers: { 'X-Custom': 'val' } })
      expect(mockFetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
          'X-Custom': 'val',
        }),
      }))
    })
  })

  describe('错误处理', () => {
    it('网络异常时抛出提示', async () => {
      mockFetch.mockRejectedValue(new Error('net'))
      await expect(request('/api/test')).rejects.toThrow('网络异常，请稍后重试')
    })

    it('401 时清除会话并重定向', async () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'old' }))
      const locationMock = { href: '' }
      Object.defineProperty(window, 'location', {
        value: locationMock,
        writable: true,
      })
      mockFetch.mockResolvedValue({
        status: 401,
      })
      await expect(request('/api/test')).rejects.toThrow('登录已过期，请重新登录')
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
      expect(window.location.href).toBe('/auth/login')
    })

    it('403 时抛出权限错误', async () => {
      mockFetch.mockResolvedValue({
        status: 403,
      })
      await expect(request('/api/test')).rejects.toThrow('暂无权限访问该资源')
    })

    it('JSON 解析失败时抛出格式错误', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.reject(new Error('parse error')),
      })
      await expect(request('/api/test')).rejects.toThrow('接口返回格式错误')
    })

    it('非 ok 响应抛出服务端错误消息', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: () => Promise.resolve({ code: -1, message: 'Server Error' }),
      })
      await expect(request('/api/test')).rejects.toThrow('Server Error')
    })

    it('业务错误码非零时抛出消息', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 1, message: '业务处理失败' }),
      })
      await expect(request('/api/test')).rejects.toThrow('业务处理失败')
    })
  })

  describe('成功响应', () => {
    it('返回 response.data', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: { id: 1, name: 'test' } }),
      })
      const result = await request('/api/test')
      expect(result).toEqual({ id: 1, name: 'test' })
    })

    it('tokenType 默认为 Bearer', async () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'tok' }))
      mockFetch.mockResolvedValue({
        ok: true,
        status: 200,
        json: () => Promise.resolve({ code: 0, data: {} }),
      })
      await request('/api/test')
      expect(mockFetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Bearer tok' }),
      }))
    })
  })
})
