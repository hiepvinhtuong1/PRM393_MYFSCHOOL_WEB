import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import type { PageResponse } from '@/shared/types/api'
import type { Teacher } from '@/shared/types/models'

export function TeacherListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const params = { search: search || undefined, page, size: 20 }

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.teachers.list(params),
    queryFn: () => apiGet<PageResponse<Teacher>>('/admin/teachers', params),
  })

  return (
    <div>
      <PageHeader title="Giáo viên" />
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
                {['Họ tên', 'SĐT', 'Email', 'Cơ sở', 'Tài khoản', 'Trạng thái'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
              {!isLoading && !data?.content.length && <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Không có dữ liệu</td></tr>}
              {data?.content.map((t) => (
                <tr key={t.id} className="hover:bg-surface-bg transition-colors">
                  <td className="px-4 py-3 font-medium">{t.fullName}</td>
                  <td className="px-4 py-3">{t.phone ?? '—'}</td>
                  <td className="px-4 py-3">{t.email}</td>
                  <td className="px-4 py-3">{t.campusName}</td>
                  <td className="px-4 py-3 font-mono text-xs">{t.username}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${t.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {t.active ? 'Hoạt động' : 'Đã khóa'}
                    </span>
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
