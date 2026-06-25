import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { jobApi, deliveryApi, resumeApi } from '../../api'
import { getUser } from '../../utils/request'
import type { Job, Resume } from '../../types'
import { MapPin, Clock, DollarSign, Send, MessageCircle } from 'lucide-react'

export default function JobDetailPage() {
  const { id } = useParams<{ id: string }>()
  const nav = useNavigate()
  const user = getUser()
  const [job, setJob] = useState<Job | null>(null)
  const [resumes, setResumes] = useState<Resume[]>([])
  const [selectedResume, setSelectedResume] = useState<number | null>(null)
  const [applying, setApplying] = useState(false)
  const [msg, setMsg] = useState('')

  useEffect(() => {
    jobApi.detail(Number(id)).then(setJob)
    if (user?.role === 'JOB_SEEKER') {
      resumeApi.list().then(setResumes)
    }
  }, [id])

  const handleApply = async () => {
    if (!selectedResume) { setMsg('请选择一份简历'); return }
    setApplying(true)
    setMsg('')
    try {
      await deliveryApi.apply(Number(id), selectedResume)
      setMsg('投递成功！')
    } catch (err: any) {
      setMsg(err.message)
    } finally {
      setApplying(false)
    }
  }

  if (!job) return <div className="text-center py-20 text-gray-400">加载中...</div>

  return (
    <div className="max-w-3xl mx-auto">
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h1 className="text-2xl font-bold">{job.title}</h1>
        <div className="flex flex-wrap items-center gap-4 mt-3 text-gray-600">
          <span className="flex items-center gap-1"><DollarSign size={16} />{job.salaryRange}</span>
          <span className="flex items-center gap-1"><MapPin size={16} />{job.location}</span>
          <span className="flex items-center gap-1"><Clock size={16} />{job.jobType}</span>
          {user?.role === 'JOB_SEEKER' && (
            <button onClick={() => nav(`/chat/${job.recruiterId}`)}
              className="flex items-center gap-1.5 px-4 py-1.5 bg-green-600 text-white rounded-full text-sm hover:bg-green-700 transition-colors ml-auto">
              <MessageCircle size={15} />联系HR
            </button>
          )}
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 className="text-lg font-semibold mb-3">岗位描述</h2>
        <p className="text-gray-700 whitespace-pre-wrap">{job.description}</p>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 className="text-lg font-semibold mb-3">任职要求</h2>
        <p className="text-gray-700 whitespace-pre-wrap">{job.requirements}</p>
      </div>

      {user?.role === 'JOB_SEEKER' && (
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold mb-3">投递简历</h2>
          {resumes.length === 0 ? (
            <p className="text-gray-400 text-sm">
              还没有简历，
              <button onClick={() => nav('/resume')} className="text-blue-600 hover:underline">去上传</button>
            </p>
          ) : (
            <div className="space-y-3">
              <select
                value={selectedResume || ''}
                onChange={e => setSelectedResume(Number(e.target.value))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">选择简历</option>
                {resumes.map(r => (
                  <option key={r.id} value={r.id}>{r.fileName}</option>
                ))}
              </select>
              <button onClick={handleApply} disabled={applying}
                className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
                <Send size={16} />{applying ? '投递中...' : '投递简历'}
              </button>
              {msg && <p className={`text-sm ${msg.includes('成功') ? 'text-green-600' : 'text-red-600'}`}>{msg}</p>}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
