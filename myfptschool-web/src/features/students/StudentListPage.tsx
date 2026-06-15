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
import type { Student } from '@/shared/types/models'

export function StudentListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)

  const params = { search: search || undefined, page, size: 20 }
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.students.list(params),
    queryFn: () => apiGet<PageResponse<Student>>('/admin/students', params),
  })

  return (
    <div>
      <PageHeader
        title="Học sinh"
        actions={
          <Link to="/students/new">
            <Button><Plus size={16} /> Thêm học sinh</Button>
          </Link>
        }
      />

      <div className="bg-white rounded-xl border border-border-light shadow-sm">
        <div className="p-4 border-b border-border-light">
          <Input
            placeholder="Tìm theo tên, mã học sinh..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }}
            className="max-w-sm"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-surface-elevated">
              <tr>
                {['Mã HS', 'Họ tên', 'Lớp', 'Ngày sinh', 'SĐT', 'Trạng thái'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">
                    {h}
                  </th>
                ))}
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>
              )}
              {!isLoading && !data?.content.length && (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Không có dữ liệu</td></tr>
              )}
              {data?.content.map((s) => (
                <tr key={s.id} className="hover:bg-surface-bg transition-colors">
                  <td className="px-4 py-3 font-mono text-xs">{s.studentCode}</td>
                  <td className="px-4 py-3 font-medium">{s.fullName}</td>
                  <td className="px-4 py-3">{s.classroomName}</td>
                  <td className="px-4 py-3">{s.dateOfBirth}</td>
                  <td className="px-4 py-3">{s.phone ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${s.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {s.active ? 'Hoạt động' : 'Đã khóa'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <Link to={`/students/${s.id}/edit`} className="text-brand-blue hover:underline text-xs">
                      Sửa
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-border-light">
            <span className="text-xs text-text-secondary">
              {data.totalElements} học sinh · Trang {data.page + 1}/{data.totalPages}
            </span>
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
