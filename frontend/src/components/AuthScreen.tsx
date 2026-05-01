import type { FormEvent } from 'react'

type AuthMode = 'login' | 'register'
type LoadState = 'idle' | 'loading' | 'success' | 'error'

type AuthScreenProps = {
  authMode: AuthMode
  setAuthMode: (mode: AuthMode) => void
  name: string
  setName: (value: string) => void
  email: string
  setEmail: (value: string) => void
  password: string
  setPassword: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  isSubmitting: boolean
  errorMessage: string
  healthState: LoadState
  healthStatus?: string
  onCheckBackendHealth: () => void
}

export function AuthScreen({
  authMode,
  setAuthMode,
  name,
  setName,
  email,
  setEmail,
  password,
  setPassword,
  onSubmit,
  isSubmitting,
  errorMessage,
  healthState,
  healthStatus,
  onCheckBackendHealth,
}: AuthScreenProps) {
  return (
    <section className="authShell">
      <aside className="authBrand">
        <p className="authKicker">Secure Access</p>
        <h2>Fuel Voucher Control Center</h2>
        <p>
          Sign in to monitor enterprises, contracts, drivers, and vehicles in one operational dashboard.
        </p>

        <div className="authHighlights">
          <div>
            <strong>Role aware</strong>
            <span>Access rights are enforced for ADMIN and MANAGER workflows.</span>
          </div>
          <div>
            <strong>Real-time control</strong>
            <span>Track referential entities and keep statuses aligned.</span>
          </div>
          <div>
            <strong>Trusted API</strong>
            <span>JWT-secured backend with profile and health checks.</span>
          </div>
        </div>
      </aside>

      <div className="authCard">
        <div className="statusRow">
          <button type="button" onClick={onCheckBackendHealth} disabled={healthState === 'loading'}>
            {healthState === 'loading' ? 'Checking...' : 'Backend health'}
          </button>
          {healthState === 'success' ? (
            <span className="badge ok">{healthStatus ?? 'ONLINE'}</span>
          ) : (
            <span className="badge error">Offline</span>
          )}
        </div>

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

        <form onSubmit={onSubmit} className="form authForm">
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
      </div>
    </section>
  )
}
