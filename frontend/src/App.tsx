import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import AuthGuard from './components/AuthGuard'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import JobListPage from './pages/jobs/JobListPage'
import JobDetailPage from './pages/jobs/JobDetailPage'
import JobManagePage from './pages/jobs/JobManagePage'
import ResumePage from './pages/resume/ResumePage'
import DeliveryPage from './pages/delivery/DeliveryPage'
import ChatPage from './pages/chat/ChatPage'
import InterviewPage from './pages/interview/InterviewPage'
import InterviewReportPage from './pages/interview/InterviewReportPage'
import ExamPage from './pages/exam/ExamPage'
import ExamResultPage from './pages/exam/ExamResultPage'
import NotificationPage from './pages/notification/NotificationPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<AuthGuard><Layout /></AuthGuard>}>
        <Route path="/" element={<Navigate to="/jobs" replace />} />
        <Route path="/jobs" element={<JobListPage />} />
        <Route path="/jobs/:id" element={<JobDetailPage />} />
        <Route path="/jobs/manage" element={<JobManagePage />} />
        <Route path="/resume" element={<ResumePage />} />
        <Route path="/delivery" element={<DeliveryPage />} />
        <Route path="/chat" element={<ChatPage />} />
        <Route path="/chat/:userId" element={<ChatPage />} />
        <Route path="/interview" element={<InterviewPage />} />
        <Route path="/interview/:id/report" element={<InterviewReportPage />} />
        <Route path="/exam" element={<ExamPage />} />
        <Route path="/exam/:id/result" element={<ExamResultPage />} />
        <Route path="/notifications" element={<NotificationPage />} />
      </Route>
    </Routes>
  )
}
