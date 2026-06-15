import { Link } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'

export function NotificationListPage() {
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
      <div className="bg-white rounded-xl border border-border-light p-8 text-center text-text-tertiary">
        Danh sách thông báo đã gửi sẽ hiển thị ở đây.
      </div>
    </div>
  )
}
