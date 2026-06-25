import { useState, useEffect } from 'react'
import { jobApi, deliveryApi } from '../../api'
import type { Job, Delivery } from '../../types'
import { formatDate } from '../../utils/date'
import { Plus, Edit3, Trash2, Eye, FileText, X } from 'lucide-react'

export default function JobManagePage() {
  const [jobs, setJobs] = useState<Job[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Job | null>(null)
  const [deliveries, setDeliveries] = useState<Delivery[]>([])
  const [showDeliveries, setShowDeliveries] = useState<number | null>(null)
  const [filterStatus, setFilterStatus] = useState<string>('ALL')
  const [resumeView, setResumeView] = useState<{ fileName: string; parsedText: string; jobSeekerName: string; jobSeekerEmail: string; jobSeekerPhone: string } | null>(null)
  const [form, setForm] = useState({
    title: '', description: '', requirements: '', salaryRange: '', location: '', jobType: '全职',
  })

  const loadJobs = () => { jobApi.myJobs().then(r => setJobs(r.content)) }

  useEffect(() => { loadJobs() }, [])

  const reset = () => {
    setForm({ title: '', description: '', requirements: '', salaryRange: '', location: '', jobType: '全职' })
    setEditing(null)
    setShowForm(false)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (editing) {
      await jobApi.update(editing.id, form)
    } else {
      await jobApi.create(form)
    }
    reset()
    loadJobs()
  }

  const handleEdit = (job: Job) => {
    setEditing(job)
    setForm({
      title: job.title, description: job.description, requirements: job.requirements,
      salaryRange: job.salaryRange, location: job.location, jobType: job.jobType,
    })
    setShowForm(true)
  }

  const handleDelete = async (id: number) => {
    if (!confirm('确认删除该岗位？')) return
    await jobApi.delete(id)
    loadJobs()
  }

  const viewDeliveries = async (jobId: number) => {
    setShowDeliveries(jobId)
    const list = await deliveryApi.jobDeliveries(jobId)
    setDeliveries(list)
  }

  const viewResume = async (deliveryId: number) => {
    const resume = await deliveryApi.viewResume(deliveryId)
    setResumeView(resume)
  }

  const updateDeliveryStatus = async (deliveryId: number, status: string) => {
    await deliveryApi.updateStatus(deliveryId, status)
    if (showDeliveries) viewDeliveries(showDeliveries)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold">岗位管理</h2>
        <button onClick={() => { reset(); setShowForm(true) }}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus size={16} />发布岗位
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm p-6 mb-6 space-y-4">
          <h3 className="font-semibold">{editing ? '编辑岗位' : '发布新岗位'}</h3>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm font-medium mb-1">岗位名称 *</label>
              <input type="text" required value={form.title}
                onChange={e => setForm(p => ({ ...p, title: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">薪资范围</label>
              <input type="text" value={form.salaryRange}
                onChange={e => setForm(p => ({ ...p, salaryRange: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="如: 15K-25K" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">工作地点</label>
              <input type="text" value={form.location}
                onChange={e => setForm(p => ({ ...p, location: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">工作类型</label>
              <select value={form.jobType}
                onChange={e => setForm(p => ({ ...p, jobType: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500">
                <option>全职</option><option>实习</option><option>兼职</option>
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium mb-1">岗位描述</label>
              <textarea rows={4} value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium mb-1">任职要求</label>
              <textarea rows={4} value={form.requirements}
                onChange={e => setForm(p => ({ ...p, requirements: e.target.value }))}
                className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
          </div>
          <div className="flex gap-3">
            <button type="submit" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              {editing ? '保存修改' : '发布'}
            </button>
            <button type="button" onClick={reset} className="px-6 py-2 border rounded-lg hover:bg-gray-50 transition-colors">取消</button>
          </div>
        </form>
      )}

      <div className="space-y-3">
        {jobs.map(j => (
          <div key={j.id} className="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
            <div>
              <h4 className="font-medium">{j.title}</h4>
              <p className="text-sm text-gray-500">{j.location} · {j.salaryRange} · {formatDate(j.createdAt)}</p>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => viewDeliveries(j.id)}
                className="flex items-center gap-1 px-3 py-1.5 text-sm bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors">
                <Eye size={14} />查看候选人
              </button>
              <button onClick={() => handleEdit(j)}
                className="p-2 text-gray-400 hover:text-blue-600 transition-colors" title="编辑">
                <Edit3 size={16} />
              </button>
              <button onClick={() => handleDelete(j.id)}
                className="p-2 text-gray-400 hover:text-red-500 transition-colors" title="删除">
                <Trash2 size={16} />
              </button>
            </div>
          </div>
        ))}
      </div>

      {showDeliveries && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" onClick={() => { setShowDeliveries(null); setResumeView(null) }}>
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-xl max-h-[80vh] overflow-auto" onClick={e => e.stopPropagation()}>
            {resumeView ? (
              <>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-semibold">简历详情</h3>
                  <button onClick={() => setResumeView(null)}
                    className="p-1 hover:bg-gray-100 rounded transition-colors">
                    <X size={18} />
                  </button>
                </div>
                <div className="space-y-3 text-sm">
                  <div className="flex gap-4">
                    <div><span className="text-gray-500">姓名：</span>{resumeView.jobSeekerName}</div>
                    {resumeView.jobSeekerEmail && <div><span className="text-gray-500">邮箱：</span>{resumeView.jobSeekerEmail}</div>}
                    {resumeView.jobSeekerPhone && <div><span className="text-gray-500">电话：</span>{resumeView.jobSeekerPhone}</div>}
                  </div>
                  <div className="text-gray-500">简历文件：{resumeView.fileName}</div>
                  <div className="bg-gray-50 rounded-lg p-4 max-h-96 overflow-auto">
                    <pre className="whitespace-pre-wrap font-sans text-sm text-gray-700">{resumeView.parsedText}</pre>
                  </div>
                </div>
              </>
            ) : (
              <>
                <h3 className="font-semibold mb-4">投递记录</h3>
                <div className="flex gap-2 mb-4 flex-wrap">
                  {['ALL', 'PENDING', 'REVIEWED', 'INTERVIEW', 'OFFER', 'REJECTED'].map(s => (
                    <button key={s} onClick={() => setFilterStatus(s)}
                      className={`text-xs px-2.5 py-1 rounded-full transition-colors ${
                        filterStatus === s ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}>
                      {s === 'ALL' ? '全部' : s === 'PENDING' ? '待筛选' : s === 'REVIEWED' ? '已通过' : s === 'INTERVIEW' ? '面试中' : s === 'OFFER' ? '已录用' : '已拒绝'}
                    </button>
                  ))}
                </div>
                {deliveries.filter(d => filterStatus === 'ALL' || d.status === filterStatus).length === 0 ? (
                  <p className="text-gray-400 text-center py-8">暂无投递</p>
                ) : (
                  <div className="space-y-3">
                    {deliveries.filter(d => filterStatus === 'ALL' || d.status === filterStatus).map(d => (
                      <div key={d.id} className="border rounded-lg p-3">
                        <div className="flex items-center justify-between mb-2">
                          <div>
                            <span className="text-sm font-medium">{d.jobSeekerName || `求职者 #${d.jobSeekerId}`}</span>
                            <span className="text-xs text-gray-400 ml-2">{d.resumeFileName}</span>
                          </div>
                          <span className={`text-xs px-2 py-0.5 rounded-full ${
                            d.status === 'PENDING' ? 'bg-yellow-50 text-yellow-700' :
                            d.status === 'OFFER' ? 'bg-green-50 text-green-700' :
                            d.status === 'REJECTED' ? 'bg-red-50 text-red-700' :
                            'bg-blue-50 text-blue-700'
                          }`}>{d.status}</span>
                        </div>
                        <div className="flex gap-2">
                          <button onClick={() => viewResume(d.id)}
                            className="flex items-center gap-1 text-xs px-2 py-1 border rounded hover:bg-blue-50 hover:text-blue-600 transition-colors">
                            <FileText size={12} />查看简历
                          </button>
                          {['REVIEWED', 'INTERVIEW', 'OFFER', 'REJECTED'].map(s => (
                            <button key={s} onClick={() => updateDeliveryStatus(d.id, s)}
                              className="text-xs px-2 py-1 border rounded hover:bg-gray-50 transition-colors">
                              {s === 'REVIEWED' ? '通过筛选' : s === 'INTERVIEW' ? '进入面试' : s === 'OFFER' ? '录用' : '拒绝'}
                            </button>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
