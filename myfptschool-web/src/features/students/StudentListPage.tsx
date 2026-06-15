import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Download, Plus, Upload, X } from 'lucide-react'
import { apiDownload, apiGet, apiUpload } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'
import { Input } from '@/shared/components/ui/Input'
import type { PageResponse } from '@/shared/types/api'
import type { Student } from '@/shared/types/models'

interface ImportError { rowNum: number; column: string; reason: string }
interface ImportResult { success: boolean; studentsImported: number; parentsLinked: number; errors: ImportError[] }

export function StudentListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [importResult, setImportResult] = useState<ImportResult | null>(null)
  const [downloading, setDownloading] = useState(false)
  const fileRef = useRef<HTMLInputElement>(null)
  const qc = useQueryClient()

  const params = { search: search || undefined, page, size: 20 }
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.students.list(params),
    queryFn: () => apiGet<PageResponse<Student>>('/admin/students', params),
  })

  async function downloadTemplate() {
    setDownloading(true)
    try { await apiDownload('/admin/students/import/template', 'student_import_template.xlsx') }
    finally { setDownloading(false) }
  }

  const importMutation = useMutation({
    mutationFn: (file: File) => {
      const fd = new FormData()
      fd.append('file', file)
      return apiUpload<ImportResult>('/admin/students/import', fd)
    },
    onSuccess: (result) => {
      setImportResult(result)
      if (result.success) qc.invalidateQueries({ queryKey: ['students', 'list'] })
    },
  })

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (file) { importMutation.mutate(file); e.target.value = '' }
  }

  return (
    <div>
      <PageHeader
        title="Học sinh"
        actions={
          <div className="flex gap-2 flex-wrap">
            <Button variant="secondary" onClick={downloadTemplate} loading={downloading}>
              <Download size={16} /> Tải template
            </Button>
            <Button
              variant="secondary"
              onClick={() => fileRef.current?.click()}
              loading={importMutation.isPending}
            >
              <Upload size={16} /> Import Excel
            </Button>
            <input
              ref={fileRef}
              type="file"
              accept=".xlsx,.xls"
              className="hidden"
              onChange={handleFileChange}
            />
            <Link to="/students/new">
              <Button><Plus size={16} /> Thêm học sinh</Button>
            </Link>
          </div>
        }
      />

      {importResult && (
        <div className={`mb-4 rounded-xl border p-4 ${importResult.success ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1">
              {importResult.success ? (
                <p className="text-sm font-medium text-green-800">
                  Import thành công: {importResult.studentsImported} học sinh
                  {importResult.parentsLinked > 0 && `, ${importResult.parentsLinked} phụ huynh`}
                </p>
              ) : (
                <>
                  <p className="text-sm font-medium text-red-800 mb-2">
                    Import thất bại — {importResult.errors.length} lỗi:
                  </p>
                  <div className="overflow-x-auto">
                    <table className="text-xs text-red-700 border-collapse">
                      <thead>
                        <tr className="border-b border-red-200">
                          <th className="pr-4 py-1 text-left font-semibold">Hàng</th>
                          <th className="pr-4 py-1 text-left font-semibold">Cột</th>
                          <th className="py-1 text-left font-semibold">Lý do</th>
                        </tr>
                      </thead>
                      <tbody>
                        {importResult.errors.map((err, i) => (
                          <tr key={i} className="border-b border-red-100">
                            <td className="pr-4 py-1">{err.rowNum}</td>
                            <td className="pr-4 py-1">{err.column}</td>
                            <td className="py-1">{err.reason}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </div>
            <button
              onClick={() => setImportResult(null)}
              className="text-text-tertiary hover:text-text-primary shrink-0"
            >
              <X size={16} />
            </button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl border border-border-light shadow-sm">
        <div className="p-4 border-b border-border-light">
          <Input
            placeholder="Tìm theo tên học sinh..."
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
