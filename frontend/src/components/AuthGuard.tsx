import { Navigate } from 'react-router-dom'
import { getToken } from '../utils/request'

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}
