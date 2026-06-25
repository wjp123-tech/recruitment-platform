import { useState, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getUser, clearToken } from '../utils/request'
import { notifApi } from '../api'
import { Menu, X, Bell, User, LogOut } from 'lucide-react'

export default function Navbar() {
  const user = getUser()
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    notifApi.unreadCount().then(r => setUnread(r.count)).catch(() => {})
    const timer = setInterval(() => {
      notifApi.unreadCount().then(r => setUnread(r.count)).catch(() => {})
    }, 30000)
    return () => clearInterval(timer)
  }, [])

  const isRecruiter = user?.role === 'RECRUITER'
  const isActive = (path: string) => location.pathname.startsWith(path)

  const navItems = [
    { path: '/jobs', label: '岗位浏览', show: true },
    { path: '/jobs/manage', label: '岗位管理', show: isRecruiter },
    { path: '/resume', label: '我的简历', show: !isRecruiter },
    { path: '/delivery', label: '投递记录', show: true },
    { path: '/interview', label: 'AI 面试', show: !isRecruiter },
    { path: '/exam', label: 'AI 笔试', show: !isRecruiter },
    { path: '/chat', label: '在线聊天', show: true },
  ]

  const handleLogout = () => {
    clearToken()
    navigate('/login')
  }

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center justify-between h-14">
          <Link to="/" className="text-lg font-bold text-blue-600 shrink-0">
            招聘面试智能系统
          </Link>

          <div className="hidden md:flex items-center gap-1">
            {navItems.filter(i => i.show).map(i => (
              <Link
                key={i.path}
                to={i.path}
                className={`px-3 py-1.5 rounded-md text-sm transition-colors ${
                  isActive(i.path)
                    ? 'bg-blue-50 text-blue-700 font-medium'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                {i.label}
              </Link>
            ))}
          </div>

          <div className="flex items-center gap-3">
            <Link to="/notifications" className="relative p-2 text-gray-500 hover:text-blue-600 transition-colors">
              <Bell size={20} />
              {unread > 0 && (
                <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {unread > 99 ? '99+' : unread}
                </span>
              )}
            </Link>

            <div className="hidden md:flex items-center gap-2 text-sm text-gray-600">
              <User size={16} />
              <span>{user?.username}</span>
              <span className="text-xs bg-gray-100 px-1.5 py-0.5 rounded">
                {isRecruiter ? '招聘官' : '求职者'}
              </span>
            </div>

            <button
              onClick={handleLogout}
              className="hidden md:block p-2 text-gray-400 hover:text-red-500 transition-colors"
              title="退出登录"
            >
              <LogOut size={18} />
            </button>

            <button className="md:hidden p-2" onClick={() => setOpen(!open)}>
              {open ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {open && (
          <div className="md:hidden pb-3 border-t border-gray-100 pt-2">
            {navItems.filter(i => i.show).map(i => (
              <Link
                key={i.path}
                to={i.path}
                onClick={() => setOpen(false)}
                className={`block px-3 py-2 rounded-md text-sm ${
                  isActive(i.path) ? 'bg-blue-50 text-blue-700' : 'text-gray-600'
                }`}
              >
                {i.label}
              </Link>
            ))}
            <button
              onClick={handleLogout}
              className="block w-full text-left px-3 py-2 text-sm text-red-500 mt-1"
            >
              退出登录
            </button>
          </div>
        )}
      </div>
    </nav>
  )
}
