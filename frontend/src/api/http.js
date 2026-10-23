// Thin fetch wrapper. Knows nothing about stores: the token source and the
// "session expired" reaction are injected once from main.js.

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

let getToken = () => null
let onUnauthorized = () => {}

export function configureHttp({ tokenProvider, unauthorizedHandler }) {
  getToken = tokenProvider
  onUnauthorized = unauthorizedHandler
}

export async function request(path, { method = 'GET', body, query, auth = true } = {}) {
  const url = new URL(`/api${path}`, window.location.origin)
  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') url.searchParams.set(key, value)
  })

  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = auth ? getToken() : null
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) })
  } catch {
    throw new ApiError(0, 'The server could not be reached. Check your connection and try again.')
  }

  if (response.status === 204) return null
  const data = await response.json().catch(() => null)

  if (!response.ok) {
    // Services answer with RFC 7807 problem details: { status, detail, errors? }
    if (response.status === 401 && token) onUnauthorized()
    throw new ApiError(response.status, data?.detail ?? `Request failed (${response.status})`, data?.errors ?? {})
  }
  return data
}
