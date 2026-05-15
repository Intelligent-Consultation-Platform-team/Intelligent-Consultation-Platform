import { getSession, setSession, clearSession, isSessionExpired } from '../../utils/session'

const SESSION_KEY = 'demo_session'

describe('session', () => {
  describe('getSession', () => {
    it('空 localStorage 时返回 null', () => {
      expect(getSession()).toBeNull()
    })

    it('正常 JSON 时返回解析后对象', () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ user: 'test' }))
      expect(getSession()).toEqual({ user: 'test' })
    })

    it('损坏的 JSON 数据会清除并返回 null', () => {
      localStorage.setItem(SESSION_KEY, '{bad json}')
      expect(getSession()).toBeNull()
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    })
  })

  describe('setSession', () => {
    it('存储 JSON 并注入 loginAt 时间戳', () => {
      setSession({ token: 'abc', expiresIn: 3600 })
      const stored = JSON.parse(localStorage.getItem(SESSION_KEY))
      expect(stored.token).toBe('abc')
      expect(stored.expiresIn).toBe(3600)
      expect(typeof stored.loginAt).toBe('number')
      expect(stored.loginAt).toBeGreaterThan(0)
    })
  })

  describe('clearSession', () => {
    it('清除 session key', () => {
      localStorage.setItem(SESSION_KEY, JSON.stringify({ token: 'x' }))
      clearSession()
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    })
  })

  describe('isSessionExpired', () => {
    it('没有 expiresIn 返回 false', () => {
      expect(isSessionExpired({ loginAt: Date.now() })).toBe(false)
    })

    it('没有 loginAt 返回 false', () => {
      expect(isSessionExpired({ expiresIn: 3600 })).toBe(false)
    })

    it('null session 返回 false', () => {
      expect(isSessionExpired(null)).toBe(false)
    })

    it('已过期时返回 true', () => {
      const session = {
        loginAt: Date.now() - 7200 * 1000,
        expiresIn: 3600,
      }
      expect(isSessionExpired(session)).toBe(true)
    })

    it('未过期时返回 false', () => {
      const session = {
        loginAt: Date.now() - 1000,
        expiresIn: 3600,
      }
      expect(isSessionExpired(session)).toBe(false)
    })
  })
})
