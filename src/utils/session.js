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

export const setSession = (data) => {
  localStorage.setItem(SESSION_KEY, JSON.stringify({ ...data, loginAt: Date.now() }))
}

export const clearSession = () => {
  localStorage.removeItem(SESSION_KEY)
}

export const isSessionExpired = (session) => {
  if (!session?.expiresIn || !session?.loginAt) return false
  return Date.now() > session.loginAt + Number(session.expiresIn) * 1000
}
