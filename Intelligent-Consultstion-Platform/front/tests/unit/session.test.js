/* @vitest-environment jsdom */
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearSession, getSession, isSessionExpired, setSession } from '../../src/utils/session.js'

/**
 * 会话工具测试。
 * <p>
 * 覆盖会话读写、过期判断和异常 JSON 清理逻辑。
 */
describe('session utils', () => {
  beforeEach(() => {
    installStorageMock()
    localStorage.clear()
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-06-26T10:00:00Z'))
  })

  afterEach(() => {
    vi.useRealTimers()
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

  /** 验证 setSession 与 getSession 往返后数据保持一致。 */
  it('persists and reads session', () => {
    setSession({ token: 'abc', role: 'patient', expiresIn: 3600 })
    const session = getSession()
    expect(session.token).toBe('abc')
    expect(session.role).toBe('patient')
    expect(session.loginAt).toBeTypeOf('number')
  })

  /** 验证会话未过期时返回 false。 */
  it('detects active session as not expired', () => {
    expect(isSessionExpired({ loginAt: Date.now(), expiresIn: 3600 })).toBe(false)
  })

  /** 验证会话过期时返回 true。 */
  it('detects expired session', () => {
    expect(isSessionExpired({ loginAt: Date.now() - 4000 * 1000, expiresIn: 3600 })).toBe(true)
  })

  /** 验证非法 JSON 会被清理并返回 null。 */
  it('cleans invalid session json', () => {
    localStorage.setItem('demo_session', '{bad json')
    expect(getSession()).toBeNull()
    expect(localStorage.getItem('demo_session')).toBeNull()
  })

  /** 验证 clearSession 会移除本地会话。 */
  it('clears session', () => {
    localStorage.setItem('demo_session', '{}')
    clearSession()
    expect(localStorage.getItem('demo_session')).toBeNull()
  })
})
