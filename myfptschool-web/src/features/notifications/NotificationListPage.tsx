import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { apiGet, apiPut } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'
import { Badge } from '@/shared/components/ui/Badge'
import { formatDateTime } from '@/shared/lib/utils'
import type { Notification, SentNotification } from '@/shared/types/models'

interface NotificationPage {
  content: Notification[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

interface SentPage {
  content: SentNotification[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

const categoryLabel: Record<string, string> = {
  attendance: 'Điểm danh', grade: 'Điểm số', homeroom: 'Chủ nhiệm', study: 'Học tập', event: 'Sự kiện',
}
const categoryVariant: Record<string, 'info' | 'warning' | 'success' | 'default'> = {
  attendance: 'warning', grade: 'info', homeroom: 'success', study: 'default', event: 'info',
}
const targetTypeLabel: Record<string, string> = {
  individual: 'Cá nhân', classroom: 'Lớp học', all: 'Toàn trường',
}

export function NotificationListPage() {
  const [tab, setTab] = useState<'inbox' | 'sent'>('inbox')
  const [page, setPage] = useState(0)
  const qc = useQueryClient()
  const params = { page, size: 20 }

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.notifications.list(params),
    queryFn: () => apiGet<NotificationPage>('/me/notifications', params),
    enabled: tab === 'inbox',
  })

  const { data: sentData, isLoading: sentLoading } = useQuery({
    queryKey: queryKeys.notifications.sent(params),
    queryFn: () => apiGet<SentPage>('/admin/notifications', params),
    enabled: tab === 'sent',
  })

  const markRead = useMutation({
    mutationFn: (id: number) => apiPut(`/me/notifications/${id}/read`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.notifications.list({}) }),
  })

  function switchTab(t: 'inbox' | 'sent') {
    setTab(t)
    setPage(0)
  }

  return (
    <div>
      <PageHeader
        title="Thông báo"
        actions={
          <Link to="/notifications/new">
            <Button><Plus size={16} /> Soạn thông báo</Button>
          </Link>
        }
      />

      <div className="flex gap-1 mb-4 border-b border-border-light">
        {(['inbox', 'sent'] as const).map((t) => (
          <button
            key={t}
            onClick={() => switchTab(t)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${tab === t ? 'border-brand-orange text-brand-orange' : 'border-transparent text-text-secondary hover:text-text-primary'}`}
          >
            {t === 'inbox' ? 'Hộp thư đến' : 'Đã gửi'}
          </button>
        ))}
      </div>

      {tab === 'inbox' && (
        <>
          <div className="bg-white rounded-xl border border-border-light shadow-sm divide-y divide-border-light">
            {isLoading && <div className="px-4 py-8 text-center text-text-tertiary">Đang tải...</div>}
            {!isLoading && !data?.content.length && (
              <div className="px-4 py-8 text-center text-text-tertiary">Chưa có thông báo nào</div>
            )}
            {data?.content.map((n) => (
              <div
                key={n.id}
                className={`px-5 py-4 flex items-start gap-3 cursor-pointer hover:bg-surface-bg transition-colors ${!n.isRead ? 'bg-blue-50/40' : ''}`}
                onClick={() => !n.isRead && markRead.mutate(n.id)}
              >
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge variant={categoryVariant[n.category] ?? 'default'}>{categoryLabel[n.category] ?? n.category}</Badge>
                    {!n.isRead && <span className="w-2 h-2 rounded-full bg-brand-blue flex-shrink-0" />}
                  </div>
                  <p className={`text-sm font-medium ${n.isRead ? 'text-text-primary' : 'text-text-primary font-semibold'}`}>{n.title}</p>
                  <p className="text-sm text-text-secondary mt-0.5 line-clamp-2">{n.body}</p>
                  <p className="text-xs text-text-tertiary mt-1">{formatDateTime(n.createdAt)}</p>
                </div>
              </div>
            ))}
          </div>
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <span className="text-xs text-text-secondary">{data.totalElements} thông báo · Trang {data.page + 1}/{data.totalPages}</span>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</Button>
                <Button variant="secondary" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Sau →</Button>
              </div>
            </div>
          )}
        </>
      )}

      {tab === 'sent' && (
        <>
          <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-elevated">
                <tr>
                  {['Tiêu đề', 'Phân loại', 'Phạm vi', 'Người nhận', 'Thời gian', ''].map((h) => (
                    <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border-light">
                {sentLoading && <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
                {!sentLoading && !sentData?.content.length && (
                  <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Chưa gửi thông báo nào</td></tr>
                )}
                {sentData?.content.map((n) => (
                  <tr key={n.id} className="hover:bg-surface-bg">
                    <td className="px-4 py-3 font-medium">{n.title}</td>
                    <td className="px-4 py-3">
                      <Badge variant={categoryVariant[n.category] ?? 'default'}>{categoryLabel[n.category] ?? n.category}</Badge>
                    </td>
                    <td className="px-4 py-3 text-text-secondary">{targetTypeLabel[n.targetType] ?? n.targetType}</td>
                    <td className="px-4 py-3 text-text-secondary">{n.recipientCount} người</td>
                    <td className="px-4 py-3 text-text-secondary">{formatDateTime(n.createdAt)}</td>
                    <td className="px-4 py-3">
                      <span className="text-xs text-text-tertiary line-clamp-2 max-w-xs">{n.body}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {sentData && sentData.totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <span className="text-xs text-text-secondary">{sentData.totalElements} thông báo · Trang {sentData.number + 1}/{sentData.totalPages}</span>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</Button>
                <Button variant="secondary" size="sm" disabled={page >= sentData.totalPages - 1} onClick={() => setPage(p => p + 1)}>Sau →</Button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
