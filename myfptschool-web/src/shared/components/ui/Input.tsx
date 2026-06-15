import { cn } from '@/shared/lib/utils'
import { type InputHTMLAttributes, forwardRef } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string
  label?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, label, id, ...props }, ref) => (
    <div className="flex flex-col gap-1">
      {label && (
        <label htmlFor={id} className="text-sm font-medium text-text-primary">
          {label}
        </label>
      )}
      <input
        ref={ref}
        id={id}
        className={cn(
          'h-10 rounded-lg border px-3 text-sm outline-none transition-all',
          'border-border-light bg-white placeholder:text-text-tertiary',
          'focus:border-brand-blue focus:ring-2 focus:ring-blue-100',
          error && 'border-status-danger focus:ring-red-100',
          className
        )}
        {...props}
      />
      {error && <p className="text-xs text-status-danger">{error}</p>}
    </div>
  )
)
Input.displayName = 'Input'
