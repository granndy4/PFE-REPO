const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const TOKEN_STORAGE_KEY = 'fuel_voucher_token'

export type HealthResponse = {
  status: string
  service: string
  timestamp: string
}

export type AuthResponse = {
  token: string
  tokenType: string
  userId: number
  name: string
  email: string
  role: string
}

export type MeResponse = {
  userId: number
  name: string
  email: string
  role: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type RegisterRequest = {
  name: string
  email: string
  password: string
}

function buildHeaders(includeAuth: boolean): HeadersInit {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  }

  if (includeAuth) {
    const token = getStoredToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  return headers
}

export async function fetchHealth(): Promise<HealthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/health`)
  if (!response.ok) {
    throw new Error(`Backend request failed: ${response.status}`)
  }

  return response.json() as Promise<HealthResponse>
}

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: buildHeaders(false),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Invalid email or password')
  }

  return response.json() as Promise<AuthResponse>
}

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: buildHeaders(false),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create account')
  }

  return response.json() as Promise<AuthResponse>
}

export async function fetchMe(): Promise<MeResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Unauthorized')
  }

  return response.json() as Promise<MeResponse>
}

export function getStoredToken(): string | null {
  return window.localStorage.getItem(TOKEN_STORAGE_KEY)
}

export function storeToken(token: string): void {
  window.localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_STORAGE_KEY)
}
