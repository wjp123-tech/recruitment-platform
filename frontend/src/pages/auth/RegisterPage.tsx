import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../../api'
import { setToken, setUser } from '../../utils/request'

export default function RegisterPage() {
  const nav = useNavigate()
  const [form, setForm] = useState({
    username: '', password: '', role: 'JOB_SEEKER', email: '', phone: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authApi.register(form)
      setToken(res.token)
      setUser({ userId: res.userId, username: res.username, role: res.role })
      nav('/jobs')
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-8">
      <div className="w-full max-w-md">
        <h1 className="text-2xl font-bold text-center mb-8 text-blue-600">招聘面试智能系统</h1>
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm p-8 space-y-4">
          <h2 className="text-lg font-semibold text-center">注册</h2>
          {error && <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">用户名 *</label>
            <input type="text" required value={form.username}
              onChange={e => setForm(p => ({ ...p, username: e.target.value }))}
              className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">密码 *</label>
            <input type="password" required value={form.password}
              onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
              className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">角色 *</label>
            <select value={form.role}
              onChange={e => setForm(p => ({ ...p, role: e.target.value }))}
              className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="JOB_SEEKER">求职者</option>
              <option value="RECRUITER">招聘官</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
            <input type="email" value={form.email}
              onChange={e => setForm(p => ({ ...p, email: e.target.value }))}
              className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <button type="submit" disabled={loading}
            className="w-full py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 font-medium transition-colors">
            {loading ? '注册中...' : '注册'}
          </button>
          <p className="text-center text-sm text-gray-500">
            已有账号？<Link to="/login" className="text-blue-600 hover:underline">去登录</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
