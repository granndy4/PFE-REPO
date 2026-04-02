import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  clearToken,
  fetchHealth,
  fetchMe,
  getStoredToken,
  login,
  register,
  storeToken,
  type HealthResponse,
  type MeResponse,
} from './api'

type AuthMode = 'login' | 'register'
type LoadState = 'idle' | 'loading' | 'success' | 'error'

export default function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [healthState, setHealthState] = useState<LoadState>('idle')
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [currentUser, setCurrentUser] = useState<MeResponse | null>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  async function checkBackendHealth() {
    setHealthState('loading')
    try {
      const result = await fetchHealth()
      setHealth(result)
      setHealthState('success')
    } catch {
      setHealth(null)
      setHealthState('error')
    }
  }

  async function loadCurrentUser() {
    try {
      const me = await fetchMe()
      setCurrentUser(me)
    } catch {
      clearToken()
      setCurrentUser(null)
    }
  }

  useEffect(() => {
    void checkBackendHealth()
    if (getStoredToken()) {
      void loadCurrentUser()
    }
  }, [])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage('')
    setIsSubmitting(true)

    try {
      if (authMode === 'login') {
        const authResult = await login({ email, password })
        storeToken(authResult.token)
      } else {
        const authResult = await register({ name, email, password })
        storeToken(authResult.token)
      }

      await loadCurrentUser()
      setPassword('')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Authentication failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleLogout() {
    clearToken()
    setCurrentUser(null)
  }

  return (
    <main className="page">
      <section className="card">
        <h1>Fuel Voucher Platform</h1>
        <p className="subtitle">Authentication module started (JWT login/register/profile).</p>

        <div className="statusRow">
          <button type="button" onClick={checkBackendHealth} disabled={healthState === 'loading'}>
            {healthState === 'loading' ? 'Checking...' : 'Backend health'}
          </button>
          {healthState === 'success' && health ? (
            <span className="badge ok">{health.status}</span>
          ) : (
            <span className="badge error">Offline</span>
          )}
        </div>

        {currentUser ? (
          <div className="status ok">
            <p>Signed in as {currentUser.name}</p>
            <p>Email: {currentUser.email}</p>
            <p>Role: {currentUser.role}</p>
            <button type="button" onClick={handleLogout}>Sign out</button>
          </div>
        ) : (
          <>
            <div className="tabs">
              <button
                type="button"
                className={authMode === 'login' ? 'tab active' : 'tab'}
                onClick={() => setAuthMode('login')}
              >
                Login
              </button>
              <button
                type="button"
                className={authMode === 'register' ? 'tab active' : 'tab'}
                onClick={() => setAuthMode('register')}
              >
                Register
              </button>
            </div>

            <form onSubmit={handleSubmit} className="form">
              {authMode === 'register' && (
                <label>
                  Full Name
                  <input
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    required
                    minLength={2}
                  />
                </label>
              )}

              <label>
                Email
                <input
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </label>

              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  minLength={8}
                />
              </label>

              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Please wait...' : authMode === 'login' ? 'Login' : 'Create account'}
              </button>

              {errorMessage && <p className="errorText">{errorMessage}</p>}
            </form>
          </>
        )}
      </section>
    </main>
  )
}
