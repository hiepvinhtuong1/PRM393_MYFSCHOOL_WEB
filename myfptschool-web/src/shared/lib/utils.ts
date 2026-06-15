import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(iso: string) {
  const [y, m, d] = iso.split('-')
  return `${d}/${m}/${y}`
}

export function formatDateTime(iso: string) {
  const dt = new Date(iso)
  return dt.toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' })
}
