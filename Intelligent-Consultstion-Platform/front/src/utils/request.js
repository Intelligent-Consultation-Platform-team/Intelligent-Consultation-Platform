const SESSION_KEY = 'demo_session'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export const getSession = () => {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export const clearSession = () => {
  localStorage.removeItem(SESSION_KEY)
}

export const isSessionExpired = (session) => {
  if (!session?.expiresIn || !session?.loginAt) return false
  return Date.now() > session.loginAt + Number(session.expiresIn) * 1000
}

const buildQuery = (params = {}) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.append(key, value)
    }
  })
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}

const normalizeUrl = (url) => `${API_BASE_URL}${url}`

export const request = async (url, options = {}) => {
  const {
    method = 'GET',
    params,
    data,
    headers = {},
    withAuth = true,
    timeout = 15000,
  } = options

  const session = getSession()
  const token = session?.token
  const tokenType = session?.tokenType || 'Bearer'
  const controller = typeof AbortController !== 'undefined' ? new AbortController() : null
  const timer = controller ? window.setTimeout(() => controller.abort(), timeout) : null

  let response
  const isFormData = typeof FormData !== 'undefined' && data instanceof FormData
  try {
    response = await fetch(`${normalizeUrl(url)}${buildQuery(params)}`, {
      method,
      signal: controller?.signal,
      headers: {
        ...(!isFormData ? { 'Content-Type': 'application/json' } : {}),
        ...(withAuth && token ? { Authorization: `${tokenType} ${token}` } : {}),
        ...headers,
      },
      body: data ? (isFormData ? data : JSON.stringify(data)) : undefined,
    })
  } catch (error) {
    if (error?.name === 'AbortError') {
      throw new Error('请求超时，请稍后重试')
    }
    throw new Error('网络异常，请稍后重试')
  } finally {
    if (timer) window.clearTimeout(timer)
  }

  if (response.status === 401) {
    clearSession()
    window.location.href = '/auth/login'
    throw new Error('登录已过期，请重新登录')
  }

  if (response.status === 403) {
    throw new Error('暂无权限访问该资源')
  }

  const text = await response.text()
  const result = text ? JSON.parse(text) : null

  const successCode = result?.code === 0 || result?.code === 200 || result?.success === true || response.ok
  if (!response.ok || !successCode) {
    throw new Error(result?.message || `请求失败（${response.status}）`)
  }

  return result?.data ?? result
}

