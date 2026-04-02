import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  clearToken,
  createEntreprise,
  createVehicule,
  fetchEntreprises,
  fetchHealth,
  fetchMe,
  fetchVehicules,
  getStoredToken,
  login,
  register,
  storeToken,
  updateEntreprise,
  updateEntrepriseStatut,
  updateVehicule,
  updateVehiculeActif,
  type Entreprise,
  type EntreprisePayload,
  type EntrepriseStatut,
  type HealthResponse,
  type MeResponse,
  type Vehicule,
  type VehiculePayload,
  type VehiculeTypeCarburant,
} from './api'

type AuthMode = 'login' | 'register'
type LoadState = 'idle' | 'loading' | 'success' | 'error'
type EntrepriseFilterStatut = 'ALL' | EntrepriseStatut
type VehiculeFilterActif = 'ALL' | 'ACTIVE' | 'INACTIVE'
type EntrepriseSortOption = 'creeLe,desc' | 'raisonSociale,asc' | 'codeEntreprise,asc'
type VehiculeSortOption = 'creeLe,desc' | 'immatriculation,asc' | 'marque,asc'

const PAGE_SIZE = 10

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

function buildInitialEntrepriseForm(): EntrepriseFormState {
  return {
    societeId: '1',
    codeEntreprise: '',
    raisonSociale: '',
    nomCourt: '',
    matriculeFiscal: '',
    adresseFacturation: '',
    nomContact: '',
    emailContact: '',
    telephoneContact: '',
    statut: 'ACTIVE',
  }
}

function buildInitialVehiculeForm(): VehiculeFormState {
  return {
    societeId: '1',
    entrepriseId: '',
    employeId: '',
    immatriculation: '',
    codeFlotte: '',
    marque: '',
    modele: '',
    typeCarburant: 'DIESEL',
    capaciteReservoirLitres: '',
    actif: true,
  }
}

function normalizeOptionalValue(value: string): string | null {
  const normalized = value.trim()
  return normalized.length === 0 ? null : normalized
}

function parseOptionalSocieteId(value: string): number | undefined {
  const normalized = value.trim()
  if (normalized.length === 0) {
    return undefined
  }

  const parsed = Number(normalized)
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error('Societe ID must be a positive number')
  }

  return parsed
}

function parseOptionalPositiveInteger(value: string, fieldName: string): number | undefined {
  const normalized = value.trim()
  if (normalized.length === 0) {
    return undefined
  }

  const parsed = Number(normalized)
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error(`${fieldName} must be a positive number`)
  }

  return parsed
}

function parseRequiredSocieteId(value: string): number {
  const parsed = parseOptionalPositiveInteger(value, 'Societe ID')
  if (parsed === undefined) {
    throw new Error('Societe ID is required')
  }
  return parsed
}

function parseRequiredPositiveInteger(value: string, fieldName: string): number {
  const parsed = parseOptionalPositiveInteger(value, fieldName)
  if (parsed === undefined) {
    throw new Error(`${fieldName} is required`)
  }
  return parsed
}

function parseOptionalPositiveDecimal(value: string, fieldName: string): number | null {
  const normalized = value.trim()
  if (normalized.length === 0) {
    return null
  }

  const parsed = Number(normalized)
  if (Number.isNaN(parsed) || parsed <= 0) {
    throw new Error(`${fieldName} must be a positive number`)
  }

  return parsed
}

function toEntreprisePayload(form: EntrepriseFormState): EntreprisePayload {
  return {
    societeId: parseRequiredSocieteId(form.societeId),
    codeEntreprise: form.codeEntreprise.trim(),
    raisonSociale: form.raisonSociale.trim(),
    nomCourt: normalizeOptionalValue(form.nomCourt),
    matriculeFiscal: normalizeOptionalValue(form.matriculeFiscal),
    adresseFacturation: normalizeOptionalValue(form.adresseFacturation),
    nomContact: normalizeOptionalValue(form.nomContact),
    emailContact: normalizeOptionalValue(form.emailContact),
    telephoneContact: normalizeOptionalValue(form.telephoneContact),
    statut: form.statut,
  }
}

function toVehiculePayload(form: VehiculeFormState): VehiculePayload {
  return {
    societeId: parseRequiredSocieteId(form.societeId),
    entrepriseId: parseRequiredPositiveInteger(form.entrepriseId, 'Entreprise ID'),
    employeId: parseOptionalPositiveInteger(form.employeId, 'Employe ID') ?? null,
    immatriculation: form.immatriculation.trim(),
    codeFlotte: normalizeOptionalValue(form.codeFlotte),
    marque: normalizeOptionalValue(form.marque),
    modele: normalizeOptionalValue(form.modele),
    typeCarburant: form.typeCarburant,
    capaciteReservoirLitres: parseOptionalPositiveDecimal(form.capaciteReservoirLitres, 'Reservoir capacity'),
    actif: form.actif,
  }
}

function parseActifFilter(value: VehiculeFilterActif): boolean | undefined {
  if (value === 'ALL') {
    return undefined
  }

  return value === 'ACTIVE'
}

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

  const [entreprises, setEntreprises] = useState<Entreprise[]>([])
  const [entrepriseState, setEntrepriseState] = useState<LoadState>('idle')
  const [entrepriseError, setEntrepriseError] = useState('')
  const [entrepriseFormError, setEntrepriseFormError] = useState('')
  const [entrepriseTotal, setEntrepriseTotal] = useState(0)
  const [entreprisePage, setEntreprisePage] = useState(0)
  const [entrepriseTotalPages, setEntrepriseTotalPages] = useState(0)
  const [societeFilter, setSocieteFilter] = useState('1')
  const [searchFilter, setSearchFilter] = useState('')
  const [statutFilter, setStatutFilter] = useState<EntrepriseFilterStatut>('ALL')
  const [entrepriseSort, setEntrepriseSort] = useState<EntrepriseSortOption>('creeLe,desc')
  const [editingEntrepriseId, setEditingEntrepriseId] = useState<number | null>(null)
  const [entrepriseForm, setEntrepriseForm] = useState<EntrepriseFormState>(buildInitialEntrepriseForm)
  const [isEntrepriseSubmitting, setIsEntrepriseSubmitting] = useState(false)

  const [vehicules, setVehicules] = useState<Vehicule[]>([])
  const [vehiculeState, setVehiculeState] = useState<LoadState>('idle')
  const [vehiculeError, setVehiculeError] = useState('')
  const [vehiculeFormError, setVehiculeFormError] = useState('')
  const [vehiculeTotal, setVehiculeTotal] = useState(0)
  const [vehiculePage, setVehiculePage] = useState(0)
  const [vehiculeTotalPages, setVehiculeTotalPages] = useState(0)
  const [vehiculeSocieteFilter, setVehiculeSocieteFilter] = useState('1')
  const [vehiculeEntrepriseFilter, setVehiculeEntrepriseFilter] = useState('')
  const [vehiculeSearchFilter, setVehiculeSearchFilter] = useState('')
  const [vehiculeActifFilter, setVehiculeActifFilter] = useState<VehiculeFilterActif>('ALL')
  const [vehiculeSort, setVehiculeSort] = useState<VehiculeSortOption>('creeLe,desc')
  const [editingVehiculeId, setEditingVehiculeId] = useState<number | null>(null)
  const [vehiculeForm, setVehiculeForm] = useState<VehiculeFormState>(buildInitialVehiculeForm)
  const [isVehiculeSubmitting, setIsVehiculeSubmitting] = useState(false)

  const canManageReferential = useMemo(() => {
    if (!currentUser) {
      return false
    }
    return currentUser.role === 'ADMIN' || currentUser.role === 'MANAGER'
  }, [currentUser])

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

  useEffect(() => {
    if (canManageReferential) {
      void loadEntreprises(0)
      void loadVehicules(0)
    } else {
      setEntreprises([])
      setEntrepriseTotal(0)
      setEntrepriseTotalPages(0)
      setVehicules([])
      setVehiculeTotal(0)
      setVehiculeTotalPages(0)
    }
  }, [canManageReferential])

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
    setEntreprises([])
    setEntrepriseTotal(0)
    setEntreprisePage(0)
    setEntrepriseTotalPages(0)
    setEditingEntrepriseId(null)
    setEntrepriseForm(buildInitialEntrepriseForm())
    setVehicules([])
    setVehiculeTotal(0)
    setVehiculePage(0)
    setVehiculeTotalPages(0)
    setEditingVehiculeId(null)
    setVehiculeForm(buildInitialVehiculeForm())
  }

  async function loadEntreprises(targetPage = entreprisePage) {
    if (!canManageReferential) {
      return
    }

    setEntrepriseState('loading')
    setEntrepriseError('')

    try {
      const result = await fetchEntreprises({
        societeId: parseOptionalSocieteId(societeFilter),
        statut: statutFilter === 'ALL' ? undefined : statutFilter,
        search: searchFilter.trim() || undefined,
        page: targetPage,
        size: PAGE_SIZE,
        sort: entrepriseSort,
      })

      setEntreprises(result.content)
      setEntrepriseTotal(result.totalElements)
      setEntreprisePage(result.number)
      setEntrepriseTotalPages(result.totalPages)
      setEntrepriseState('success')
    } catch (error) {
      setEntreprises([])
      setEntrepriseTotal(0)
      setEntrepriseTotalPages(0)
      setEntrepriseState('error')
      setEntrepriseError(error instanceof Error ? error.message : 'Could not load entreprises')
    }
  }

  async function loadVehicules(targetPage = vehiculePage) {
    if (!canManageReferential) {
      return
    }

    setVehiculeState('loading')
    setVehiculeError('')

    try {
      const result = await fetchVehicules({
        societeId: parseOptionalSocieteId(vehiculeSocieteFilter),
        entrepriseId: parseOptionalPositiveInteger(vehiculeEntrepriseFilter, 'Entreprise ID'),
        actif: parseActifFilter(vehiculeActifFilter),
        search: vehiculeSearchFilter.trim() || undefined,
        page: targetPage,
        size: PAGE_SIZE,
        sort: vehiculeSort,
      })

      setVehicules(result.content)
      setVehiculeTotal(result.totalElements)
      setVehiculePage(result.number)
      setVehiculeTotalPages(result.totalPages)
      setVehiculeState('success')
    } catch (error) {
      setVehicules([])
      setVehiculeTotal(0)
      setVehiculeTotalPages(0)
      setVehiculeState('error')
      setVehiculeError(error instanceof Error ? error.message : 'Could not load vehicules')
    }
  }

  function startEditEntreprise(entreprise: Entreprise) {
    setEditingEntrepriseId(entreprise.id)
    setEntrepriseFormError('')
    setEntrepriseForm({
      societeId: String(entreprise.societeId),
      codeEntreprise: entreprise.codeEntreprise,
      raisonSociale: entreprise.raisonSociale,
      nomCourt: entreprise.nomCourt ?? '',
      matriculeFiscal: entreprise.matriculeFiscal ?? '',
      adresseFacturation: entreprise.adresseFacturation ?? '',
      nomContact: entreprise.nomContact ?? '',
      emailContact: entreprise.emailContact ?? '',
      telephoneContact: entreprise.telephoneContact ?? '',
      statut: entreprise.statut,
    })
  }

  function resetEntrepriseForm() {
    setEditingEntrepriseId(null)
    setEntrepriseFormError('')
    setEntrepriseForm(buildInitialEntrepriseForm())
  }

  function startEditVehicule(vehicule: Vehicule) {
    setEditingVehiculeId(vehicule.id)
    setVehiculeFormError('')
    setVehiculeForm({
      societeId: String(vehicule.societeId),
      entrepriseId: String(vehicule.entrepriseId),
      employeId: vehicule.employeId ? String(vehicule.employeId) : '',
      immatriculation: vehicule.immatriculation,
      codeFlotte: vehicule.codeFlotte ?? '',
      marque: vehicule.marque ?? '',
      modele: vehicule.modele ?? '',
      typeCarburant: vehicule.typeCarburant,
      capaciteReservoirLitres: vehicule.capaciteReservoirLitres ? String(vehicule.capaciteReservoirLitres) : '',
      actif: vehicule.actif,
    })
  }

  function resetVehiculeForm() {
    setEditingVehiculeId(null)
    setVehiculeFormError('')
    setVehiculeForm(buildInitialVehiculeForm())
  }

  async function handleEntrepriseSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setEntrepriseFormError('')
    setIsEntrepriseSubmitting(true)

    try {
      const payload = toEntreprisePayload(entrepriseForm)

      if (editingEntrepriseId === null) {
        await createEntreprise(payload)
      } else {
        await updateEntreprise(editingEntrepriseId, payload)
      }

      await loadEntreprises(editingEntrepriseId === null ? 0 : entreprisePage)
      resetEntrepriseForm()
    } catch (error) {
      setEntrepriseFormError(error instanceof Error ? error.message : 'Could not save entreprise')
    } finally {
      setIsEntrepriseSubmitting(false)
    }
  }

  async function handleEntrepriseStatut(id: number, statut: EntrepriseStatut) {
    try {
      await updateEntrepriseStatut(id, statut)
      await loadEntreprises(entreprisePage)
    } catch (error) {
      setEntrepriseError(error instanceof Error ? error.message : 'Could not change entreprise status')
    }
  }

  async function handleVehiculeSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setVehiculeFormError('')
    setIsVehiculeSubmitting(true)

    try {
      const payload = toVehiculePayload(vehiculeForm)

      if (editingVehiculeId === null) {
        await createVehicule(payload)
      } else {
        await updateVehicule(editingVehiculeId, payload)
      }

      await loadVehicules(editingVehiculeId === null ? 0 : vehiculePage)
      resetVehiculeForm()
    } catch (error) {
      setVehiculeFormError(error instanceof Error ? error.message : 'Could not save vehicule')
    } finally {
      setIsVehiculeSubmitting(false)
    }
  }

  async function handleVehiculeActif(id: number, actif: boolean) {
    try {
      await updateVehiculeActif(id, actif)
      await loadVehicules(vehiculePage)
    } catch (error) {
      setVehiculeError(error instanceof Error ? error.message : 'Could not change vehicule status')
    }
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
          <>
            <div className="status ok">
              <p>Signed in as {currentUser.name}</p>
              <p>Email: {currentUser.email}</p>
              <p>Role: {currentUser.role}</p>
              <button type="button" onClick={handleLogout}>Sign out</button>
            </div>

            {canManageReferential ? (
              <section className="workspace">
                <h2>Entreprises contractees</h2>
                <p className="subtitle">Module referentiel: CRUD, pagination et tri.</p>

                <div className="filterRow">
                  <label>
                    Societe ID
                    <input
                      value={societeFilter}
                      onChange={(event) => setSocieteFilter(event.target.value)}
                      placeholder="1"
                    />
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

                  <button
                    type="button"
                    onClick={() => void loadEntreprises(0)}
                    disabled={entrepriseState === 'loading'}
                  >
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
                  <form className="form" onSubmit={handleEntrepriseSubmit}>
                    <h3>{editingEntrepriseId === null ? 'Create entreprise' : 'Edit entreprise'}</h3>

                    <label>
                      Societe ID
                      <input
                        value={entrepriseForm.societeId}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          societeId: event.target.value,
                        }))}
                        required
                      />
                    </label>

                    <label>
                      Code entreprise
                      <input
                        value={entrepriseForm.codeEntreprise}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          codeEntreprise: event.target.value,
                        }))}
                        required
                        maxLength={50}
                      />
                    </label>

                    <label>
                      Raison sociale
                      <input
                        value={entrepriseForm.raisonSociale}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          raisonSociale: event.target.value,
                        }))}
                        required
                        maxLength={200}
                      />
                    </label>

                    <label>
                      Nom court
                      <input
                        value={entrepriseForm.nomCourt}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          nomCourt: event.target.value,
                        }))}
                        maxLength={120}
                      />
                    </label>

                    <label>
                      Matricule fiscal
                      <input
                        value={entrepriseForm.matriculeFiscal}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          matriculeFiscal: event.target.value,
                        }))}
                        maxLength={80}
                      />
                    </label>

                    <label>
                      Adresse facturation
                      <textarea
                        value={entrepriseForm.adresseFacturation}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          adresseFacturation: event.target.value,
                        }))}
                        rows={3}
                      />
                    </label>

                    <label>
                      Nom contact
                      <input
                        value={entrepriseForm.nomContact}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          nomContact: event.target.value,
                        }))}
                        maxLength={120}
                      />
                    </label>

                    <label>
                      Email contact
                      <input
                        type="email"
                        value={entrepriseForm.emailContact}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          emailContact: event.target.value,
                        }))}
                        maxLength={160}
                      />
                    </label>

                    <label>
                      Telephone contact
                      <input
                        value={entrepriseForm.telephoneContact}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          telephoneContact: event.target.value,
                        }))}
                        maxLength={40}
                      />
                    </label>

                    <label>
                      Statut
                      <select
                        value={entrepriseForm.statut}
                        onChange={(event) => setEntrepriseForm((current) => ({
                          ...current,
                          statut: event.target.value as EntrepriseStatut,
                        }))}
                      >
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="SUSPENDED">SUSPENDED</option>
                        <option value="CLOSED">CLOSED</option>
                      </select>
                    </label>

                    <div className="actionRow">
                      <button type="submit" disabled={isEntrepriseSubmitting}>
                        {isEntrepriseSubmitting
                          ? 'Saving...'
                          : editingEntrepriseId === null
                            ? 'Create entreprise'
                            : 'Save changes'}
                      </button>

                      {editingEntrepriseId !== null && (
                        <button type="button" className="secondary" onClick={resetEntrepriseForm}>
                          Cancel edit
                        </button>
                      )}
                    </div>

                    {entrepriseFormError && <p className="errorText">{entrepriseFormError}</p>}
                  </form>

                  <div className="enterpriseList">
                    {entreprises.length === 0 ? (
                      <p className="subtitle">No entreprise found for current filters.</p>
                    ) : (
                      entreprises.map((entreprise) => (
                        <article key={entreprise.id} className="enterpriseItem">
                          <div className="enterpriseHead">
                            <h3>{entreprise.codeEntreprise} - {entreprise.raisonSociale}</h3>
                            <span className={`badge ${entreprise.statut === 'ACTIVE' ? 'ok' : 'error'}`}>
                              {entreprise.statut}
                            </span>
                          </div>

                          <p>Societe ID: {entreprise.societeId}</p>
                          <p>Contact: {entreprise.nomContact || 'N/A'} ({entreprise.emailContact || 'N/A'})</p>

                          <div className="actionRow">
                            <button type="button" className="secondary" onClick={() => startEditEntreprise(entreprise)}>
                              Edit
                            </button>
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
                        </article>
                      ))
                    )}
                  </div>
                </div>
              </section>
            ) : (
              <div className="status">
                <p>Your current role can use authentication endpoints.</p>
                <p>Entreprise and vehicule management is available for ADMIN and MANAGER.</p>
              </div>
            )}

            {canManageReferential && (
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

                  <button
                    type="button"
                    onClick={() => void loadVehicules(0)}
                    disabled={vehiculeState === 'loading'}
                  >
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
                  <form className="form" onSubmit={handleVehiculeSubmit}>
                    <h3>{editingVehiculeId === null ? 'Create vehicule' : 'Edit vehicule'}</h3>

                    <label>
                      Societe ID
                      <input
                        value={vehiculeForm.societeId}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          societeId: event.target.value,
                        }))}
                        required
                      />
                    </label>

                    <label>
                      Entreprise ID
                      <input
                        value={vehiculeForm.entrepriseId}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          entrepriseId: event.target.value,
                        }))}
                        required
                      />
                    </label>

                    <label>
                      Employe ID
                      <input
                        value={vehiculeForm.employeId}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          employeId: event.target.value,
                        }))}
                      />
                    </label>

                    <label>
                      Immatriculation
                      <input
                        value={vehiculeForm.immatriculation}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          immatriculation: event.target.value,
                        }))}
                        required
                        maxLength={40}
                      />
                    </label>

                    <label>
                      Code flotte
                      <input
                        value={vehiculeForm.codeFlotte}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          codeFlotte: event.target.value,
                        }))}
                        maxLength={50}
                      />
                    </label>

                    <label>
                      Marque
                      <input
                        value={vehiculeForm.marque}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          marque: event.target.value,
                        }))}
                        maxLength={60}
                      />
                    </label>

                    <label>
                      Modele
                      <input
                        value={vehiculeForm.modele}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          modele: event.target.value,
                        }))}
                        maxLength={60}
                      />
                    </label>

                    <label>
                      Type carburant
                      <select
                        value={vehiculeForm.typeCarburant}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          typeCarburant: event.target.value as VehiculeTypeCarburant,
                        }))}
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
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          capaciteReservoirLitres: event.target.value,
                        }))}
                        placeholder="Ex: 60"
                      />
                    </label>

                    <label className="checkboxLabel">
                      <input
                        type="checkbox"
                        checked={vehiculeForm.actif}
                        onChange={(event) => setVehiculeForm((current) => ({
                          ...current,
                          actif: event.target.checked,
                        }))}
                      />
                      Actif
                    </label>

                    <div className="actionRow">
                      <button type="submit" disabled={isVehiculeSubmitting}>
                        {isVehiculeSubmitting
                          ? 'Saving...'
                          : editingVehiculeId === null
                            ? 'Create vehicule'
                            : 'Save changes'}
                      </button>

                      {editingVehiculeId !== null && (
                        <button type="button" className="secondary" onClick={resetVehiculeForm}>
                          Cancel edit
                        </button>
                      )}
                    </div>

                    {vehiculeFormError && <p className="errorText">{vehiculeFormError}</p>}
                  </form>

                  <div className="enterpriseList">
                    {vehicules.length === 0 ? (
                      <p className="subtitle">No vehicule found for current filters.</p>
                    ) : (
                      vehicules.map((vehicule) => (
                        <article key={vehicule.id} className="enterpriseItem">
                          <div className="enterpriseHead">
                            <h3>{vehicule.immatriculation} - {vehicule.marque || 'N/A'} {vehicule.modele || ''}</h3>
                            <span className={`badge ${vehicule.actif ? 'ok' : 'error'}`}>
                              {vehicule.actif ? 'ACTIVE' : 'INACTIVE'}
                            </span>
                          </div>

                          <p>Societe ID: {vehicule.societeId} | Entreprise ID: {vehicule.entrepriseId}</p>
                          <p>Type carburant: {vehicule.typeCarburant} | Code flotte: {vehicule.codeFlotte || 'N/A'}</p>

                          <div className="actionRow">
                            <button type="button" className="secondary" onClick={() => startEditVehicule(vehicule)}>
                              Edit
                            </button>
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
                        </article>
                      ))
                    )}
                  </div>
                </div>
              </section>
            )}
          </>
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
