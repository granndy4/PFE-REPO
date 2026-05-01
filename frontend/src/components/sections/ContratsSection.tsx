import type { Dispatch, FormEvent, SetStateAction } from 'react'
import type { Contrat, ContratStatut } from '../../api'

type ContratFilterStatut = 'ALL' | ContratStatut
type ContratSortOption = 'creeLe,desc' | 'numeroContrat,asc' | 'dateDebut,desc'

type ContratFormState = {
  societeId: string
  entrepriseId: string
  numeroContrat: string
  dateDebut: string
  dateFin: string
  codeDevise: string
  delaiPaiementJours: string
  montantMaxMensuel: string
  statut: ContratStatut
  signeLe: string
  notes: string
}

type Props = {
  contratSocieteFilter: string
  setContratSocieteFilter: (value: string) => void
  contratEntrepriseFilter: string
  setContratEntrepriseFilter: (value: string) => void
  contratSearchFilter: string
  setContratSearchFilter: (value: string) => void
  contratStatutFilter: ContratFilterStatut
  setContratStatutFilter: (value: ContratFilterStatut) => void
  contratSort: ContratSortOption
  setContratSort: (value: ContratSortOption) => void
  contratState: 'idle' | 'loading' | 'success' | 'error'
  contratTotal: number
  contratError: string
  contratPage: number
  contratTotalPages: number
  contrats: Contrat[]
  loadContrats: (page: number) => Promise<void>
  contratForm: ContratFormState
  setContratForm: Dispatch<SetStateAction<ContratFormState>>
  handleContratSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>
  editingContratId: number | null
  isContratSubmitting: boolean
  resetContratForm: () => void
  contratFormError: string
  startEditContrat: (contrat: Contrat) => void
  handleContratStatut: (id: number, statut: ContratStatut) => Promise<void>
}

export function ContratsSection(props: Props) {
  const {
    contratSocieteFilter,
    setContratSocieteFilter,
    contratEntrepriseFilter,
    setContratEntrepriseFilter,
    contratSearchFilter,
    setContratSearchFilter,
    contratStatutFilter,
    setContratStatutFilter,
    contratSort,
    setContratSort,
    contratState,
    contratTotal,
    contratError,
    contratPage,
    contratTotalPages,
    contrats,
    loadContrats,
    contratForm,
    setContratForm,
    handleContratSubmit,
    editingContratId,
    isContratSubmitting,
    resetContratForm,
    contratFormError,
    startEditContrat,
    handleContratStatut,
  } = props

  return (
    <section className="workspace">
      <h2>Contrats client</h2>
      <p className="subtitle">CRUD contrats client with referential consistency and persistent audit.</p>

      <div className="filterRow">
        <label>
          Societe ID
          <input
            value={contratSocieteFilter}
            onChange={(event) => setContratSocieteFilter(event.target.value)}
            placeholder="1"
          />
        </label>

        <label>
          Entreprise ID
          <input
            value={contratEntrepriseFilter}
            onChange={(event) => setContratEntrepriseFilter(event.target.value)}
            placeholder="Optional"
          />
        </label>

        <label>
          Search
          <input
            value={contratSearchFilter}
            onChange={(event) => setContratSearchFilter(event.target.value)}
            placeholder="Numero contrat or notes"
          />
        </label>

        <label>
          Statut
          <select
            value={contratStatutFilter}
            onChange={(event) => setContratStatutFilter(event.target.value as ContratFilterStatut)}
          >
            <option value="ALL">ALL</option>
            <option value="DRAFT">DRAFT</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="SUSPENDED">SUSPENDED</option>
            <option value="TERMINATED">TERMINATED</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
        </label>

        <label>
          Sort
          <select
            value={contratSort}
            onChange={(event) => setContratSort(event.target.value as ContratSortOption)}
          >
            <option value="creeLe,desc">Newest</option>
            <option value="numeroContrat,asc">Numero contrat A-Z</option>
            <option value="dateDebut,desc">Date debut recent first</option>
          </select>
        </label>

        <button type="button" onClick={() => void loadContrats(0)} disabled={contratState === 'loading'}>
          {contratState === 'loading' ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      <p className="resultText">{contratTotal} contrat(s)</p>
      {contratError && <p className="errorText">{contratError}</p>}
      <div className="paginationRow">
        <button
          type="button"
          className="secondary"
          onClick={() => void loadContrats(Math.max(contratPage - 1, 0))}
          disabled={contratPage <= 0 || contratState === 'loading'}
        >
          Previous page
        </button>
        <span>
          Page {contratTotalPages === 0 ? 0 : contratPage + 1} / {Math.max(contratTotalPages, 1)}
        </span>
        <button
          type="button"
          className="secondary"
          onClick={() => void loadContrats(contratPage + 1)}
          disabled={contratPage + 1 >= contratTotalPages || contratState === 'loading'}
        >
          Next page
        </button>
      </div>

      <div className="workspaceGrid">
        <form className="form" onSubmit={(event) => void handleContratSubmit(event)}>
          <h3>{editingContratId === null ? 'Create contrat' : 'Edit contrat'}</h3>

          <label>
            Societe ID
            <input
              value={contratForm.societeId}
              onChange={(event) =>
                setContratForm((current) => ({
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
              value={contratForm.entrepriseId}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  entrepriseId: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Numero contrat
            <input
              value={contratForm.numeroContrat}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  numeroContrat: event.target.value,
                }))
              }
              required
              maxLength={60}
            />
          </label>

          <label>
            Date debut
            <input
              type="date"
              value={contratForm.dateDebut}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  dateDebut: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Date fin
            <input
              type="date"
              value={contratForm.dateFin}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  dateFin: event.target.value,
                }))
              }
            />
          </label>

          <label>
            Code devise
            <input
              value={contratForm.codeDevise}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  codeDevise: event.target.value.toUpperCase(),
                }))
              }
              maxLength={3}
            />
          </label>

          <label>
            Delai paiement (jours)
            <input
              value={contratForm.delaiPaiementJours}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  delaiPaiementJours: event.target.value,
                }))
              }
            />
          </label>

          <label>
            Montant max mensuel
            <input
              value={contratForm.montantMaxMensuel}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  montantMaxMensuel: event.target.value,
                }))
              }
              placeholder="Ex: 15000"
            />
          </label>

          <label>
            Statut
            <select
              value={contratForm.statut}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  statut: event.target.value as ContratStatut,
                }))
              }
            >
              <option value="DRAFT">DRAFT</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
              <option value="TERMINATED">TERMINATED</option>
              <option value="EXPIRED">EXPIRED</option>
            </select>
          </label>

          <label>
            Signe le
            <input
              type="date"
              value={contratForm.signeLe}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  signeLe: event.target.value,
                }))
              }
            />
          </label>

          <label>
            Notes
            <textarea
              value={contratForm.notes}
              onChange={(event) =>
                setContratForm((current) => ({
                  ...current,
                  notes: event.target.value,
                }))
              }
              rows={3}
            />
          </label>

          <div className="actionRow">
            <button type="submit" disabled={isContratSubmitting}>
              {isContratSubmitting ? 'Saving...' : editingContratId === null ? 'Create contrat' : 'Save changes'}
            </button>

            {editingContratId !== null && (
              <button type="button" className="secondary" onClick={resetContratForm}>
                Cancel edit
              </button>
            )}
          </div>

          {contratFormError && <p className="errorText">{contratFormError}</p>}
        </form>

        <div className="tableWrap">
          <table className="dataTable">
            <thead>
              <tr>
                <th>Numero</th>
                <th>Societe</th>
                <th>Entreprise</th>
                <th>Date debut</th>
                <th>Date fin</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {contrats.length === 0 ? (
                <tr>
                  <td colSpan={7} className="emptyCell">No contrat found for current filters.</td>
                </tr>
              ) : (
                contrats.map((contrat) => (
                  <tr key={contrat.id}>
                    <td>{contrat.numeroContrat}</td>
                    <td>{contrat.societeId}</td>
                    <td>{contrat.entrepriseId}</td>
                    <td>{contrat.dateDebut}</td>
                    <td>{contrat.dateFin || 'Open'}</td>
                    <td>
                      <span className={`badge ${contrat.statut === 'ACTIVE' ? 'ok' : 'error'}`}>{contrat.statut}</span>
                    </td>
                    <td>
                      <div className="compactActions">
                        <button type="button" className="secondary" onClick={() => startEditContrat(contrat)}>Edit</button>
                        <button
                          type="button"
                          className="warn"
                          onClick={() => void handleContratStatut(contrat.id, 'SUSPENDED')}
                          disabled={contrat.statut === 'SUSPENDED'}
                        >
                          Suspend
                        </button>
                        <button
                          type="button"
                          className="okButton"
                          onClick={() => void handleContratStatut(contrat.id, 'ACTIVE')}
                          disabled={contrat.statut === 'ACTIVE'}
                        >
                          Activate
                        </button>
                        <button
                          type="button"
                          className="secondary"
                          onClick={() => void handleContratStatut(contrat.id, 'TERMINATED')}
                          disabled={contrat.statut === 'TERMINATED'}
                        >
                          Terminate
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
