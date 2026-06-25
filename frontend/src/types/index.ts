export interface User {
  id: number
  username: string
  role: 'JOB_SEEKER' | 'RECRUITER'
  email?: string
  phone?: string
  avatar?: string
}

export interface Job {
  id: number
  recruiterId: number
  title: string
  description: string
  requirements: string
  salaryRange: string
  location: string
  jobType: string
  status: string
  createdAt: string
}

export interface Resume {
  id: number
  userId: number
  fileName: string
  parsedText: string
  status: string
  createdAt: string
}

export interface Delivery {
  id: number
  jobId: number
  resumeId: number
  resumeFileName?: string
  jobSeekerId: number
  jobSeekerName?: string
  recruiterId: number
  status: 'PENDING' | 'REVIEWED' | 'INTERVIEW' | 'OFFER' | 'REJECTED'
  remark: string
  createdAt: string
}

export interface Notification {
  id: number
  receiverId: number
  type: string
  title: string
  content: string
  isRead: boolean
  refId: number
  createdAt: string
}

export interface InterviewSession {
  id: number
  userId: number
  jobId: number
  resumeId: number
  maxRounds: number
  currentRound: number
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  createdAt: string
}

export interface InterviewReport {
  id: number
  sessionId: number
  overallScore: number
  dimensions: string
  strengths: string
  weaknesses: string
  suggestions: string
  createdAt: string
}

export interface Exam {
  id: number
  userId: number
  jobId: number
  questions: string
  totalScore: number
  status: 'IN_PROGRESS' | 'GRADED'
  createdAt: string
}

export interface ExamQuestion {
  type: 'SINGLE_CHOICE' | 'MULTI_CHOICE' | 'ESSAY' | 'CODING'
  content: string
  options?: string[]
  answer?: string
  score: number
  difficulty: string
  knowledgePoint: string
}

export interface ExamAnswer {
  id: number
  examId: number
  questionIndex: number
  userAnswer: string
  score: number
  feedback: string
  gradedAt: string
}

export interface ChatMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  isRead: boolean
  createdAt: string
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
