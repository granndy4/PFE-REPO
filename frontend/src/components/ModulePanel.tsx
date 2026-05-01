type ReferentialModule = 'dashboard' | 'entreprises' | 'contrats' | 'employes' | 'vehicules'

type ModulePanelProps = {
  activeModule: ReferentialModule
  onChangeModule: (module: ReferentialModule) => void
  totals: {
    entreprises: number
    contrats: number
    employes: number
    vehicules: number
  }
}

export function ModulePanel({ activeModule, onChangeModule, totals }: ModulePanelProps) {
  return (
    <section className="modulePanel">
      <div className="moduleTabs" role="tablist" aria-label="Referential modules">
        <button
          type="button"
          className={activeModule === 'dashboard' ? 'tab moduleTab active' : 'tab moduleTab'}
          onClick={() => onChangeModule('dashboard')}
        >
          Dashboard
        </button>
        <button
          type="button"
          className={activeModule === 'entreprises' ? 'tab moduleTab active' : 'tab moduleTab'}
          onClick={() => onChangeModule('entreprises')}
        >
          Entreprises
        </button>
        <button
          type="button"
          className={activeModule === 'contrats' ? 'tab moduleTab active' : 'tab moduleTab'}
          onClick={() => onChangeModule('contrats')}
        >
          Contrats
        </button>
        <button
          type="button"
          className={activeModule === 'employes' ? 'tab moduleTab active' : 'tab moduleTab'}
          onClick={() => onChangeModule('employes')}
        >
          Employes
        </button>
        <button
          type="button"
          className={activeModule === 'vehicules' ? 'tab moduleTab active' : 'tab moduleTab'}
          onClick={() => onChangeModule('vehicules')}
        >
          Vehicules
        </button>
      </div>

      <div className="moduleStats">
        <span className="metricPill">Entreprises: {totals.entreprises}</span>
        <span className="metricPill">Contrats: {totals.contrats}</span>
        <span className="metricPill">Employes: {totals.employes}</span>
        <span className="metricPill">Vehicules: {totals.vehicules}</span>
      </div>
    </section>
  )
}
