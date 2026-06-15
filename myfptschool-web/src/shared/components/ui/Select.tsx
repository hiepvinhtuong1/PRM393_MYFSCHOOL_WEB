import { cn } from '@/shared/lib/utils'
import { type SelectHTMLAttributes, forwardRef } from 'react'

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  error?: string
  label?: string
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, error, label, id, children, ...props }, ref) => (
    <div className="flex flex-col gap-1">
      {label && (
        <label htmlFor={id} className="text-sm font-medium text-text-primary">
          {label}
        </label>
      )}
      <select
        ref={ref}
        id={id}
        className={cn(
          'h-10 rounded-lg border px-3 text-sm outline-none transition-all bg-white',
          'border-border-light',
          'focus:border-brand-blue focus:ring-2 focus:ring-blue-100',
          error && 'border-status-danger',
          className
        )}
        {...props}
      >
        {children}
      </select>
      {error && <p className="text-xs text-status-danger">{error}</p>}
    </div>
  )
)
Select.displayName = 'Select'
