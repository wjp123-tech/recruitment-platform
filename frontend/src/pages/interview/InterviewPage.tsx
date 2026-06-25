import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { interviewApi, jobApi, resumeApi } from '../../api'
import type { InterviewSession, Job, Resume } from '../../types'
import ReactMarkdown from 'react-markdown'
import { Play, Send, FileText, Loader2 } from 'lucide-react'

export default function InterviewPage() {
  const nav = useNavigate()
  const [sessions, setSessions] = useState<InterviewSession[]>([])
  const [jobs, setJobs] = useState<Job[]>([])
  const [resumes, setResumes] = useState<Resume[]>([])
  const [selectedJob, setSelectedJob] = useState<number | null>(null)
  const [selectedResume, setSelectedResume] = useState<number | null>(null)
  const [activeSession, setActiveSession] = useState<InterviewSession | null>(null)
  const [messages, setMessages] = useState<{ role: string; content: string }[]>([])
  const [input, setInput] = useState('')
  const [streaming, setStreaming] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    interviewApi.listSessions().then(setSessions)
    jobApi.list().then(r => setJobs(r.content))
    resumeApi.list().then(setResumes)
  }, [])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleCreate = async () => {
    if (!selectedJob || !selectedResume) return
    const session = await interviewApi.createSession(selectedJob, selectedResume, { maxRounds: 10 })
    const { opening } = await interviewApi.start(session.id)
    setActiveSession(session)
    setMessages([{ role: 'assistant', content: opening }])
  }

  const handleSend = () => {
    if (!input.trim() || !activeSession) return
    const userMsg = input
    setInput('')
    setMessages(prev => [...prev, { role: 'user', content: userMsg }])
    setStreaming(true)

    let aiContent = ''
    setMessages(prev => [...prev, { role: 'assistant', content: '' }])

    interviewApi.answerStream(
      activeSession.id, userMsg,
      (chunk) => {
        aiContent += chunk
        setMessages(prev => {
          const updated = [...prev]
          updated[updated.length - 1] = { role: 'assistant', content: aiContent }
          return updated
        })
      },
      () => { setStreaming(false) },
      (err) => {
        setMessages(prev => {
          const updated = [...prev]
          updated[updated.length - 1] = { role: 'assistant', content: '错误: ' + err.message }
          return updated
        })
        setStreaming(false)
      }
    )
  }

  const handleEnd = async () => {
    if (!activeSession) return
    const report = await interviewApi.end(activeSession.id)
    nav(`/interview/${report.sessionId}/report`)
  }

  if (activeSession) {
    return (
      <div className="max-w-3xl mx-auto h-[calc(100vh-7rem)] flex flex-col">
        <div className="bg-white rounded-t-xl shadow-sm p-4 flex items-center justify-between border-b">
          <h3 className="font-semibold">AI 模拟面试 · 第 {Math.floor(messages.length / 2)} 轮</h3>
          <button onClick={handleEnd} disabled={streaming}
            className="px-4 py-1.5 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 text-sm transition-colors">
            结束面试
          </button>
        </div>
        <div className="flex-1 overflow-y-auto bg-white p-4 space-y-4">
          {messages.map((m, i) => (
            <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[80%] rounded-xl px-4 py-3 ${
                m.role === 'user' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-800'
              }`}>
                <div className="markdown-body text-sm">
                  <ReactMarkdown>{m.content}</ReactMarkdown>
                </div>
              </div>
            </div>
          ))}
          {streaming && messages[messages.length - 1]?.content === '' && (
            <div className="flex items-center gap-2 text-gray-400 text-sm">
              <Loader2 size={14} className="animate-spin" />AI 正在思考...
            </div>
          )}
          <div ref={bottomRef} />
        </div>
        <div className="bg-white border-t p-4 flex gap-2 rounded-b-xl shadow-sm">
          <input
            type="text" value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend() } }}
            placeholder="输入你的回答..."
            disabled={streaming}
            className="flex-1 px-4 py-2 border rounded-full outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-50"
          />
          <button onClick={handleSend} disabled={streaming || !input.trim()}
            className="p-2.5 bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:opacity-50 transition-colors">
            <Send size={18} />
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-xl font-bold mb-6">AI 模拟面试</h2>
      <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">选择面试岗位</label>
          <select value={selectedJob || ''}
            onChange={e => setSelectedJob(Number(e.target.value))}
            className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">选择岗位</option>
            {jobs.map(j => <option key={j.id} value={j.id}>{j.title}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">选择简历</label>
          <select value={selectedResume || ''}
            onChange={e => setSelectedResume(Number(e.target.value))}
            className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">选择简历</option>
            {resumes.map(r => <option key={r.id} value={r.id}>{r.fileName}</option>)}
          </select>
        </div>
        <button onClick={handleCreate} disabled={!selectedJob || !selectedResume}
          className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
          <Play size={16} />开始面试
        </button>
      </div>

      {sessions.length > 0 && (
        <div className="mt-8">
          <h3 className="font-semibold mb-3">面试记录</h3>
          <div className="space-y-2">
            {sessions.map(s => (
              <div key={s.id} className="bg-white rounded-lg p-4 shadow-sm flex items-center justify-between">
                <div>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${
                    s.status === 'COMPLETED' ? 'bg-green-50 text-green-700' :
                    s.status === 'IN_PROGRESS' ? 'bg-blue-50 text-blue-700' : 'bg-gray-50 text-gray-600'
                  }`}>{s.status}</span>
                  <span className="text-sm text-gray-500 ml-2">轮次: {s.currentRound}/{s.maxRounds}</span>
                </div>
                {s.status === 'COMPLETED' && (
                  <button onClick={() => nav(`/interview/${s.id}/report`)}
                    className="flex items-center gap-1 text-sm text-blue-600 hover:underline">
                    <FileText size={14} />查看报告
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
