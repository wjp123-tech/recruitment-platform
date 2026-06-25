import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { jobApi, authApi } from '../../api'
import { getUser } from '../../utils/request'
import { formatDate } from '../../utils/date'
import type { Job } from '../../types'
import { Search, MapPin, Briefcase, Target, Settings } from 'lucide-react'

export default function JobListPage() {
  const user = getUser()
  const [jobs, setJobs] = useState<Job[]>([])
  const [keyword, setKeyword] = useState('')
  const [recommended, setRecommended] = useState<Job[]>([])
  const [loading, setLoading] = useState(true)
  const [showPrefs, setShowPrefs] = useState(false)
  const [prefs, setPrefs] = useState({ desiredTitle: '', desiredLocation: '', desiredSalary: '' })
  const [prefsLoaded, setPrefsLoaded] = useState(false)

  const isJobSeeker = user?.role === 'JOB_SEEKER'

  useEffect(() => {
    if (isJobSeeker) {
      authApi.getProfile().then(p => {
        if (p.desiredTitle || p.desiredLocation || p.desiredSalary) {
          setPrefs({ desiredTitle: p.desiredTitle || '', desiredLocation: p.desiredLocation || '', desiredSalary: p.desiredSalary || '' })
        }
        setPrefsLoaded(true)
      }).catch(() => setPrefsLoaded(true))
    }
  }, [])

  const fetchJobs = async (kw?: string) => {
    setLoading(true)
    try {
      const res = await jobApi.list(kw)
      setJobs(res.content)
    } finally {
      setLoading(false)
    }
  }

  const loadRecommendations = () => {
    if (isJobSeeker) {
      jobApi.recommend(5).then(setRecommended).catch(() => {})
    }
  }

  useEffect(() => { fetchJobs(); loadRecommendations() }, [])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    fetchJobs(keyword)
  }

  const handleSavePrefs = async () => {
    await authApi.savePreferences(prefs)
    setShowPrefs(false)
    loadRecommendations()
  }

  const hasPrefs = prefs.desiredTitle || prefs.desiredLocation || prefs.desiredSalary

  return (
    <div>
      <form onSubmit={handleSearch} className="flex gap-2 mb-4">
        <div className="relative flex-1">
          <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input type="text" value={keyword}
            onChange={e => setKeyword(e.target.value)}
            placeholder="搜索岗位名称或描述..."
            className="w-full pl-10 pr-4 py-2.5 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button type="submit" className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          搜索
        </button>
      </form>

      {/* 求职意向设置 */}
      {isJobSeeker && (
        <div className="mb-4 flex items-center gap-2">
          <button onClick={() => setShowPrefs(true)}
            className="flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg border hover:bg-gray-50 transition-colors">
            <Settings size={14} />
            {hasPrefs ? `意向：${prefs.desiredTitle || prefs.desiredLocation || prefs.desiredSalary || '已设置'}` : '设置求职意向，获得精准推荐'}
          </button>
          {!hasPrefs && prefsLoaded && (
            <span className="text-xs text-orange-500 flex items-center gap-1">
              <Target size={12} />设置意向后可AI推荐
            </span>
          )}
        </div>
      )}

      {/* AI 推荐 */}
      {recommended.length > 0 && (
        <div className="mb-6">
          <h3 className="text-sm font-medium text-gray-500 mb-2 flex items-center gap-1.5">
            <Target size={14} className="text-blue-600" />
            {hasPrefs ? '根据你的求职意向推荐' : 'AI 岗位推荐'}
          </h3>
          <div className="grid gap-3 md:grid-cols-2">
            {recommended.map(j => (
              <Link key={j.id} to={`/jobs/${j.id}`}
                className="block bg-gradient-to-r from-blue-50 to-white border border-blue-100 rounded-lg p-4 hover:shadow transition-shadow">
                <div className="font-medium text-blue-700">{j.title}</div>
                <div className="text-sm text-gray-500 mt-1 flex items-center gap-3">
                  <span className="flex items-center gap-1"><MapPin size={14} />{j.location}</span>
                  <span>{j.salaryRange}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {jobs.map(j => (
          <Link key={j.id} to={`/jobs/${j.id}`}
            className="block bg-white border rounded-xl p-5 hover:shadow-md hover:border-blue-200 transition-all">
            <div className="flex items-start justify-between">
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-gray-900 truncate">{j.title}</h3>
                <p className="text-blue-600 font-medium mt-1">{j.salaryRange}</p>
              </div>
              <span className="shrink-0 text-xs bg-green-50 text-green-700 px-2 py-1 rounded-full">
                {j.jobType}
              </span>
            </div>
            <div className="flex items-center gap-3 mt-3 text-sm text-gray-500">
              <span className="flex items-center gap-1"><MapPin size={14} />{j.location}</span>
              <span className="flex items-center gap-1"><Briefcase size={14} />{formatDate(j.createdAt)}</span>
            </div>
          </Link>
        ))}
      </div>
      {!loading && jobs.length === 0 && (
        <div className="text-center text-gray-400 py-20">暂无岗位信息</div>
      )}

      {/* 意向设置弹窗 */}
      {showPrefs && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" onClick={() => setShowPrefs(false)}>
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h3 className="font-semibold text-lg mb-4 flex items-center gap-2">
              <Target size={18} className="text-blue-600" />设置求职意向
            </h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">期望岗位</label>
                <input type="text" value={prefs.desiredTitle}
                  onChange={e => setPrefs(p => ({ ...p, desiredTitle: e.target.value }))}
                  placeholder="如: Java后端开发、前端开发"
                  className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">期望城市</label>
                <input type="text" value={prefs.desiredLocation}
                  onChange={e => setPrefs(p => ({ ...p, desiredLocation: e.target.value }))}
                  placeholder="如: 北京、上海"
                  className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">期望薪资</label>
                <input type="text" value={prefs.desiredSalary}
                  onChange={e => setPrefs(p => ({ ...p, desiredSalary: e.target.value }))}
                  placeholder="如: 15K-25K"
                  className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button onClick={handleSavePrefs}
                className="flex-1 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                保存并推荐
              </button>
              <button onClick={() => setShowPrefs(false)}
                className="flex-1 py-2.5 border rounded-lg hover:bg-gray-50 transition-colors">
                取消
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
