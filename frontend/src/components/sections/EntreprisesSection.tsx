import type { Dispatch, FormEvent, SetStateAction } from 'react'
import type { Entreprise, EntrepriseStatut } from '../../api'

type EntrepriseFilterStatut = 'ALL' | EntrepriseStatut
type EntrepriseSortOption = 'creeLe,desc' | 'raisonSociale,asc' | 'codeEntreprise,asc'

type EntrepriseFormState = {
  societeId: string
  codeEntreprise: string
  raisonSociale: string
  nomCourt: string
  matriculeFiscal: string
  adresseFacturation: string
  nomContact: string
  emailContact: string
  telephoneContact: string
  statut: EntrepriseStatut
}

type Props = {
  societeFilter: string
  setSocieteFilter: (value: string) => void
  searchFilter: string
  setSearchFilter: (value: string) => void
  statutFilter: EntrepriseFilterStatut
  setStatutFilter: (value: EntrepriseFilterStatut) => void
  entrepriseSort: EntrepriseSortOption
  setEntrepriseSort: (value: EntrepriseSortOption) => void
  entrepriseState: 'idle' | 'loading' | 'success' | 'error'
  entrepriseTotal: number
  entrepriseError: string
  entreprisePage: number
  entrepriseTotalPages: number
  entreprises: Entreprise[]
  loadEntreprises: (page: number) => Promise<void>
  entrepriseForm: EntrepriseFormState
  setEntrepriseForm: Dispatch<SetStateAction<EntrepriseFormState>>
  handleEntrepriseSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>
  editingEntrepriseId: number | null
  isEntrepriseSubmitting: boolean
  resetEntrepriseForm: () => void
  entrepriseFormError: string
  startEditEntreprise: (entreprise: Entreprise) => void
  handleEntrepriseStatut: (id: number, statut: EntrepriseStatut) => Promise<void>
}

export function EntreprisesSection(props: Props) {
  const {
    societeFilter,
    setSocieteFilter,
    searchFilter,
    setSearchFilter,
    statutFilter,
    setStatutFilter,
    entrepriseSort,
    setEntrepriseSort,
    entrepriseState,
    entrepriseTotal,
    entrepriseError,
    entreprisePage,
    entrepriseTotalPages,
    entreprises,
    loadEntreprises,
    entrepriseForm,
    setEntrepriseForm,
    handleEntrepriseSubmit,
    editingEntrepriseId,
    isEntrepriseSubmitting,
    resetEntrepriseForm,
    entrepriseFormError,
    startEditEntreprise,
    handleEntrepriseStatut,
  } = props

  return (
    <section className="workspace">
      <h2>Entreprises contractees</h2>
      <p className="subtitle">Module referentiel: CRUD, pagination et tri.</p>

      <div className="filterRow">
        <label>
          Societe ID
          <input value={societeFilter} onChange={(event) => setSocieteFilter(event.target.value)} placeholder="1" />
        </label>

        <label>
          Search
          <input
            value={searchFilter}
            onChange={(event) => setSearchFilter(event.target.value)}
            placeholder="Code or raison sociale"
          />
        </label>

        <label>
          Statut
          <select
            value={statutFilter}
            onChange={(event) => setStatutFilter(event.target.value as EntrepriseFilterStatut)}
          >
            <option value="ALL">ALL</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="SUSPENDED">SUSPENDED</option>
            <option value="CLOSED">CLOSED</option>
          </select>
        </label>

        <label>
          Sort
          <select
            value={entrepriseSort}
            onChange={(event) => setEntrepriseSort(event.target.value as EntrepriseSortOption)}
          >
            <option value="creeLe,desc">Newest</option>
            <option value="raisonSociale,asc">Raison sociale A-Z</option>
            <option value="codeEntreprise,asc">Code A-Z</option>
          </select>
        </label>

        <button type="button" onClick={() => void loadEntreprises(0)} disabled={entrepriseState === 'loading'}>
          {entrepriseState === 'loading' ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      <p className="resultText">{entrepriseTotal} entreprise(s)</p>
      {entrepriseError && <p className="errorText">{entrepriseError}</p>}
      <div className="paginationRow">
        <button
          type="button"
          className="secondary"
          onClick={() => void loadEntreprises(Math.max(entreprisePage - 1, 0))}
          disabled={entreprisePage <= 0 || entrepriseState === 'loading'}
        >
          Previous page
        </button>
        <span>
          Page {entrepriseTotalPages === 0 ? 0 : entreprisePage + 1} / {Math.max(entrepriseTotalPages, 1)}
        </span>
        <button
          type="button"
          className="secondary"
          onClick={() => void loadEntreprises(entreprisePage + 1)}
          disabled={entreprisePage + 1 >= entrepriseTotalPages || entrepriseState === 'loading'}
        >
          Next page
        </button>
      </div>

      <div className="workspaceGrid">
        <form className="form" onSubmit={(event) => void handleEntrepriseSubmit(event)}>
          <h3>{editingEntrepriseId === null ? 'Create entreprise' : 'Edit entreprise'}</h3>

          <label>
            Societe ID
            <input
              value={entrepriseForm.societeId}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  societeId: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Code entreprise
            <input
              value={entrepriseForm.codeEntreprise}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  codeEntreprise: event.target.value,
                }))
              }
              required
              maxLength={50}
            />
          </label>

          <label>
            Raison sociale
            <input
              value={entrepriseForm.raisonSociale}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  raisonSociale: event.target.value,
                }))
              }
              required
              maxLength={200}
            />
          </label>

          <label>
            Nom court
            <input
              value={entrepriseForm.nomCourt}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  nomCourt: event.target.value,
                }))
              }
              maxLength={120}
            />
          </label>

          <label>
            Matricule fiscal
            <input
              value={entrepriseForm.matriculeFiscal}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  matriculeFiscal: event.target.value,
                }))
              }
              maxLength={80}
            />
          </label>

          <label>
            Adresse facturation
            <textarea
              value={entrepriseForm.adresseFacturation}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  adresseFacturation: event.target.value,
                }))
              }
              rows={3}
            />
          </label>

          <label>
            Nom contact
            <input
              value={entrepriseForm.nomContact}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  nomContact: event.target.value,
                }))
              }
              maxLength={120}
            />
          </label>

          <label>
            Email contact
            <input
              type="email"
              value={entrepriseForm.emailContact}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  emailContact: event.target.value,
                }))
              }
              maxLength={160}
            />
          </label>

          <label>
            Telephone contact
            <input
              value={entrepriseForm.telephoneContact}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  telephoneContact: event.target.value,
                }))
              }
              maxLength={40}
            />
          </label>

          <label>
            Statut
            <select
              value={entrepriseForm.statut}
              onChange={(event) =>
                setEntrepriseForm((current) => ({
                  ...current,
                  statut: event.target.value as EntrepriseStatut,
                }))
              }
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
              <option value="CLOSED">CLOSED</option>
            </select>
          </label>

          <div className="actionRow">
            <button type="submit" disabled={isEntrepriseSubmitting}>
              {isEntrepriseSubmitting ? 'Saving...' : editingEntrepriseId === null ? 'Create entreprise' : 'Save changes'}
            </button>

            {editingEntrepriseId !== null && (
              <button type="button" className="secondary" onClick={resetEntrepriseForm}>
                Cancel edit
              </button>
            )}
          </div>

          {entrepriseFormError && <p className="errorText">{entrepriseFormError}</p>}
        </form>

        <div className="tableWrap">
          <table className="dataTable">
            <thead>
              <tr>
                <th>Code</th>
                <th>Raison Sociale</th>
                <th>Societe</th>
                <th>Statut</th>
                <th>Contact</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {entreprises.length === 0 ? (
                <tr>
                  <td colSpan={6} className="emptyCell">No entreprise found for current filters.</td>
                </tr>
              ) : (
                entreprises.map((entreprise) => (
                  <tr key={entreprise.id}>
                    <td>{entreprise.codeEntreprise}</td>
                    <td>{entreprise.raisonSociale}</td>
                    <td>{entreprise.societeId}</td>
                    <td>
                      <span className={`badge ${entreprise.statut === 'ACTIVE' ? 'ok' : 'error'}`}>{entreprise.statut}</span>
                    </td>
                    <td>{entreprise.nomContact || 'N/A'}</td>
                    <td>
                      <div className="compactActions">
                        <button type="button" className="secondary" onClick={() => startEditEntreprise(entreprise)}>Edit</button>
                        <button
                          type="button"
                          className="warn"
                          onClick={() => void handleEntrepriseStatut(entreprise.id, 'SUSPENDED')}
                          disabled={entreprise.statut === 'SUSPENDED'}
                        >
                          Suspend
                        </button>
                        <button
                          type="button"
                          className="okButton"
                          onClick={() => void handleEntrepriseStatut(entreprise.id, 'ACTIVE')}
                          disabled={entreprise.statut === 'ACTIVE'}
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
