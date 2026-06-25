import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { interviewApi } from '../../api'
import type { InterviewReport } from '../../types'
import { Star, TrendingUp, MessageCircle, Briefcase } from 'lucide-react'

export default function InterviewReportPage() {
  const { id } = useParams<{ id: string }>()
  const [report, setReport] = useState<InterviewReport | null>(null)

  useEffect(() => {
    interviewApi.getReport(Number(id)).then(setReport).catch(() => {})
  }, [id])

  if (!report) return <div className="text-center py-20 text-gray-400">加载报告...</div>

  const strengths = report.strengths ? report.strengths.split(';').filter(Boolean) : []
  const weaknesses = report.weaknesses ? report.weaknesses.split(';').filter(Boolean) : []

  return (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-xl font-bold mb-6">面试评估报告</h2>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6 text-center">
        <div className="text-5xl font-bold text-blue-600 mb-2">{report.overallScore}</div>
        <p className="text-gray-500">综合评分</p>
        <div className="w-full bg-gray-200 rounded-full h-2.5 mt-4">
          <div className="bg-blue-600 h-2.5 rounded-full transition-all" style={{ width: `${report.overallScore}%` }} />
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h3 className="font-semibold mb-3 flex items-center gap-2"><Star size={16} className="text-green-500" />优势</h3>
        <ul className="space-y-2">
          {strengths.map((s, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
              <span className="text-green-500 mt-1">+</span>{s}
            </li>
          ))}
        </ul>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h3 className="font-semibold mb-3 flex items-center gap-2"><TrendingUp size={16} className="text-orange-500" />改进建议</h3>
        <ul className="space-y-2">
          {weaknesses.map((s, i) => (
            <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
              <span className="text-orange-500 mt-1">-</span>{s}
            </li>
          ))}
        </ul>
      </div>

      {report.suggestions && (
        <div className="bg-white rounded-xl shadow-sm p-6">
          <h3 className="font-semibold mb-3 flex items-center gap-2"><MessageCircle size={16} className="text-blue-500" />整体评价</h3>
          <p className="text-sm text-gray-700 whitespace-pre-wrap">{report.suggestions}</p>
        </div>
      )}
    </div>
  )
}
