type ToastItem = {
  id: number
  type: 'success' | 'error'
  message: string
}

type Props = {
  toasts: ToastItem[]
  onDismiss: (id: number) => void
}

export function ToastStack({ toasts, onDismiss }: Props) {
  return (
    <div className="toastStack" aria-live="polite" aria-atomic="false">
      {toasts.map((toast) => (
        <div key={toast.id} className={toast.type === 'success' ? 'toast success' : 'toast error'}>
          <span>{toast.message}</span>
          <button type="button" className="toastClose" onClick={() => onDismiss(toast.id)}>
            x
          </button>
        </div>
      ))}
    </div>
  )
}
