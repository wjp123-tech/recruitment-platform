import { useState, useEffect } from 'react'
import { deliveryApi, jobApi } from '../../api'
import { getUser } from '../../utils/request'
import type { Delivery } from '../../types'
import { formatDate } from '../../utils/date'
import { Briefcase } from 'lucide-react'

const STATUS_MAP: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待筛选', color: 'bg-yellow-50 text-yellow-700' },
  REVIEWED: { label: '已通过筛选', color: 'bg-blue-50 text-blue-700' },
  INTERVIEW: { label: '面试中', color: 'bg-purple-50 text-purple-700' },
  OFFER: { label: '已录用', color: 'bg-green-50 text-green-700' },
  REJECTED: { label: '未通过', color: 'bg-red-50 text-red-700' },
}

export default function DeliveryPage() {
  const user = getUser()
  const [deliveries, setDeliveries] = useState<Delivery[]>([])
  const [jobTitles, setJobTitles] = useState<Record<number, string>>({})

  useEffect(() => {
    const fetchDeliveries = user?.role === 'RECRUITER'
      ? jobApi.myJobs().then(r => {
          // For recruiters, merge all job deliveries
          return Promise.all(r.content.map(j =>
            deliveryApi.jobDeliveries(j.id)
          )).then(arrs => arrs.flat())
        })
      : deliveryApi.myDeliveries().then(r => r.content)

    fetchDeliveries.then(setDeliveries)
  }, [])

  useEffect(() => {
    const jobIds = [...new Set(deliveries.map(d => d.jobId))]
    jobIds.forEach(id => {
      if (!jobTitles[id]) {
        jobApi.detail(id).then(j => setJobTitles(p => ({ ...p, [id]: j.title }))).catch(() => {})
      }
    })
  }, [deliveries])

  return (
    <div>
      <h2 className="text-xl font-bold mb-6">
        {user?.role === 'RECRUITER' ? '收到的投递' : '我的投递'}
      </h2>

      {deliveries.length === 0 ? (
        <div className="text-center text-gray-400 py-20">
          <Briefcase size={48} className="mx-auto mb-4 text-gray-300" />
          暂无投递记录
        </div>
      ) : (
        <div className="space-y-3">
          {deliveries.map(d => {
            const s = STATUS_MAP[d.status] || STATUS_MAP.PENDING
            return (
              <div key={d.id} className="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
                <div>
                  <h4 className="font-medium">{jobTitles[d.jobId] || `岗位 #${d.jobId}`}</h4>
                  <p className="text-sm text-gray-400 mt-0.5">投递于 {formatDate(d.createdAt)}</p>
                  {d.remark && <p className="text-sm text-gray-500 mt-1">备注: {d.remark}</p>}
                </div>
                <span className={`text-xs px-3 py-1 rounded-full font-medium ${s.color}`}>
                  {s.label}
                </span>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
