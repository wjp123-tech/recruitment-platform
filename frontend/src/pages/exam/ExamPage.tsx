import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { examApi, jobApi } from '../../api'
import type { Exam, ExamQuestion, Job } from '../../types'
import { Play, Send, Clock, AlertCircle, CheckCircle } from 'lucide-react'

function formatTime(totalSeconds: number): string {
  const m = Math.floor(totalSeconds / 60)
  const s = totalSeconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export default function ExamPage() {
  const nav = useNavigate()
  const [exams, setExams] = useState<Exam[]>([])
  const [jobs, setJobs] = useState<Job[]>([])
  const [selectedJob, setSelectedJob] = useState<number | null>(null)
  const [duration, setDuration] = useState(30)
  const [activeExam, setActiveExam] = useState<Exam | null>(null)
  const [questions, setQuestions] = useState<ExamQuestion[]>([])
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [generating, setGenerating] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [remaining, setRemaining] = useState(0)
  const [expired, setExpired] = useState(false)
  const timerRef = useRef<ReturnType<typeof setInterval>>()

  useEffect(() => {
    examApi.history().then(setExams)
    jobApi.list().then(r => setJobs(r.content))
  }, [])

  // 倒计时
  useEffect(() => {
    if (activeExam) {
      examApi.remainingTime(activeExam.id).then(r => setRemaining(r))
      timerRef.current = setInterval(() => {
        setRemaining(prev => {
          if (prev <= 1) {
            clearInterval(timerRef.current)
            setExpired(true)
            return 0
          }
          return prev - 1
        })
      }, 1000)
    }
    return () => { if (timerRef.current) clearInterval(timerRef.current) }
  }, [activeExam])

  // 时间到自动提交
  useEffect(() => {
    if (expired && activeExam) {
      handleSubmit()
    }
  }, [expired])

  const handleGenerate = async () => {
    if (!selectedJob) return
    setGenerating(true)
    try {
      const config = {
        totalCount: 5, singleChoice: 3, multiChoice: 0, essay: 2, coding: 0,
        durationMinutes: duration,
      }
      const exam = await examApi.generate(selectedJob, config)
      setActiveExam(exam)
      const detail = await examApi.detail(exam.id)
      setQuestions(detail.questions)
      setRemaining(duration * 60)
      setExpired(false)
    } catch (err: any) {
      alert(err.message)
    } finally {
      setGenerating(false)
    }
  }

  const handleSubmit = async () => {
    if (!activeExam) return
    setSubmitting(true)
    try {
      await examApi.submit(activeExam.id, answers)
      nav(`/exam/${activeExam.id}/result`)
    } catch (err: any) {
      alert(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (activeExam) {
    const isLastMinute = remaining <= 60

    return (
      <div className="max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-4 sticky top-14 bg-gray-50 z-10 py-3">
          <h2 className="text-xl font-bold">AI 模拟笔试</h2>
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-500">总分: {activeExam.totalScore} 分 · {duration} 分钟</span>
            <span className={`flex items-center gap-1.5 text-lg font-mono font-bold px-3 py-1.5 rounded-lg ${
              isLastMinute ? 'bg-red-50 text-red-600 animate-pulse' : 'bg-blue-50 text-blue-700'
            }`}>
              <Clock size={18} />
              {formatTime(remaining)}
            </span>
          </div>
        </div>

        <div className="space-y-5">
          {questions.map((q, i) => (
            <div key={i} className="bg-white rounded-xl shadow-sm p-5">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium text-gray-500">
                  第 {i + 1}/{questions.length} 题 · {q.type === 'SINGLE_CHOICE' ? '单选题' : '简答题'} · {q.score} 分
                </span>
                <span className="text-xs bg-gray-100 px-2 py-0.5 rounded">{q.difficulty}</span>
              </div>
              <p className="text-gray-800 mb-3 whitespace-pre-wrap">{q.content}</p>
              {q.type === 'SINGLE_CHOICE' && q.options ? (
                <div className="space-y-2">
                  {q.options.map((opt, oi) => (
                    <label key={oi} className={`flex items-center gap-2 p-2.5 rounded-lg cursor-pointer transition-colors ${
                      answers[i] === opt.charAt(0) ? 'bg-blue-50 border border-blue-200' : 'border hover:bg-gray-50'
                    }`}>
                      <input type="radio" name={`q-${i}`} value={opt.charAt(0)}
                        checked={answers[i] === opt.charAt(0)}
                        onChange={e => setAnswers(p => ({ ...p, [i]: e.target.value }))}
                        className="hidden" />
                      <span className="text-sm">{opt}</span>
                    </label>
                  ))}
                </div>
              ) : (
                <textarea
                  value={answers[i] || ''}
                  onChange={e => setAnswers(p => ({ ...p, [i]: e.target.value }))}
                  rows={4}
                  placeholder="请输入你的答案..."
                  className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                />
              )}
            </div>
          ))}
        </div>

        <div className="mt-6 flex items-center gap-4">
          <button onClick={handleSubmit} disabled={submitting}
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors text-lg">
            <Send size={18} />{submitting ? '提交中...' : '提交答卷'}
          </button>
          {isLastMinute && (
            <span className="flex items-center gap-1 text-sm text-red-600">
              <AlertCircle size={14} />剩余时间不足 1 分钟，请尽快提交！
            </span>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-xl font-bold mb-6">AI 模拟笔试</h2>
      <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">选择笔试岗位</label>
          <select value={selectedJob || ''}
            onChange={e => setSelectedJob(Number(e.target.value))}
            className="w-full px-3 py-2 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">选择岗位</option>
            {jobs.map(j => <option key={j.id} value={j.id}>{j.title}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">考试时长: {duration} 分钟</label>
          <input type="range" min={5} max={120} step={5} value={duration}
            onChange={e => setDuration(Number(e.target.value))}
            className="w-full" />
          <div className="flex justify-between text-xs text-gray-400">
            <span>5分钟</span><span>120分钟</span>
          </div>
        </div>
        <button onClick={handleGenerate} disabled={!selectedJob || generating}
          className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors">
          <Play size={16} />{generating ? 'AI 正在出题...' : '开始笔试'}
        </button>
      </div>

      {exams.length > 0 && (
        <div className="mt-8">
          <h3 className="font-semibold mb-3">笔试记录</h3>
          <div className="space-y-2">
            {exams.map(e => (
              <div key={e.id} className="bg-white rounded-lg p-4 shadow-sm flex items-center justify-between">
                <span className="text-sm">试卷 #{e.id} · 总分 {e.totalScore}</span>
                {e.status === 'GRADED' ? (
                  <button onClick={() => nav(`/exam/${e.id}/result`)}
                    className="flex items-center gap-1 text-sm text-green-600 hover:underline">
                    <CheckCircle size={14} />查看成绩
                  </button>
                ) : (
                  <span className="text-xs bg-yellow-50 text-yellow-700 px-2 py-0.5 rounded">未提交</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
