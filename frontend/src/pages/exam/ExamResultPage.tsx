import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { examApi } from '../../api'
import type { Exam, ExamQuestion, ExamAnswer } from '../../types'
import { CheckCircle, XCircle, Award } from 'lucide-react'

export default function ExamResultPage() {
  const { id } = useParams<{ id: string }>()
  const [exam, setExam] = useState<Exam | null>(null)
  const [questions, setQuestions] = useState<ExamQuestion[]>([])
  const [answers, setAnswers] = useState<ExamAnswer[]>([])

  useEffect(() => {
    examApi.result(Number(id)).then(r => {
      setExam(r.exam)
      setQuestions(r.questions)
      setAnswers(r.answers)
    })
  }, [id])

  if (!exam) return <div className="text-center py-20 text-gray-400">加载中...</div>

  const totalScore = answers.reduce((s, a) => s + (a.score || 0), 0)

  return (
    <div className="max-w-3xl mx-auto">
      <div className="text-center mb-8">
        <Award size={48} className="mx-auto text-blue-600 mb-3" />
        <div className="text-4xl font-bold text-blue-600">{totalScore} / {exam.totalScore}</div>
        <p className="text-gray-500 mt-1">笔试成绩</p>
      </div>

      <div className="space-y-4">
        {questions.map((q, i) => {
          const ans = answers[i]
          const isCorrect = q.type === 'SINGLE_CHOICE' && ans?.userAnswer === q.answer
          return (
            <div key={i} className="bg-white rounded-xl shadow-sm p-5">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium">第 {i + 1} 题 · {q.score} 分</span>
                <span className="text-sm font-semibold text-blue-600">
                  {ans?.score ?? 0}/{q.score} 分
                </span>
              </div>
              <p className="text-gray-800 mb-3">{q.content}</p>

              {q.type === 'SINGLE_CHOICE' ? (
                <div className="space-y-2">
                  {q.options?.map((opt, oi) => {
                    const letter = opt.charAt(0)
                    let bg = 'bg-gray-50'
                    if (letter === q.answer) bg = 'bg-green-50 border-green-200'
                    if (letter === ans?.userAnswer && ans?.userAnswer !== q.answer) bg = 'bg-red-50 border-red-200'
                    return (
                      <div key={oi} className={`px-3 py-2 rounded-lg border text-sm ${bg}`}>
                        {letter === ans?.userAnswer && <span className="mr-2">{isCorrect ? <CheckCircle size={14} className="inline text-green-500" /> : <XCircle size={14} className="inline text-red-500" />}</span>}
                        {opt}
                        {letter === q.answer && <span className="ml-2 text-xs text-green-600">正确答案</span>}
                      </div>
                    )
                  })}
                </div>
              ) : (
                <div>
                  <p className="text-sm text-gray-500 mb-2">你的答案:</p>
                  <pre className="text-sm bg-gray-50 p-3 rounded-lg whitespace-pre-wrap">{ans?.userAnswer || '(未作答)'}</pre>
                </div>
              )}

              {ans?.feedback && (
                <div className="mt-3 text-sm text-gray-600 bg-blue-50 p-3 rounded-lg">
                  <span className="font-medium">批改反馈: </span>{ans.feedback}
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
