const TOKEN_KEY = 'recruitment_token'
const USER_KEY = 'recruitment_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser(): { userId: number; username: string; role: string } | null {
  const u = localStorage.getItem(USER_KEY)
  return u ? JSON.parse(u) : null
}

export function setUser(user: { userId: number; username: string; role: string }) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export async function request<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(url, { ...options, headers })
  const json = await res.json()

  if (json.code !== 0) {
    if (json.code === 401) {
      clearToken()
      window.location.href = '/login'
    }
    throw new Error(json.message || '请求失败')
  }
  return json.data
}

export const api = {
  get: <T>(url: string) => request<T>(url),
  post: <T>(url: string, body?: unknown) =>
    request<T>(url, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(url: string, body?: unknown) =>
    request<T>(url, { method: 'PUT', body: JSON.stringify(body) }),
  delete: <T>(url: string) => request<T>(url, { method: 'DELETE' }),
}
