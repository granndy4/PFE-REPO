type UserSessionCardProps = {
  name: string
  email: string
  role: string
  onSignOut: () => void
}

export function UserSessionCard({ name, email, role, onSignOut }: UserSessionCardProps) {
  return (
    <div className="status ok">
      <p>Signed in as {name}</p>
      <p>Email: {email}</p>
      <p>Role: {role}</p>
      <button type="button" onClick={onSignOut}>Sign out</button>
    </div>
  )
}
