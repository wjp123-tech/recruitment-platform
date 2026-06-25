import { useState, useEffect } from 'react'
import { notifApi } from '../../api'
import type { Notification } from '../../types'
import { formatDate } from '../../utils/date'
import { Bell, CheckCheck } from 'lucide-react'

const TYPE_MAP: Record<string, string> = {
  DELIVERY_NEW: '新投递',
  DELIVERY_STATUS: '投递状态更新',
  INTERVIEW_COMPLETE: '面试完成',
  EXAM_GRADED: '笔试批改完成',
  CHAT_NEW_MSG: '新消息',
}

export default function NotificationPage() {
  const [notifs, setNotifs] = useState<Notification[]>([])

  useEffect(() => {
    notifApi.list().then(r => setNotifs(r.content))
  }, [])

  const handleMarkRead = async (id: number) => {
    await notifApi.markRead(id)
    setNotifs(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n))
  }

  const handleMarkAll = async () => {
    await notifApi.markAllRead()
    setNotifs(prev => prev.map(n => ({ ...n, isRead: true })))
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold">消息通知</h2>
        <button onClick={handleMarkAll}
          className="flex items-center gap-1 text-sm text-blue-600 hover:underline">
          <CheckCheck size={16} />全部已读
        </button>
      </div>

      {notifs.length === 0 ? (
        <div className="text-center text-gray-400 py-20">
          <Bell size={48} className="mx-auto mb-4 text-gray-300" />
          暂无通知
        </div>
      ) : (
        <div className="space-y-2">
          {notifs.map(n => (
            <button key={n.id} onClick={() => handleMarkRead(n.id)}
              className={`w-full text-left bg-white rounded-lg shadow-sm p-4 transition-colors hover:shadow ${
                !n.isRead ? 'border-l-4 border-blue-500' : ''
              }`}>
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">{n.title}</span>
                <span className="text-xs text-gray-400">{formatDate(n.createdAt)}</span>
              </div>
              <p className="text-sm text-gray-600 mt-1">{n.content}</p>
              <span className="text-xs text-gray-400 mt-1 inline-block">
                {TYPE_MAP[n.type] || n.type}
              </span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
