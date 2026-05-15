const SESSION_KEY = 'demo_session'

const getSession = () => {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
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

export const request = async (url, options = {}) => {
  const {
    method = 'GET',
    params,
    data,
    headers = {},
    withAuth = true,
  } = options

  const session = getSession()
  const token = session?.token
  const tokenType = session?.tokenType || 'Bearer'

  const response = await fetch(`${url}${buildQuery(params)}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(withAuth && token ? { Authorization: `${tokenType} ${token}` } : {}),
      ...headers,
    },
    body: data ? JSON.stringify(data) : undefined,
  })

  if (response.status === 401) {
    localStorage.removeItem(SESSION_KEY)
    window.location.href = '/auth/login'
    throw new Error('登录已过期，请重新登录')
  }

  let result = null
  try {
    result = await response.json()
  } catch {
    throw new Error('接口返回格式错误')
  }

  if (!response.ok || result?.code !== 0) {
    throw new Error(result?.message || '请求失败')
  }

  return result.data
}

