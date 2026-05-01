import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  clearToken,
  createContrat,
  createEmploye,
  createEntreprise,
  createVehicule,
  fetchContrats,
  fetchEmployes,
  fetchEntreprises,
  fetchHealth,
  fetchMe,
  fetchVehicules,
  getStoredToken,
  login,
  register,
  storeToken,
  updateContrat,
  updateContratStatut,
  updateEntreprise,
  updateEntrepriseStatut,
  updateEmploye,
  updateEmployeActif,
  updateVehicule,
  updateVehiculeActif,
  type Contrat,
  type ContratPayload,
  type ContratStatut,
  type Employe,
  type EmployePayload,
  type Entreprise,
  type EntreprisePayload,
  type EntrepriseStatut,
  type HealthResponse,
  type MeResponse,
  type Vehicule,
  type VehiculePayload,
  type VehiculeTypeCarburant,
} from './api'
import { AuthScreen } from './components/AuthScreen'
import { DashboardHome } from './components/DashboardHome'
import { ModulePanel } from './components/ModulePanel'
import { ToastStack } from './components/ToastStack'
import { UserSessionCard } from './components/UserSessionCard'
import { ContratsSection } from './components/sections/ContratsSection'
import { EmployesSection } from './components/sections/EmployesSection'
import { EntreprisesSection } from './components/sections/EntreprisesSection'
import { VehiculesSection } from './components/sections/VehiculesSection'

type AuthMode = 'login' | 'register'
type LoadState = 'idle' | 'loading' | 'success' | 'error'
type EntrepriseFilterStatut = 'ALL' | EntrepriseStatut
type ContratFilterStatut = 'ALL' | ContratStatut
type VehiculeFilterActif = 'ALL' | 'ACTIVE' | 'INACTIVE'
type EmployeFilterActif = 'ALL' | 'ACTIVE' | 'INACTIVE'
type EntrepriseSortOption = 'creeLe,desc' | 'raisonSociale,asc' | 'codeEntreprise,asc'
type ContratSortOption = 'creeLe,desc' | 'numeroContrat,asc' | 'dateDebut,desc'
type EmployeSortOption = 'creeLe,desc' | 'nomComplet,asc' | 'codeEmploye,asc'
type VehiculeSortOption = 'creeLe,desc' | 'immatriculation,asc' | 'marque,asc'
type ReferentialModule = 'dashboard' | 'entreprises' | 'contrats' | 'employes' | 'vehicules'

type ToastItem = {
  id: number
  type: 'success' | 'error'
  message: string
}

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

function buildInitialEmployeForm(): EmployeFormState {
  return {
    societeId: '1',
    entrepriseId: '',
    codeEmploye: '',
    nomComplet: '',
    cin: '',
    telephone: '',
    email: '',
    poste: '',
    actif: true,
  }
}

function buildInitialContratForm(): ContratFormState {
  return {
    societeId: '1',
    entrepriseId: '',
    numeroContrat: '',
    dateDebut: '',
    dateFin: '',
    codeDevise: 'TND',
    delaiPaiementJours: '30',
    montantMaxMensuel: '',
    statut: 'ACTIVE',
    signeLe: '',
    notes: '',
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

function parseOptionalNonNegativeInteger(value: string, fieldName: string): number | null {
  const normalized = value.trim()
  if (normalized.length === 0) {
    return null
  }

  const parsed = Number(normalized)
  if (!Number.isInteger(parsed) || parsed < 0) {
    throw new Error(`${fieldName} must be a non-negative integer`)
  }

  return parsed
}

function parseOptionalNonNegativeDecimal(value: string, fieldName: string): number | null {
  const normalized = value.trim()
  if (normalized.length === 0) {
    return null
  }

  const parsed = Number(normalized)
  if (Number.isNaN(parsed) || parsed < 0) {
    throw new Error(`${fieldName} must be a non-negative number`)
  }

  return parsed
}

function parseRequiredDate(value: string, fieldName: string): string {
  const normalized = value.trim()
  if (normalized.length === 0) {
    throw new Error(`${fieldName} is required`)
  }
  return normalized
}

function parseOptionalDate(value: string): string | null {
  const normalized = value.trim()
  return normalized.length === 0 ? null : normalized
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

function toEmployePayload(form: EmployeFormState): EmployePayload {
  return {
    societeId: parseRequiredSocieteId(form.societeId),
    entrepriseId: parseRequiredPositiveInteger(form.entrepriseId, 'Entreprise ID'),
    codeEmploye: form.codeEmploye.trim(),
    nomComplet: form.nomComplet.trim(),
    cin: normalizeOptionalValue(form.cin),
    telephone: normalizeOptionalValue(form.telephone),
    email: normalizeOptionalValue(form.email),
    poste: normalizeOptionalValue(form.poste),
    actif: form.actif,
  }
}

function toContratPayload(form: ContratFormState): ContratPayload {
  return {
    societeId: parseRequiredSocieteId(form.societeId),
    entrepriseId: parseRequiredPositiveInteger(form.entrepriseId, 'Entreprise ID'),
    numeroContrat: form.numeroContrat.trim(),
    dateDebut: parseRequiredDate(form.dateDebut, 'Date debut'),
    dateFin: parseOptionalDate(form.dateFin),
    codeDevise: normalizeOptionalValue(form.codeDevise)?.toUpperCase() ?? null,
    delaiPaiementJours: parseOptionalNonNegativeInteger(form.delaiPaiementJours, 'Delai paiement'),
    montantMaxMensuel: parseOptionalNonNegativeDecimal(form.montantMaxMensuel, 'Montant max mensuel'),
    statut: form.statut,
    signeLe: parseOptionalDate(form.signeLe),
    notes: normalizeOptionalValue(form.notes),
  }
}

function parseContratStatutFilter(value: ContratFilterStatut): ContratStatut | undefined {
  if (value === 'ALL') {
    return undefined
  }

  return value
}

function parseActifFilter(value: VehiculeFilterActif): boolean | undefined {
  if (value === 'ALL') {
    return undefined
  }

  return value === 'ACTIVE'
}

function parseEmployeActifFilter(value: EmployeFilterActif): boolean | undefined {
  if (value === 'ALL') {
    return undefined
  }

  return value === 'ACTIVE'
}

export default function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [activeModule, setActiveModule] = useState<ReferentialModule>('dashboard')
  const [healthState, setHealthState] = useState<LoadState>('idle')
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [currentUser, setCurrentUser] = useState<MeResponse | null>(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [toasts, setToasts] = useState<ToastItem[]>([])

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

  const [contrats, setContrats] = useState<Contrat[]>([])
  const [contratState, setContratState] = useState<LoadState>('idle')
  const [contratError, setContratError] = useState('')
  const [contratFormError, setContratFormError] = useState('')
  const [contratTotal, setContratTotal] = useState(0)
  const [contratPage, setContratPage] = useState(0)
  const [contratTotalPages, setContratTotalPages] = useState(0)
  const [contratSocieteFilter, setContratSocieteFilter] = useState('1')
  const [contratEntrepriseFilter, setContratEntrepriseFilter] = useState('')
  const [contratSearchFilter, setContratSearchFilter] = useState('')
  const [contratStatutFilter, setContratStatutFilter] = useState<ContratFilterStatut>('ALL')
  const [contratSort, setContratSort] = useState<ContratSortOption>('creeLe,desc')
  const [editingContratId, setEditingContratId] = useState<number | null>(null)
  const [contratForm, setContratForm] = useState<ContratFormState>(buildInitialContratForm)
  const [isContratSubmitting, setIsContratSubmitting] = useState(false)

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

  const [employes, setEmployes] = useState<Employe[]>([])
  const [employeState, setEmployeState] = useState<LoadState>('idle')
  const [employeError, setEmployeError] = useState('')
  const [employeFormError, setEmployeFormError] = useState('')
  const [employeTotal, setEmployeTotal] = useState(0)
  const [employePage, setEmployePage] = useState(0)
  const [employeTotalPages, setEmployeTotalPages] = useState(0)
  const [employeSocieteFilter, setEmployeSocieteFilter] = useState('1')
  const [employeEntrepriseFilter, setEmployeEntrepriseFilter] = useState('')
  const [employeSearchFilter, setEmployeSearchFilter] = useState('')
  const [employeActifFilter, setEmployeActifFilter] = useState<EmployeFilterActif>('ALL')
  const [employeSort, setEmployeSort] = useState<EmployeSortOption>('creeLe,desc')
  const [editingEmployeId, setEditingEmployeId] = useState<number | null>(null)
  const [employeForm, setEmployeForm] = useState<EmployeFormState>(buildInitialEmployeForm)
  const [isEmployeSubmitting, setIsEmployeSubmitting] = useState(false)

  const employesForVehiculeEntreprise = useMemo(() => {
    let entrepriseId: number | undefined
    let societeId: number | undefined

    try {
      entrepriseId = parseOptionalPositiveInteger(vehiculeForm.entrepriseId, 'Entreprise ID')
      societeId = parseOptionalSocieteId(vehiculeForm.societeId)
    } catch {
      return []
    }

    if (entrepriseId === undefined) {
      return []
    }

    return employes.filter((employe) => {
      const entrepriseMatch = employe.entrepriseId === entrepriseId
      const societeMatch = societeId === undefined || employe.societeId === societeId
      return entrepriseMatch && societeMatch && employe.actif
    })
  }, [employes, vehiculeForm.entrepriseId, vehiculeForm.societeId])

  const canManageReferential = useMemo(() => {
    if (!currentUser) {
      return false
    }
    return currentUser.role === 'ADMIN' || currentUser.role === 'MANAGER'
  }, [currentUser])

  function dismissToast(id: number) {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }

  function pushToast(type: 'success' | 'error', message: string) {
    const id = Date.now() + Math.floor(Math.random() * 1000)
    setToasts((current) => [...current, { id, type, message }])

    window.setTimeout(() => {
      dismissToast(id)
    }, 3200)
  }

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
      void loadContrats(0)
      void loadEmployes(0)
      void loadVehicules(0)
    } else {
      setActiveModule('dashboard')
      setEntreprises([])
      setEntrepriseTotal(0)
      setEntrepriseTotalPages(0)
      setContrats([])
      setContratTotal(0)
      setContratTotalPages(0)
      setEmployes([])
      setEmployeTotal(0)
      setEmployeTotalPages(0)
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
      const wasLogin = authMode === 'login'
      if (authMode === 'login') {
        const authResult = await login({ email, password })
        storeToken(authResult.token)
      } else {
        const authResult = await register({ name, email, password })
        storeToken(authResult.token)
      }

      await loadCurrentUser()
      setActiveModule('dashboard')
      setPassword('')
      pushToast('success', wasLogin ? 'Login successful' : 'Account created successfully')
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Authentication failed')
      pushToast('error', error instanceof Error ? error.message : 'Authentication failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleLogout() {
    clearToken()
    setActiveModule('dashboard')
    setCurrentUser(null)
    setEntreprises([])
    setEntrepriseTotal(0)
    setEntreprisePage(0)
    setEntrepriseTotalPages(0)
    setEditingEntrepriseId(null)
    setEntrepriseForm(buildInitialEntrepriseForm())
    setContrats([])
    setContratTotal(0)
    setContratPage(0)
    setContratTotalPages(0)
    setEditingContratId(null)
    setContratForm(buildInitialContratForm())
    setVehicules([])
    setVehiculeTotal(0)
    setVehiculePage(0)
    setVehiculeTotalPages(0)
    setEditingVehiculeId(null)
    setVehiculeForm(buildInitialVehiculeForm())
    setEmployes([])
    setEmployeTotal(0)
    setEmployePage(0)
    setEmployeTotalPages(0)
    setEditingEmployeId(null)
    setEmployeForm(buildInitialEmployeForm())
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

  async function loadContrats(targetPage = contratPage) {
    if (!canManageReferential) {
      return
    }

    setContratState('loading')
    setContratError('')

    try {
      const result = await fetchContrats({
        societeId: parseOptionalSocieteId(contratSocieteFilter),
        entrepriseId: parseOptionalPositiveInteger(contratEntrepriseFilter, 'Entreprise ID'),
        statut: parseContratStatutFilter(contratStatutFilter),
        search: contratSearchFilter.trim() || undefined,
        page: targetPage,
        size: PAGE_SIZE,
        sort: contratSort,
      })

      setContrats(result.content)
      setContratTotal(result.totalElements)
      setContratPage(result.number)
      setContratTotalPages(result.totalPages)
      setContratState('success')
    } catch (error) {
      setContrats([])
      setContratTotal(0)
      setContratTotalPages(0)
      setContratState('error')
      setContratError(error instanceof Error ? error.message : 'Could not load contrats')
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

  async function loadEmployes(targetPage = employePage) {
    if (!canManageReferential) {
      return
    }

    setEmployeState('loading')
    setEmployeError('')

    try {
      const result = await fetchEmployes({
        societeId: parseOptionalSocieteId(employeSocieteFilter),
        entrepriseId: parseOptionalPositiveInteger(employeEntrepriseFilter, 'Entreprise ID'),
        actif: parseEmployeActifFilter(employeActifFilter),
        search: employeSearchFilter.trim() || undefined,
        page: targetPage,
        size: PAGE_SIZE,
        sort: employeSort,
      })

      setEmployes(result.content)
      setEmployeTotal(result.totalElements)
      setEmployePage(result.number)
      setEmployeTotalPages(result.totalPages)
      setEmployeState('success')
    } catch (error) {
      setEmployes([])
      setEmployeTotal(0)
      setEmployeTotalPages(0)
      setEmployeState('error')
      setEmployeError(error instanceof Error ? error.message : 'Could not load employes')
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

  function startEditContrat(contrat: Contrat) {
    setEditingContratId(contrat.id)
    setContratFormError('')
    setContratForm({
      societeId: String(contrat.societeId),
      entrepriseId: String(contrat.entrepriseId),
      numeroContrat: contrat.numeroContrat,
      dateDebut: contrat.dateDebut,
      dateFin: contrat.dateFin ?? '',
      codeDevise: contrat.codeDevise,
      delaiPaiementJours: String(contrat.delaiPaiementJours),
      montantMaxMensuel: contrat.montantMaxMensuel === null ? '' : String(contrat.montantMaxMensuel),
      statut: contrat.statut,
      signeLe: contrat.signeLe ?? '',
      notes: contrat.notes ?? '',
    })
  }

  function resetContratForm() {
    setEditingContratId(null)
    setContratFormError('')
    setContratForm(buildInitialContratForm())
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

  function startEditEmploye(employe: Employe) {
    setEditingEmployeId(employe.id)
    setEmployeFormError('')
    setEmployeForm({
      societeId: String(employe.societeId),
      entrepriseId: String(employe.entrepriseId),
      codeEmploye: employe.codeEmploye,
      nomComplet: employe.nomComplet,
      cin: employe.cin ?? '',
      telephone: employe.telephone ?? '',
      email: employe.email ?? '',
      poste: employe.poste ?? '',
      actif: employe.actif,
    })
  }

  function resetEmployeForm() {
    setEditingEmployeId(null)
    setEmployeFormError('')
    setEmployeForm(buildInitialEmployeForm())
  }

  async function handleEntrepriseSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setEntrepriseFormError('')
    setIsEntrepriseSubmitting(true)

    try {
      const isCreate = editingEntrepriseId === null
      const payload = toEntreprisePayload(entrepriseForm)

      if (editingEntrepriseId === null) {
        await createEntreprise(payload)
      } else {
        await updateEntreprise(editingEntrepriseId, payload)
      }

      await loadEntreprises(editingEntrepriseId === null ? 0 : entreprisePage)
      resetEntrepriseForm()
      pushToast('success', isCreate ? 'Entreprise created' : 'Entreprise updated')
    } catch (error) {
      setEntrepriseFormError(error instanceof Error ? error.message : 'Could not save entreprise')
      pushToast('error', error instanceof Error ? error.message : 'Could not save entreprise')
    } finally {
      setIsEntrepriseSubmitting(false)
    }
  }

  async function handleEntrepriseStatut(id: number, statut: EntrepriseStatut) {
    try {
      await updateEntrepriseStatut(id, statut)
      await loadEntreprises(entreprisePage)
      pushToast('success', `Entreprise status set to ${statut}`)
    } catch (error) {
      setEntrepriseError(error instanceof Error ? error.message : 'Could not change entreprise status')
      pushToast('error', error instanceof Error ? error.message : 'Could not change entreprise status')
    }
  }

  async function handleContratSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setContratFormError('')
    setIsContratSubmitting(true)

    try {
      const isCreate = editingContratId === null
      const payload = toContratPayload(contratForm)

      if (editingContratId === null) {
        await createContrat(payload)
      } else {
        await updateContrat(editingContratId, payload)
      }

      await loadContrats(editingContratId === null ? 0 : contratPage)
      resetContratForm()
      pushToast('success', isCreate ? 'Contrat created' : 'Contrat updated')
    } catch (error) {
      setContratFormError(error instanceof Error ? error.message : 'Could not save contrat')
      pushToast('error', error instanceof Error ? error.message : 'Could not save contrat')
    } finally {
      setIsContratSubmitting(false)
    }
  }

  async function handleContratStatut(id: number, statut: ContratStatut) {
    try {
      await updateContratStatut(id, statut)
      await loadContrats(contratPage)
      pushToast('success', `Contrat status set to ${statut}`)
    } catch (error) {
      setContratError(error instanceof Error ? error.message : 'Could not change contrat status')
      pushToast('error', error instanceof Error ? error.message : 'Could not change contrat status')
    }
  }

  async function handleVehiculeSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setVehiculeFormError('')
    setIsVehiculeSubmitting(true)

    try {
      const isCreate = editingVehiculeId === null
      const payload = toVehiculePayload(vehiculeForm)

      if (editingVehiculeId === null) {
        await createVehicule(payload)
      } else {
        await updateVehicule(editingVehiculeId, payload)
      }

      await loadVehicules(editingVehiculeId === null ? 0 : vehiculePage)
      resetVehiculeForm()
      pushToast('success', isCreate ? 'Vehicule created' : 'Vehicule updated')
    } catch (error) {
      setVehiculeFormError(error instanceof Error ? error.message : 'Could not save vehicule')
      pushToast('error', error instanceof Error ? error.message : 'Could not save vehicule')
    } finally {
      setIsVehiculeSubmitting(false)
    }
  }

  async function handleVehiculeActif(id: number, actif: boolean) {
    try {
      await updateVehiculeActif(id, actif)
      await loadVehicules(vehiculePage)
      pushToast('success', `Vehicule ${actif ? 'activated' : 'deactivated'}`)
    } catch (error) {
      setVehiculeError(error instanceof Error ? error.message : 'Could not change vehicule status')
      pushToast('error', error instanceof Error ? error.message : 'Could not change vehicule status')
    }
  }

  async function handleEmployeSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setEmployeFormError('')
    setIsEmployeSubmitting(true)

    try {
      const isCreate = editingEmployeId === null
      const payload = toEmployePayload(employeForm)

      if (editingEmployeId === null) {
        await createEmploye(payload)
      } else {
        await updateEmploye(editingEmployeId, payload)
      }

      await loadEmployes(editingEmployeId === null ? 0 : employePage)
      resetEmployeForm()
      pushToast('success', isCreate ? 'Employe created' : 'Employe updated')
    } catch (error) {
      setEmployeFormError(error instanceof Error ? error.message : 'Could not save employe')
      pushToast('error', error instanceof Error ? error.message : 'Could not save employe')
    } finally {
      setIsEmployeSubmitting(false)
    }
  }

  async function handleEmployeActif(id: number, actif: boolean) {
    try {
      await updateEmployeActif(id, actif)
      await loadEmployes(employePage)
      await loadVehicules(vehiculePage)
      pushToast('success', `Employe ${actif ? 'activated' : 'deactivated'}`)
    } catch (error) {
      setEmployeError(error instanceof Error ? error.message : 'Could not change employe status')
      pushToast('error', error instanceof Error ? error.message : 'Could not change employe status')
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
            <UserSessionCard
              name={currentUser.name}
              email={currentUser.email}
              role={currentUser.role}
              onSignOut={handleLogout}
            />

            {canManageReferential && (
              <ModulePanel
                activeModule={activeModule}
                onChangeModule={setActiveModule}
                totals={{
                  entreprises: entrepriseTotal,
                  contrats: contratTotal,
                  employes: employeTotal,
                  vehicules: vehiculeTotal,
                }}
              />
            )}

            {activeModule === 'dashboard' && (
              <DashboardHome
                userName={currentUser.name}
                userRole={currentUser.role}
                canManageReferential={canManageReferential}
                totals={{
                  entreprises: entrepriseTotal,
                  contrats: contratTotal,
                  employes: employeTotal,
                  vehicules: vehiculeTotal,
                }}
                onOpenModule={setActiveModule}
              />
            )}

            {canManageReferential ? (
              activeModule === 'entreprises' ? (
                <EntreprisesSection
                  societeFilter={societeFilter}
                  setSocieteFilter={setSocieteFilter}
                  searchFilter={searchFilter}
                  setSearchFilter={setSearchFilter}
                  statutFilter={statutFilter}
                  setStatutFilter={setStatutFilter}
                  entrepriseSort={entrepriseSort}
                  setEntrepriseSort={setEntrepriseSort}
                  entrepriseState={entrepriseState}
                  entrepriseTotal={entrepriseTotal}
                  entrepriseError={entrepriseError}
                  entreprisePage={entreprisePage}
                  entrepriseTotalPages={entrepriseTotalPages}
                  entreprises={entreprises}
                  loadEntreprises={loadEntreprises}
                  entrepriseForm={entrepriseForm}
                  setEntrepriseForm={setEntrepriseForm}
                  handleEntrepriseSubmit={handleEntrepriseSubmit}
                  editingEntrepriseId={editingEntrepriseId}
                  isEntrepriseSubmitting={isEntrepriseSubmitting}
                  resetEntrepriseForm={resetEntrepriseForm}
                  entrepriseFormError={entrepriseFormError}
                  startEditEntreprise={startEditEntreprise}
                  handleEntrepriseStatut={handleEntrepriseStatut}
                />
              ) : null
            ) : null}

            {canManageReferential && activeModule === 'employes' && (
              <EmployesSection
                employeSocieteFilter={employeSocieteFilter}
                setEmployeSocieteFilter={setEmployeSocieteFilter}
                employeEntrepriseFilter={employeEntrepriseFilter}
                setEmployeEntrepriseFilter={setEmployeEntrepriseFilter}
                employeSearchFilter={employeSearchFilter}
                setEmployeSearchFilter={setEmployeSearchFilter}
                employeActifFilter={employeActifFilter}
                setEmployeActifFilter={setEmployeActifFilter}
                employeSort={employeSort}
                setEmployeSort={setEmployeSort}
                employeState={employeState}
                employeTotal={employeTotal}
                employeError={employeError}
                employePage={employePage}
                employeTotalPages={employeTotalPages}
                employes={employes}
                loadEmployes={loadEmployes}
                employeForm={employeForm}
                setEmployeForm={setEmployeForm}
                handleEmployeSubmit={handleEmployeSubmit}
                editingEmployeId={editingEmployeId}
                isEmployeSubmitting={isEmployeSubmitting}
                resetEmployeForm={resetEmployeForm}
                employeFormError={employeFormError}
                startEditEmploye={startEditEmploye}
                handleEmployeActif={handleEmployeActif}
              />
            )}

            {canManageReferential && activeModule === 'contrats' && (
              <ContratsSection
                contratSocieteFilter={contratSocieteFilter}
                setContratSocieteFilter={setContratSocieteFilter}
                contratEntrepriseFilter={contratEntrepriseFilter}
                setContratEntrepriseFilter={setContratEntrepriseFilter}
                contratSearchFilter={contratSearchFilter}
                setContratSearchFilter={setContratSearchFilter}
                contratStatutFilter={contratStatutFilter}
                setContratStatutFilter={setContratStatutFilter}
                contratSort={contratSort}
                setContratSort={setContratSort}
                contratState={contratState}
                contratTotal={contratTotal}
                contratError={contratError}
                contratPage={contratPage}
                contratTotalPages={contratTotalPages}
                contrats={contrats}
                loadContrats={loadContrats}
                contratForm={contratForm}
                setContratForm={setContratForm}
                handleContratSubmit={handleContratSubmit}
                editingContratId={editingContratId}
                isContratSubmitting={isContratSubmitting}
                resetContratForm={resetContratForm}
                contratFormError={contratFormError}
                startEditContrat={startEditContrat}
                handleContratStatut={handleContratStatut}
              />
            )}

            {canManageReferential && activeModule === 'vehicules' && (
              <VehiculesSection
                vehiculeSocieteFilter={vehiculeSocieteFilter}
                setVehiculeSocieteFilter={setVehiculeSocieteFilter}
                vehiculeEntrepriseFilter={vehiculeEntrepriseFilter}
                setVehiculeEntrepriseFilter={setVehiculeEntrepriseFilter}
                vehiculeSearchFilter={vehiculeSearchFilter}
                setVehiculeSearchFilter={setVehiculeSearchFilter}
                vehiculeActifFilter={vehiculeActifFilter}
                setVehiculeActifFilter={setVehiculeActifFilter}
                vehiculeSort={vehiculeSort}
                setVehiculeSort={setVehiculeSort}
                vehiculeState={vehiculeState}
                vehiculeTotal={vehiculeTotal}
                vehiculeError={vehiculeError}
                vehiculePage={vehiculePage}
                vehiculeTotalPages={vehiculeTotalPages}
                vehicules={vehicules}
                loadVehicules={loadVehicules}
                vehiculeForm={vehiculeForm}
                setVehiculeForm={setVehiculeForm}
                handleVehiculeSubmit={handleVehiculeSubmit}
                editingVehiculeId={editingVehiculeId}
                isVehiculeSubmitting={isVehiculeSubmitting}
                resetVehiculeForm={resetVehiculeForm}
                vehiculeFormError={vehiculeFormError}
                startEditVehicule={startEditVehicule}
                handleVehiculeActif={handleVehiculeActif}
                employesForVehiculeEntreprise={employesForVehiculeEntreprise}
              />
            )}
          </>
        ) : (
          <AuthScreen
            authMode={authMode}
            setAuthMode={setAuthMode}
            name={name}
            setName={setName}
            email={email}
            setEmail={setEmail}
            password={password}
            setPassword={setPassword}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            errorMessage={errorMessage}
            healthState={healthState}
            healthStatus={health?.status}
            onCheckBackendHealth={checkBackendHealth}
          />
        )}
        <ToastStack toasts={toasts} onDismiss={dismissToast} />
      </section>
    </main>
  )
}
