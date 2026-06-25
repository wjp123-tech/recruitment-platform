import { useState, useEffect, useRef } from 'react'
import { resumeApi } from '../../api'
import type { Resume } from '../../types'
import { formatDate } from '../../utils/date'
import { Upload, FileText, Trash2 } from 'lucide-react'

export default function ResumePage() {
  const [resumes, setResumes] = useState<Resume[]>([])
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState<Resume | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)

  useEffect(() => { resumeApi.list().then(setResumes) }, [])

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const r = await resumeApi.upload(file)
      setResumes(prev => [...prev, r])
    } catch (err: any) {
      alert(err.message)
    } finally {
      setUploading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('确认删除该简历？')) return
    await resumeApi.delete(id)
    setResumes(prev => prev.filter(r => r.id !== id))
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold">我的简历</h2>
        <button onClick={() => fileRef.current?.click()}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Upload size={16} />{uploading ? '上传中...' : '上传简历'}
        </button>
        <input ref={fileRef} type="file" hidden accept=".pdf,.docx,.doc,.txt,.md"
          onChange={handleUpload} />
      </div>

      {resumes.length === 0 ? (
        <div className="text-center text-gray-400 py-20">
          <FileText size={48} className="mx-auto mb-4 text-gray-300" />
          还没有简历，请上传您的第一份简历
        </div>
      ) : (
        <div className="space-y-3">
          {resumes.map(r => (
            <div key={r.id} className="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
              <div className="flex-1 min-w-0">
                <h4 className="font-medium truncate">{r.fileName}</h4>
                <p className="text-xs text-gray-400 mt-1">上传于 {formatDate(r.createdAt)} · {r.status}</p>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <button onClick={() => setPreview(r)}
                  className="px-3 py-1.5 text-sm border rounded-lg hover:bg-gray-50 transition-colors">
                  预览
                </button>
                <button onClick={() => handleDelete(r.id)}
                  className="p-1.5 text-gray-400 hover:text-red-500 transition-colors">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {preview && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
          onClick={() => setPreview(null)}>
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-auto"
            onClick={e => e.stopPropagation()}>
            <h3 className="font-semibold mb-3">{preview.fileName}</h3>
            <pre className="text-sm text-gray-700 whitespace-pre-wrap bg-gray-50 p-4 rounded-lg">
              {preview.parsedText}
            </pre>
          </div>
        </div>
      )}
    </div>
  )
}
