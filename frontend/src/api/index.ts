import { api, getToken } from '../utils/request'
import type {
  User, Job, Resume, Delivery, Notification,
  InterviewSession, InterviewReport, Exam, ExamQuestion,
  ExamAnswer, ChatMessage, ApiResult,
} from '../types'

const BASE = ''

// Auth
export const authApi = {
  login: (body: { username: string; password: string }) =>
    api.post<{ token: string; userId: number; username: string; role: string }>(`${BASE}/api/user/login`, body),
  register: (body: { username: string; password: string; role: string; email?: string; phone?: string }) =>
    api.post<{ token: string; userId: number; username: string; role: string }>(`${BASE}/api/user/register`, body),
  getProfile: () => api.get<User>(`${BASE}/api/user/profile`),
  updateProfile: (body: { email?: string; phone?: string }) =>
    api.put<void>(`${BASE}/api/user/profile`, body),
  savePreferences: (body: { desiredTitle: string; desiredLocation: string; desiredSalary: string }) =>
    api.put<User>(`${BASE}/api/user/preferences`, body),
}

// Jobs
export const jobApi = {
  list: (keyword?: string, page = 0, size = 10) =>
    api.get<{ content: Job[]; totalElements: number; totalPages: number }>(
      `${BASE}/api/jobs?keyword=${keyword || ''}&page=${page}&size=${size}`
    ),
  detail: (id: number) => api.get<Job>(`${BASE}/api/jobs/${id}`),
  create: (body: Partial<Job>) => api.post<Job>(`${BASE}/api/jobs`, body),
  update: (id: number, body: Partial<Job>) => api.put<Job>(`${BASE}/api/jobs/${id}`, body),
  delete: (id: number) => api.delete<void>(`${BASE}/api/jobs/${id}`),
  myJobs: (page = 0, size = 10) =>
    api.get<{ content: Job[] }>(`${BASE}/api/jobs/my?page=${page}&size=${size}`),
  recommend: (topK = 10) => api.get<Job[]>(`${BASE}/api/jobs/recommend?topK=${topK}`),
}

// Resume
export const resumeApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const token = getToken()
    return fetch(`${BASE}/api/resume/upload`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    }).then(r => r.json()).then(j => j.data as Resume)
  },
  list: () => api.get<Resume[]>(`${BASE}/api/resume/list`),
  detail: (id: number) => api.get<Resume>(`${BASE}/api/resume/${id}`),
  delete: (id: number) => api.delete<void>(`${BASE}/api/resume/${id}`),
}

// Delivery
export const deliveryApi = {
  apply: (jobId: number, resumeId: number) =>
    api.post<Delivery>(`${BASE}/api/delivery/apply`, { jobId, resumeId }),
  myDeliveries: (page = 0, size = 10) =>
    api.get<{ content: Delivery[] }>(`${BASE}/api/delivery/my?page=${page}&size=${size}`),
  jobDeliveries: (jobId: number, page = 0, size = 10) =>
    api.get<Delivery[]>(`${BASE}/api/delivery/job/${jobId}?page=${page}&size=${size}`),
  viewResume: (deliveryId: number) =>
    api.get<{ resumeId: number; fileName: string; parsedText: string; jobSeekerName: string; jobSeekerEmail: string; jobSeekerPhone: string }>(`${BASE}/api/delivery/${deliveryId}/resume`),
  updateStatus: (id: number, status: string, remark?: string) =>
    api.put<Delivery>(`${BASE}/api/delivery/${id}/status`, { status, remark }),
}

// Chat
export const chatApi = {
  getContacts: () => api.get<number[]>(`${BASE}/api/chat/contacts`),
  getHistory: (userId: number, page = 0, size = 30) =>
    api.get<{ content: ChatMessage[] }>(`${BASE}/api/chat/history/${userId}?page=${page}&size=${size}`),
  send: (receiverId: number, content: string) =>
    api.post<ChatMessage>(`${BASE}/api/chat/send`, { receiverId, content }),
}

// Notification
export const notifApi = {
  list: (page = 0, size = 20) =>
    api.get<{ content: Notification[] }>(`${BASE}/api/notification/list?page=${page}&size=${size}`),
  unreadCount: () => api.get<{ count: number }>(`${BASE}/api/notification/unread-count`),
  markRead: (id: number) => api.put<void>(`${BASE}/api/notification/${id}/read`),
  markAllRead: () => api.put<void>(`${BASE}/api/notification/read-all`),
}

// Interview
export const interviewApi = {
  createSession: (jobId: number, resumeId: number, config?: { maxRounds: number }) =>
    api.post<InterviewSession>(`${BASE}/api/interview/sessions`, { jobId, resumeId, config }),
  start: (id: number) =>
    api.post<{ opening: string }>(`${BASE}/api/interview/sessions/${id}/start`),
  end: (id: number) =>
    api.post<InterviewReport>(`${BASE}/api/interview/sessions/${id}/end`),
  listSessions: () => api.get<InterviewSession[]>(`${BASE}/api/interview/sessions`),
  getReport: (id: number) => api.get<InterviewReport>(`${BASE}/api/interview/sessions/${id}/report`),
  answerStream: (
    sessionId: number,
    answer: string,
    onChunk: (text: string) => void,
    onDone: () => void,
    onError: (err: Error) => void,
  ) => {
    const token = getToken()
    fetch(`${BASE}/api/interview/sessions/${sessionId}/answer`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ answer }),
    }).then(async (res) => {
      const reader = res.body?.getReader()
      if (!reader) { onError(new Error('No stream')); return }
      const decoder = new TextDecoder()
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) { onDone(); break }
        buffer += decoder.decode(value, { stream: true })
        const parts = buffer.split('\n\n')
        buffer = parts.pop() || ''
        for (const part of parts) {
          const lines = part.split('\n')
          for (const line of lines) {
            if (line.startsWith('data:')) {
              onChunk(line.slice(5).replace(/\\n/g, '\n'))
            }
          }
        }
      }
    }).catch(onError)
  },
}

// Exam
export const examApi = {
  generate: (jobId: number, config?: { totalCount: number; singleChoice: number; multiChoice: number; essay: number; coding: number }) =>
    api.post<Exam>(`${BASE}/api/exam/generate`, { jobId, config }),
  detail: (id: number) =>
    api.get<{ exam: Exam; questions: ExamQuestion[] }>(`${BASE}/api/exam/${id}`),
  submit: (examId: number, answers: Record<number, string>) =>
    api.post<void>(`${BASE}/api/exam/${examId}/submit`, { answers }),
  remainingTime: (examId: number) =>
    api.get<number>(`${BASE}/api/exam/${examId}/remaining`),
  result: (examId: number) =>
    api.get<{ exam: Exam; questions: ExamQuestion[]; answers: ExamAnswer[] }>(`${BASE}/api/exam/${examId}/result`),
  history: () => api.get<Exam[]>(`${BASE}/api/exam/history`),
}
