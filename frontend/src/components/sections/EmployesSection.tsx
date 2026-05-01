import type { Dispatch, FormEvent, SetStateAction } from 'react'
import type { Employe } from '../../api'

type EmployeFilterActif = 'ALL' | 'ACTIVE' | 'INACTIVE'
type EmployeSortOption = 'creeLe,desc' | 'nomComplet,asc' | 'codeEmploye,asc'

type EmployeFormState = {
  societeId: string
  entrepriseId: string
  codeEmploye: string
  nomComplet: string
  cin: string
  telephone: string
  email: string
  poste: string
  actif: boolean
}

type Props = {
  employeSocieteFilter: string
  setEmployeSocieteFilter: (value: string) => void
  employeEntrepriseFilter: string
  setEmployeEntrepriseFilter: (value: string) => void
  employeSearchFilter: string
  setEmployeSearchFilter: (value: string) => void
  employeActifFilter: EmployeFilterActif
  setEmployeActifFilter: (value: EmployeFilterActif) => void
  employeSort: EmployeSortOption
  setEmployeSort: (value: EmployeSortOption) => void
  employeState: 'idle' | 'loading' | 'success' | 'error'
  employeTotal: number
  employeError: string
  employePage: number
  employeTotalPages: number
  employes: Employe[]
  loadEmployes: (page: number) => Promise<void>
  employeForm: EmployeFormState
  setEmployeForm: Dispatch<SetStateAction<EmployeFormState>>
  handleEmployeSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>
  editingEmployeId: number | null
  isEmployeSubmitting: boolean
  resetEmployeForm: () => void
  employeFormError: string
  startEditEmploye: (employe: Employe) => void
  handleEmployeActif: (id: number, actif: boolean) => Promise<void>
}

export function EmployesSection(props: Props) {
  const {
    employeSocieteFilter,
    setEmployeSocieteFilter,
    employeEntrepriseFilter,
    setEmployeEntrepriseFilter,
    employeSearchFilter,
    setEmployeSearchFilter,
    employeActifFilter,
    setEmployeActifFilter,
    employeSort,
    setEmployeSort,
    employeState,
    employeTotal,
    employeError,
    employePage,
    employeTotalPages,
    employes,
    loadEmployes,
    employeForm,
    setEmployeForm,
    handleEmployeSubmit,
    editingEmployeId,
    isEmployeSubmitting,
    resetEmployeForm,
    employeFormError,
    startEditEmploye,
    handleEmployeActif,
  } = props

  return (
    <section className="workspace">
      <h2>Employes entreprise</h2>
      <p className="subtitle">CRUD employes relies on entreprises_contractees and writes audit entries.</p>

      <div className="filterRow">
        <label>
          Societe ID
          <input
            value={employeSocieteFilter}
            onChange={(event) => setEmployeSocieteFilter(event.target.value)}
            placeholder="1"
          />
        </label>

        <label>
          Entreprise ID
          <input
            value={employeEntrepriseFilter}
            onChange={(event) => setEmployeEntrepriseFilter(event.target.value)}
            placeholder="Optional"
          />
        </label>

        <label>
          Search
          <input
            value={employeSearchFilter}
            onChange={(event) => setEmployeSearchFilter(event.target.value)}
            placeholder="Code or nom"
          />
        </label>

        <label>
          Actif
          <select
            value={employeActifFilter}
            onChange={(event) => setEmployeActifFilter(event.target.value as EmployeFilterActif)}
          >
            <option value="ALL">ALL</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </label>

        <label>
          Sort
          <select
            value={employeSort}
            onChange={(event) => setEmployeSort(event.target.value as EmployeSortOption)}
          >
            <option value="creeLe,desc">Newest</option>
            <option value="nomComplet,asc">Nom complet A-Z</option>
            <option value="codeEmploye,asc">Code A-Z</option>
          </select>
        </label>

        <button type="button" onClick={() => void loadEmployes(0)} disabled={employeState === 'loading'}>
          {employeState === 'loading' ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      <p className="resultText">{employeTotal} employe(s)</p>
      {employeError && <p className="errorText">{employeError}</p>}
      <div className="paginationRow">
        <button
          type="button"
          className="secondary"
          onClick={() => void loadEmployes(Math.max(employePage - 1, 0))}
          disabled={employePage <= 0 || employeState === 'loading'}
        >
          Previous page
        </button>
        <span>
          Page {employeTotalPages === 0 ? 0 : employePage + 1} / {Math.max(employeTotalPages, 1)}
        </span>
        <button
          type="button"
          className="secondary"
          onClick={() => void loadEmployes(employePage + 1)}
          disabled={employePage + 1 >= employeTotalPages || employeState === 'loading'}
        >
          Next page
        </button>
      </div>

      <div className="workspaceGrid">
        <form className="form" onSubmit={(event) => void handleEmployeSubmit(event)}>
          <h3>{editingEmployeId === null ? 'Create employe' : 'Edit employe'}</h3>

          <label>
            Societe ID
            <input
              value={employeForm.societeId}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  societeId: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Entreprise ID
            <input
              value={employeForm.entrepriseId}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  entrepriseId: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Code employe
            <input
              value={employeForm.codeEmploye}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  codeEmploye: event.target.value,
                }))
              }
              required
              maxLength={50}
            />
          </label>

          <label>
            Nom complet
            <input
              value={employeForm.nomComplet}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  nomComplet: event.target.value,
                }))
              }
              required
              maxLength={140}
            />
          </label>

          <label>
            CIN
            <input
              value={employeForm.cin}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  cin: event.target.value,
                }))
              }
              maxLength={40}
            />
          </label>

          <label>
            Telephone
            <input
              value={employeForm.telephone}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  telephone: event.target.value,
                }))
              }
              maxLength={40}
            />
          </label>

          <label>
            Email
            <input
              type="email"
              value={employeForm.email}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  email: event.target.value,
                }))
              }
              maxLength={160}
            />
          </label>

          <label>
            Poste
            <input
              value={employeForm.poste}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  poste: event.target.value,
                }))
              }
              maxLength={100}
            />
          </label>

          <label className="checkboxLabel">
            <input
              type="checkbox"
              checked={employeForm.actif}
              onChange={(event) =>
                setEmployeForm((current) => ({
                  ...current,
                  actif: event.target.checked,
                }))
              }
            />
            Actif
          </label>

          <div className="actionRow">
            <button type="submit" disabled={isEmployeSubmitting}>
              {isEmployeSubmitting ? 'Saving...' : editingEmployeId === null ? 'Create employe' : 'Save changes'}
            </button>

            {editingEmployeId !== null && (
              <button type="button" className="secondary" onClick={resetEmployeForm}>
                Cancel edit
              </button>
            )}
          </div>

          {employeFormError && <p className="errorText">{employeFormError}</p>}
        </form>

        <div className="tableWrap">
          <table className="dataTable">
            <thead>
              <tr>
                <th>Code</th>
                <th>Nom complet</th>
                <th>Societe</th>
                <th>Entreprise</th>
                <th>Contact</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {employes.length === 0 ? (
                <tr>
                  <td colSpan={7} className="emptyCell">No employe found for current filters.</td>
                </tr>
              ) : (
                employes.map((employe) => (
                  <tr key={employe.id}>
                    <td>{employe.codeEmploye}</td>
                    <td>{employe.nomComplet}</td>
                    <td>{employe.societeId}</td>
                    <td>{employe.entrepriseId}</td>
                    <td>{employe.telephone || employe.email || 'N/A'}</td>
                    <td>
                      <span className={`badge ${employe.actif ? 'ok' : 'error'}`}>{employe.actif ? 'ACTIVE' : 'INACTIVE'}</span>
                    </td>
                    <td>
                      <div className="compactActions">
                        <button type="button" className="secondary" onClick={() => startEditEmploye(employe)}>Edit</button>
                        <button
                          type="button"
                          className="warn"
                          onClick={() => void handleEmployeActif(employe.id, false)}
                          disabled={!employe.actif}
                        >
                          Deactivate
                        </button>
                        <button
                          type="button"
                          className="okButton"
                          onClick={() => void handleEmployeActif(employe.id, true)}
                          disabled={employe.actif}
                        >
                          Activate
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  )
}
