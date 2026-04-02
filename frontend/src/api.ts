const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const TOKEN_STORAGE_KEY = 'fuel_voucher_token'

export type HealthResponse = {
  status: string
  service: string
  timestamp: string
}

export type AuthResponse = {
  token: string
  tokenType: string
  userId: number
  name: string
  email: string
  role: string
}

export type MeResponse = {
  userId: number
  name: string
  email: string
  role: string
}

export type EntrepriseStatut = 'ACTIVE' | 'SUSPENDED' | 'CLOSED'

export type Entreprise = {
  id: number
  societeId: number
  codeEntreprise: string
  raisonSociale: string
  nomCourt: string | null
  matriculeFiscal: string | null
  adresseFacturation: string | null
  nomContact: string | null
  emailContact: string | null
  telephoneContact: string | null
  statut: EntrepriseStatut
  creeLe: string
  modifieLe: string
}

export type PageResponse<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type EntreprisePayload = {
  societeId: number
  codeEntreprise: string
  raisonSociale: string
  nomCourt?: string | null
  matriculeFiscal?: string | null
  adresseFacturation?: string | null
  nomContact?: string | null
  emailContact?: string | null
  telephoneContact?: string | null
  statut?: EntrepriseStatut
}

export type EntrepriseQuery = {
  societeId?: number
  statut?: EntrepriseStatut
  search?: string
  page?: number
  size?: number
  sort?: string
}

export type VehiculeTypeCarburant = 'GASOLINE' | 'DIESEL' | 'GPL' | 'ELECTRIC' | 'OTHER'

export type Vehicule = {
  id: number
  societeId: number
  entrepriseId: number
  employeId: number | null
  immatriculation: string
  codeFlotte: string | null
  marque: string | null
  modele: string | null
  typeCarburant: VehiculeTypeCarburant
  capaciteReservoirLitres: number | null
  actif: boolean
  creeLe: string
  modifieLe: string
}

export type VehiculePayload = {
  societeId: number
  entrepriseId: number
  employeId?: number | null
  immatriculation: string
  codeFlotte?: string | null
  marque?: string | null
  modele?: string | null
  typeCarburant: VehiculeTypeCarburant
  capaciteReservoirLitres?: number | null
  actif?: boolean
}

export type VehiculeQuery = {
  societeId?: number
  entrepriseId?: number
  actif?: boolean
  search?: string
  page?: number
  size?: number
  sort?: string
}

export type Employe = {
  id: number
  societeId: number
  entrepriseId: number
  codeEmploye: string
  nomComplet: string
  cin: string | null
  telephone: string | null
  email: string | null
  poste: string | null
  actif: boolean
  creeLe: string
  modifieLe: string
}

export type EmployePayload = {
  societeId: number
  entrepriseId: number
  codeEmploye: string
  nomComplet: string
  cin?: string | null
  telephone?: string | null
  email?: string | null
  poste?: string | null
  actif?: boolean
}

export type EmployeQuery = {
  societeId?: number
  entrepriseId?: number
  actif?: boolean
  search?: string
  page?: number
  size?: number
  sort?: string
}

export type ContratStatut = 'DRAFT' | 'ACTIVE' | 'SUSPENDED' | 'TERMINATED' | 'EXPIRED'

export type Contrat = {
  id: number
  societeId: number
  entrepriseId: number
  numeroContrat: string
  dateDebut: string
  dateFin: string | null
  codeDevise: string
  delaiPaiementJours: number
  montantMaxMensuel: number | null
  statut: ContratStatut
  signeLe: string | null
  notes: string | null
  creeParUtilisateurId: number | null
  creeLe: string
  modifieLe: string
}

export type ContratPayload = {
  societeId: number
  entrepriseId: number
  numeroContrat: string
  dateDebut: string
  dateFin?: string | null
  codeDevise?: string | null
  delaiPaiementJours?: number | null
  montantMaxMensuel?: number | null
  statut?: ContratStatut
  signeLe?: string | null
  notes?: string | null
}

export type ContratQuery = {
  societeId?: number
  entrepriseId?: number
  statut?: ContratStatut
  search?: string
  page?: number
  size?: number
  sort?: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type RegisterRequest = {
  name: string
  email: string
  password: string
}

function buildHeaders(includeAuth: boolean): HeadersInit {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  }

  if (includeAuth) {
    const token = getStoredToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  return headers
}

function buildQueryString(query: Record<string, string | number | boolean | undefined>): string {
  const searchParams = new URLSearchParams()

  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined) {
      searchParams.set(key, String(value))
    }
  })

  const built = searchParams.toString()
  return built ? `?${built}` : ''
}

export async function fetchHealth(): Promise<HealthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/health`)
  if (!response.ok) {
    throw new Error(`Backend request failed: ${response.status}`)
  }

  return response.json() as Promise<HealthResponse>
}

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: buildHeaders(false),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Invalid email or password')
  }

  return response.json() as Promise<AuthResponse>
}

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: buildHeaders(false),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create account')
  }

  return response.json() as Promise<AuthResponse>
}

export async function fetchMe(): Promise<MeResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Unauthorized')
  }

  return response.json() as Promise<MeResponse>
}

export async function fetchEntreprises(query: EntrepriseQuery = {}): Promise<PageResponse<Entreprise>> {
  const queryString = buildQueryString({
    societeId: query.societeId,
    statut: query.statut,
    search: query.search,
    page: query.page,
    size: query.size,
    sort: query.sort,
  })

  const response = await fetch(`${API_BASE_URL}/api/entreprises${queryString}`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Could not load entreprises')
  }

  return response.json() as Promise<PageResponse<Entreprise>>
}

export async function createEntreprise(payload: EntreprisePayload): Promise<Entreprise> {
  const response = await fetch(`${API_BASE_URL}/api/entreprises`, {
    method: 'POST',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create entreprise')
  }

  return response.json() as Promise<Entreprise>
}

export async function updateEntreprise(id: number, payload: EntreprisePayload): Promise<Entreprise> {
  const response = await fetch(`${API_BASE_URL}/api/entreprises/${id}`, {
    method: 'PUT',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not update entreprise')
  }

  return response.json() as Promise<Entreprise>
}

export async function updateEntrepriseStatut(id: number, statut: EntrepriseStatut): Promise<Entreprise> {
  const response = await fetch(`${API_BASE_URL}/api/entreprises/${id}/statut`, {
    method: 'PATCH',
    headers: buildHeaders(true),
    body: JSON.stringify({ statut }),
  })

  if (!response.ok) {
    throw new Error('Could not update entreprise status')
  }

  return response.json() as Promise<Entreprise>
}

export async function fetchVehicules(query: VehiculeQuery = {}): Promise<PageResponse<Vehicule>> {
  const queryString = buildQueryString({
    societeId: query.societeId,
    entrepriseId: query.entrepriseId,
    actif: query.actif,
    search: query.search,
    page: query.page,
    size: query.size,
    sort: query.sort,
  })

  const response = await fetch(`${API_BASE_URL}/api/vehicules${queryString}`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Could not load vehicules')
  }

  return response.json() as Promise<PageResponse<Vehicule>>
}

export async function createVehicule(payload: VehiculePayload): Promise<Vehicule> {
  const response = await fetch(`${API_BASE_URL}/api/vehicules`, {
    method: 'POST',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create vehicule')
  }

  return response.json() as Promise<Vehicule>
}

export async function updateVehicule(id: number, payload: VehiculePayload): Promise<Vehicule> {
  const response = await fetch(`${API_BASE_URL}/api/vehicules/${id}`, {
    method: 'PUT',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not update vehicule')
  }

  return response.json() as Promise<Vehicule>
}

export async function updateVehiculeActif(id: number, actif: boolean): Promise<Vehicule> {
  const response = await fetch(`${API_BASE_URL}/api/vehicules/${id}/actif`, {
    method: 'PATCH',
    headers: buildHeaders(true),
    body: JSON.stringify({ actif }),
  })

  if (!response.ok) {
    throw new Error('Could not update vehicule status')
  }

  return response.json() as Promise<Vehicule>
}

export async function fetchEmployes(query: EmployeQuery = {}): Promise<PageResponse<Employe>> {
  const queryString = buildQueryString({
    societeId: query.societeId,
    entrepriseId: query.entrepriseId,
    actif: query.actif,
    search: query.search,
    page: query.page,
    size: query.size,
    sort: query.sort,
  })

  const response = await fetch(`${API_BASE_URL}/api/employes${queryString}`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Could not load employes')
  }

  return response.json() as Promise<PageResponse<Employe>>
}

export async function createEmploye(payload: EmployePayload): Promise<Employe> {
  const response = await fetch(`${API_BASE_URL}/api/employes`, {
    method: 'POST',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create employe')
  }

  return response.json() as Promise<Employe>
}

export async function updateEmploye(id: number, payload: EmployePayload): Promise<Employe> {
  const response = await fetch(`${API_BASE_URL}/api/employes/${id}`, {
    method: 'PUT',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not update employe')
  }

  return response.json() as Promise<Employe>
}

export async function updateEmployeActif(id: number, actif: boolean): Promise<Employe> {
  const response = await fetch(`${API_BASE_URL}/api/employes/${id}/actif`, {
    method: 'PATCH',
    headers: buildHeaders(true),
    body: JSON.stringify({ actif }),
  })

  if (!response.ok) {
    throw new Error('Could not update employe status')
  }

  return response.json() as Promise<Employe>
}

export async function fetchContrats(query: ContratQuery = {}): Promise<PageResponse<Contrat>> {
  const queryString = buildQueryString({
    societeId: query.societeId,
    entrepriseId: query.entrepriseId,
    statut: query.statut,
    search: query.search,
    page: query.page,
    size: query.size,
    sort: query.sort,
  })

  const response = await fetch(`${API_BASE_URL}/api/contrats${queryString}`, {
    headers: buildHeaders(true),
  })

  if (!response.ok) {
    throw new Error('Could not load contrats')
  }

  return response.json() as Promise<PageResponse<Contrat>>
}

export async function createContrat(payload: ContratPayload): Promise<Contrat> {
  const response = await fetch(`${API_BASE_URL}/api/contrats`, {
    method: 'POST',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not create contrat')
  }

  return response.json() as Promise<Contrat>
}

export async function updateContrat(id: number, payload: ContratPayload): Promise<Contrat> {
  const response = await fetch(`${API_BASE_URL}/api/contrats/${id}`, {
    method: 'PUT',
    headers: buildHeaders(true),
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Could not update contrat')
  }

  return response.json() as Promise<Contrat>
}

export async function updateContratStatut(id: number, statut: ContratStatut): Promise<Contrat> {
  const response = await fetch(`${API_BASE_URL}/api/contrats/${id}/statut`, {
    method: 'PATCH',
    headers: buildHeaders(true),
    body: JSON.stringify({ statut }),
  })

  if (!response.ok) {
    throw new Error('Could not update contrat status')
  }

  return response.json() as Promise<Contrat>
}

export function getStoredToken(): string | null {
  return window.localStorage.getItem(TOKEN_STORAGE_KEY)
}

export function storeToken(token: string): void {
  window.localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_STORAGE_KEY)
}
