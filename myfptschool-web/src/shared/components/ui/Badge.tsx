import { cn } from '@/shared/lib/utils'

type Variant = 'present' | 'late' | 'excused' | 'absent' | 'success' | 'warning' | 'danger' | 'info' | 'default'

const variantClass: Record<Variant, string> = {
  present: 'bg-green-100 text-green-800',
  late: 'bg-amber-100 text-amber-800',
  excused: 'bg-blue-100 text-blue-800',
  absent: 'bg-red-100 text-red-800',
  success: 'bg-green-100 text-green-800',
  warning: 'bg-amber-100 text-amber-800',
  danger: 'bg-red-100 text-red-800',
  info: 'bg-blue-100 text-blue-800',
  default: 'bg-surface-elevated text-text-secondary',
}

interface BadgeProps {
  variant?: Variant
  children: React.ReactNode
  className?: string
}

export function Badge({ variant = 'default', children, className }: BadgeProps) {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium', variantClass[variant], className)}>
      {children}
    </span>
  )
}
