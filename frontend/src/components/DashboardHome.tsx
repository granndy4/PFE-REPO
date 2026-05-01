type ReferentialModule = 'entreprises' | 'contrats' | 'employes' | 'vehicules'

type DashboardHomeProps = {
  userName: string
  userRole: string
  canManageReferential: boolean
  totals: {
    entreprises: number
    contrats: number
    employes: number
    vehicules: number
  }
  onOpenModule: (module: ReferentialModule) => void
}

export function DashboardHome({
  userName,
  userRole,
  canManageReferential,
  totals,
  onOpenModule,
}: DashboardHomeProps) {
  const totalObjects = totals.entreprises + totals.contrats + totals.employes + totals.vehicules

  return (
    <section className="workspace dashboardHome">
      <header className="dashboardHeader">
        <div>
          <p className="dashboardKicker">Dashboard</p>
          <h2>Welcome back, {userName}</h2>
          <p className="subtitle dashboardSubtitle">
            Role: {userRole} · {canManageReferential ? 'Operational access enabled' : 'Authentication-only access'}
          </p>
        </div>
        <div className="dashboardPulse">
          <strong>{totalObjects}</strong>
          <span>Total entities tracked</span>
        </div>
      </header>

      <div className="dashboardGrid">
        <article className="dashboardCard">
          <p>Entreprises</p>
          <strong>{totals.entreprises}</strong>
          <button type="button" onClick={() => onOpenModule('entreprises')} disabled={!canManageReferential}>
            Open module
          </button>
        </article>
        <article className="dashboardCard">
          <p>Contrats</p>
          <strong>{totals.contrats}</strong>
          <button type="button" onClick={() => onOpenModule('contrats')} disabled={!canManageReferential}>
            Open module
          </button>
        </article>
        <article className="dashboardCard">
          <p>Employes</p>
          <strong>{totals.employes}</strong>
          <button type="button" onClick={() => onOpenModule('employes')} disabled={!canManageReferential}>
            Open module
          </button>
        </article>
        <article className="dashboardCard">
          <p>Vehicules</p>
          <strong>{totals.vehicules}</strong>
          <button type="button" onClick={() => onOpenModule('vehicules')} disabled={!canManageReferential}>
            Open module
          </button>
        </article>
      </div>

      {!canManageReferential && (
        <div className="status">
          <p>Your account is authenticated successfully.</p>
          <p>Referential management remains available for ADMIN and MANAGER roles.</p>
        </div>
      )}
    </section>
  )
}
