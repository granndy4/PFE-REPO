import type { Dispatch, FormEvent, SetStateAction } from 'react'
import type { Employe, Vehicule, VehiculeTypeCarburant } from '../../api'

type VehiculeFilterActif = 'ALL' | 'ACTIVE' | 'INACTIVE'
type VehiculeSortOption = 'creeLe,desc' | 'immatriculation,asc' | 'marque,asc'

type VehiculeFormState = {
  societeId: string
  entrepriseId: string
  employeId: string
  immatriculation: string
  codeFlotte: string
  marque: string
  modele: string
  typeCarburant: VehiculeTypeCarburant
  capaciteReservoirLitres: string
  actif: boolean
}

type Props = {
  vehiculeSocieteFilter: string
  setVehiculeSocieteFilter: (value: string) => void
  vehiculeEntrepriseFilter: string
  setVehiculeEntrepriseFilter: (value: string) => void
  vehiculeSearchFilter: string
  setVehiculeSearchFilter: (value: string) => void
  vehiculeActifFilter: VehiculeFilterActif
  setVehiculeActifFilter: (value: VehiculeFilterActif) => void
  vehiculeSort: VehiculeSortOption
  setVehiculeSort: (value: VehiculeSortOption) => void
  vehiculeState: 'idle' | 'loading' | 'success' | 'error'
  vehiculeTotal: number
  vehiculeError: string
  vehiculePage: number
  vehiculeTotalPages: number
  vehicules: Vehicule[]
  loadVehicules: (page: number) => Promise<void>
  vehiculeForm: VehiculeFormState
  setVehiculeForm: Dispatch<SetStateAction<VehiculeFormState>>
  handleVehiculeSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>
  editingVehiculeId: number | null
  isVehiculeSubmitting: boolean
  resetVehiculeForm: () => void
  vehiculeFormError: string
  startEditVehicule: (vehicule: Vehicule) => void
  handleVehiculeActif: (id: number, actif: boolean) => Promise<void>
  employesForVehiculeEntreprise: Employe[]
}

export function VehiculesSection(props: Props) {
  const {
    vehiculeSocieteFilter,
    setVehiculeSocieteFilter,
    vehiculeEntrepriseFilter,
    setVehiculeEntrepriseFilter,
    vehiculeSearchFilter,
    setVehiculeSearchFilter,
    vehiculeActifFilter,
    setVehiculeActifFilter,
    vehiculeSort,
    setVehiculeSort,
    vehiculeState,
    vehiculeTotal,
    vehiculeError,
    vehiculePage,
    vehiculeTotalPages,
    vehicules,
    loadVehicules,
    vehiculeForm,
    setVehiculeForm,
    handleVehiculeSubmit,
    editingVehiculeId,
    isVehiculeSubmitting,
    resetVehiculeForm,
    vehiculeFormError,
    startEditVehicule,
    handleVehiculeActif,
    employesForVehiculeEntreprise,
  } = props

  return (
    <section className="workspace">
      <h2>Vehicules</h2>
      <p className="subtitle">CRUD vehicules relies on entreprises_contractees and writes audit entries.</p>

      <div className="filterRow">
        <label>
          Societe ID
          <input
            value={vehiculeSocieteFilter}
            onChange={(event) => setVehiculeSocieteFilter(event.target.value)}
            placeholder="1"
          />
        </label>

        <label>
          Entreprise ID
          <input
            value={vehiculeEntrepriseFilter}
            onChange={(event) => setVehiculeEntrepriseFilter(event.target.value)}
            placeholder="Optional"
          />
        </label>

        <label>
          Search
          <input
            value={vehiculeSearchFilter}
            onChange={(event) => setVehiculeSearchFilter(event.target.value)}
            placeholder="Immatriculation or marque"
          />
        </label>

        <label>
          Actif
          <select
            value={vehiculeActifFilter}
            onChange={(event) => setVehiculeActifFilter(event.target.value as VehiculeFilterActif)}
          >
            <option value="ALL">ALL</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </label>

        <label>
          Sort
          <select
            value={vehiculeSort}
            onChange={(event) => setVehiculeSort(event.target.value as VehiculeSortOption)}
          >
            <option value="creeLe,desc">Newest</option>
            <option value="immatriculation,asc">Immatriculation A-Z</option>
            <option value="marque,asc">Marque A-Z</option>
          </select>
        </label>

        <button type="button" onClick={() => void loadVehicules(0)} disabled={vehiculeState === 'loading'}>
          {vehiculeState === 'loading' ? 'Loading...' : 'Refresh'}
        </button>
      </div>

      <p className="resultText">{vehiculeTotal} vehicule(s)</p>
      {vehiculeError && <p className="errorText">{vehiculeError}</p>}
      <div className="paginationRow">
        <button
          type="button"
          className="secondary"
          onClick={() => void loadVehicules(Math.max(vehiculePage - 1, 0))}
          disabled={vehiculePage <= 0 || vehiculeState === 'loading'}
        >
          Previous page
        </button>
        <span>
          Page {vehiculeTotalPages === 0 ? 0 : vehiculePage + 1} / {Math.max(vehiculeTotalPages, 1)}
        </span>
        <button
          type="button"
          className="secondary"
          onClick={() => void loadVehicules(vehiculePage + 1)}
          disabled={vehiculePage + 1 >= vehiculeTotalPages || vehiculeState === 'loading'}
        >
          Next page
        </button>
      </div>

      <div className="workspaceGrid">
        <form className="form" onSubmit={(event) => void handleVehiculeSubmit(event)}>
          <h3>{editingVehiculeId === null ? 'Create vehicule' : 'Edit vehicule'}</h3>

          <label>
            Societe ID
            <input
              value={vehiculeForm.societeId}
              onChange={(event) =>
                setVehiculeForm((current) => ({
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
              value={vehiculeForm.entrepriseId}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  entrepriseId: event.target.value,
                }))
              }
              required
            />
          </label>

          <label>
            Employe
            <select
              value={vehiculeForm.employeId}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  employeId: event.target.value,
                }))
              }
            >
              <option value="">Unassigned</option>
              {employesForVehiculeEntreprise.map((employe) => (
                <option key={employe.id} value={String(employe.id)}>
                  {employe.codeEmploye} - {employe.nomComplet}
                </option>
              ))}
            </select>
          </label>

          {vehiculeForm.entrepriseId.trim().length > 0 && employesForVehiculeEntreprise.length === 0 && (
            <p className="subtitle">No active employe available for selected entreprise.</p>
          )}

          <label>
            Immatriculation
            <input
              value={vehiculeForm.immatriculation}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  immatriculation: event.target.value,
                }))
              }
              required
              maxLength={40}
            />
          </label>

          <label>
            Code flotte
            <input
              value={vehiculeForm.codeFlotte}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  codeFlotte: event.target.value,
                }))
              }
              maxLength={50}
            />
          </label>

          <label>
            Marque
            <input
              value={vehiculeForm.marque}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  marque: event.target.value,
                }))
              }
              maxLength={60}
            />
          </label>

          <label>
            Modele
            <input
              value={vehiculeForm.modele}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  modele: event.target.value,
                }))
              }
              maxLength={60}
            />
          </label>

          <label>
            Type carburant
            <select
              value={vehiculeForm.typeCarburant}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  typeCarburant: event.target.value as VehiculeTypeCarburant,
                }))
              }
            >
              <option value="DIESEL">DIESEL</option>
              <option value="GASOLINE">GASOLINE</option>
              <option value="GPL">GPL</option>
              <option value="ELECTRIC">ELECTRIC</option>
              <option value="OTHER">OTHER</option>
            </select>
          </label>

          <label>
            Capacite reservoir (litres)
            <input
              value={vehiculeForm.capaciteReservoirLitres}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  capaciteReservoirLitres: event.target.value,
                }))
              }
              placeholder="Ex: 60"
            />
          </label>

          <label className="checkboxLabel">
            <input
              type="checkbox"
              checked={vehiculeForm.actif}
              onChange={(event) =>
                setVehiculeForm((current) => ({
                  ...current,
                  actif: event.target.checked,
                }))
              }
            />
            Actif
          </label>

          <div className="actionRow">
            <button type="submit" disabled={isVehiculeSubmitting}>
              {isVehiculeSubmitting ? 'Saving...' : editingVehiculeId === null ? 'Create vehicule' : 'Save changes'}
            </button>

            {editingVehiculeId !== null && (
              <button type="button" className="secondary" onClick={resetVehiculeForm}>
                Cancel edit
              </button>
            )}
          </div>

          {vehiculeFormError && <p className="errorText">{vehiculeFormError}</p>}
        </form>

        <div className="tableWrap">
          <table className="dataTable">
            <thead>
              <tr>
                <th>Immatriculation</th>
                <th>Societe</th>
                <th>Entreprise</th>
                <th>Employe</th>
                <th>Carburant</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {vehicules.length === 0 ? (
                <tr>
                  <td colSpan={7} className="emptyCell">No vehicule found for current filters.</td>
                </tr>
              ) : (
                vehicules.map((vehicule) => (
                  <tr key={vehicule.id}>
                    <td>{vehicule.immatriculation}</td>
                    <td>{vehicule.societeId}</td>
                    <td>{vehicule.entrepriseId}</td>
                    <td>{vehicule.employeId ?? 'Unassigned'}</td>
                    <td>{vehicule.typeCarburant}</td>
                    <td>
                      <span className={`badge ${vehicule.actif ? 'ok' : 'error'}`}>{vehicule.actif ? 'ACTIVE' : 'INACTIVE'}</span>
                    </td>
                    <td>
                      <div className="compactActions">
                        <button type="button" className="secondary" onClick={() => startEditVehicule(vehicule)}>Edit</button>
                        <button
                          type="button"
                          className="warn"
                          onClick={() => void handleVehiculeActif(vehicule.id, false)}
                          disabled={!vehicule.actif}
                        >
                          Deactivate
                        </button>
                        <button
                          type="button"
                          className="okButton"
                          onClick={() => void handleVehiculeActif(vehicule.id, true)}
                          disabled={vehicule.actif}
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
