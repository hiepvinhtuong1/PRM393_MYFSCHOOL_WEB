import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'
import { Input } from '@/shared/components/ui/Input'
import type { PageResponse } from '@/shared/types/api'
import type { Parent } from '@/shared/types/models'

export function ParentListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const params = { search: search || undefined, page, size: 20 }

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.parents.list(params),
    queryFn: () => apiGet<PageResponse<Parent>>('/admin/parents', params),
  })

  return (
    <div>
      <PageHeader
        title="Phụ huynh"
        actions={
          <Link to="/parents/new">
            <Button><Plus size={16} /> Thêm phụ huynh</Button>
          </Link>
        }
      />

      <div className="bg-white rounded-xl border border-border-light shadow-sm">
        <div className="p-4 border-b border-border-light">
          <Input
            placeholder="Tìm theo tên phụ huynh..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }}
            className="max-w-sm"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-surface-elevated">
              <tr>
                {['CCCD/CMND', 'Họ tên', 'Giới tính', 'SĐT', 'Tài khoản', 'Con em', ''].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
              {!isLoading && !data?.content.length && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Không có dữ liệu</td></tr>}
              {data?.content.map((p) => (
                <tr key={p.id} className="hover:bg-surface-bg transition-colors">
                  <td className="px-4 py-3 font-mono text-xs">{p.parentCode}</td>
                  <td className="px-4 py-3 font-medium">{p.fullName}</td>
                  <td className="px-4 py-3">{p.gender ?? '—'}</td>
                  <td className="px-4 py-3">{p.phone ?? '—'}</td>
                  <td className="px-4 py-3 font-mono text-xs">{p.username ?? '—'}</td>
                  <td className="px-4 py-3">
                    {p.children.length > 0
                      ? p.children.map((c) => (
                          <span key={c.id} className="inline-block bg-surface-elevated rounded px-1.5 py-0.5 text-xs mr-1 mb-0.5">{c.fullName}</span>
                        ))
                      : <span className="text-text-tertiary text-xs">Chưa liên kết</span>
                    }
                  </td>
                  <td className="px-4 py-3">
                    <Link to={`/parents/${p.id}/edit`} className="text-brand-blue hover:underline text-xs">Sửa</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-border-light">
            <span className="text-xs text-text-secondary">{data.totalElements} phụ huynh · Trang {data.page + 1}/{data.totalPages}</span>
            <div className="flex gap-2">
              <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Trước</Button>
              <Button variant="secondary" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Sau →</Button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
