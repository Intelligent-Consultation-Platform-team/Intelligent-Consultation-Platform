const SESSION_KEY = 'demo_session'

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

  let response
  try {
    response = await fetch(`${url}${buildQuery(params)}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(withAuth && token ? { Authorization: `${tokenType} ${token}` } : {}),
        ...headers,
      },
      body: data ? JSON.stringify(data) : undefined,
    })
  } catch {
    throw new Error('网络异常，请稍后重试')
  }

  if (response.status === 401) {
    clearSession()
    window.location.href = '/auth/login'
    throw new Error('登录已过期，请重新登录')
  }

  if (response.status === 403) {
    throw new Error('暂无权限访问该资源')
  }

  let result = null
  try {
    result = await response.json()
  } catch {
    throw new Error('接口返回格式错误')
  }

  const successCode = result?.code === 0 || result?.code === 200 || result?.success === true
  if (!response.ok || !successCode) {
    throw new Error(result?.message || `请求失败（${response.status}）`)
  }

  return result.data
}

