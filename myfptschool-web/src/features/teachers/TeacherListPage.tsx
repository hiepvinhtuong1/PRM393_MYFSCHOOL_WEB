import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { apiGet, apiPatch } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Button } from '@/shared/components/ui/Button'
import type { PageResponse } from '@/shared/types/api'
import type { Teacher } from '@/shared/types/models'

export function TeacherListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const params = { search: search || undefined, page, size: 20 }
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.teachers.list(params),
    queryFn: () => apiGet<PageResponse<Teacher>>('/admin/teachers', params),
  })

  const toggleMutation = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) =>
      apiPatch<void>(`/admin/teachers/${id}/status`, { active }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.teachers.list(params) }),
  })

  function handleToggle(t: Teacher) {
    const action = t.active ? 'khóa' : 'mở khóa'
    if (!confirm(`Bạn có chắc muốn ${action} tài khoản của ${t.fullName}?`)) return
    toggleMutation.mutate({ id: t.id, active: !t.active })
  }

  return (
    <div>
      <PageHeader
        title="Giáo viên"
        actions={
          <Link to="/teachers/new">
            <Button><Plus size={16} /> Thêm giáo viên</Button>
          </Link>
        }
      />
      <div className="bg-white rounded-xl border border-border-light shadow-sm">
        <div className="p-4 border-b border-border-light">
          <Input
            placeholder="Tìm theo tên..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }}
            className="max-w-sm"
          />
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-surface-elevated">
              <tr>
                {['Họ tên', 'SĐT', 'Email', 'Cơ sở', 'Tài khoản', 'Trạng thái', ''].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
              {!isLoading && !data?.content.length && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Không có dữ liệu</td></tr>}
              {data?.content.map((t) => (
                <tr key={t.id} className="hover:bg-surface-bg transition-colors">
                  <td className="px-4 py-3 font-medium">{t.fullName}</td>
                  <td className="px-4 py-3">{t.phone ?? '—'}</td>
                  <td className="px-4 py-3">{t.email ?? '—'}</td>
                  <td className="px-4 py-3">{t.campusName ?? '—'}</td>
                  <td className="px-4 py-3 font-mono text-xs">{t.username ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${t.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {t.active ? 'Hoạt động' : 'Đã khóa'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <Link to={`/teachers/${t.id}/edit`} className="text-brand-blue hover:underline text-xs">
                        Sửa
                      </Link>
                      <button
                        onClick={() => handleToggle(t)}
                        disabled={toggleMutation.isPending}
                        className={`text-xs hover:underline disabled:opacity-50 ${t.active ? 'text-status-danger' : 'text-green-700'}`}
                      >
                        {t.active ? 'Khóa' : 'Mở khóa'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-border-light">
            <span className="text-xs text-text-secondary">{data.totalElements} giáo viên</span>
            <div className="flex gap-2">
              <button className="text-xs px-3 py-1 border rounded disabled:opacity-40" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</button>
              <button className="text-xs px-3 py-1 border rounded disabled:opacity-40" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Sau →</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
