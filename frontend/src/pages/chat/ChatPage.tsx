import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { chatApi, jobApi } from '../../api'
import { getUser } from '../../utils/request'
import type { ChatMessage } from '../../types'
import { formatDate } from '../../utils/date'
import { Send, User } from 'lucide-react'

export default function ChatPage() {
  const { userId: paramUserId } = useParams<{ userId: string }>()
  const currentUser = getUser()
  const [contacts, setContacts] = useState<{ id: number; name: string }[]>([])
  const [activeChat, setActiveChat] = useState<number | null>(Number(paramUserId) || null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const bottomRef = useRef<HTMLDivElement>(null)
  const pollRef = useRef<ReturnType<typeof setInterval>>()

  useEffect(() => {
    chatApi.getContacts().then(async ids => {
      const enriched = await Promise.all(ids.map(async id => {
        try {
          const jobs = (await jobApi.myJobs ? jobApi.myJobs().then(r => r.content) : [])
          // Show as "招聘官" or "求职者" based on conversation context
          return { id, name: `用户 #${id}` }
        } catch { return { id, name: `用户 #${id}` } }
      }))
      setContacts(enriched)
    })
  }, [])

  useEffect(() => {
    if (activeChat) {
      loadMessages()
      pollRef.current = setInterval(loadMessages, 3000)
    }
    return () => { if (pollRef.current) clearInterval(pollRef.current) }
  }, [activeChat])

  const loadMessages = async () => {
    if (!activeChat) return
    try {
      const r = await chatApi.getHistory(activeChat)
      const msgs = r.content.slice().reverse()
      setMessages(prev => {
        if (prev.length === msgs.length && prev.length > 0) return prev
        return msgs
      })
    } catch { /* ignore poll errors */ }
  }

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async () => {
    if (!input.trim() || !activeChat) return
    const msg = await chatApi.send(activeChat, input)
    setMessages(prev => [...prev, msg])
    setInput('')
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex h-[calc(100vh-7rem)] bg-white rounded-xl shadow-sm overflow-hidden">
      <div className="w-60 border-r bg-gray-50 p-4">
        <h3 className="text-sm font-semibold text-gray-500 mb-3">联系人</h3>
        {contacts.map(c => (
          <button key={c.id} onClick={() => { setActiveChat(c.id); setMessages([]) }}
            className={`w-full text-left px-3 py-2 rounded-lg text-sm mb-1 transition-colors ${
              activeChat === c.id ? 'bg-blue-100 text-blue-700' : 'hover:bg-gray-100'
            }`}>
            <User size={14} className="inline mr-2" />
            {c.name}
          </button>
        ))}
        {contacts.length === 0 && (
          <p className="text-xs text-gray-400">暂无联系人，浏览岗位并联系HR开始对话</p>
        )}
      </div>

      <div className="flex-1 flex flex-col">
        {activeChat ? (
          <>
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map(m => (
                <div key={m.id} className={`flex ${m.senderId === currentUser?.userId ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[70%] rounded-xl px-4 py-2 ${
                    m.senderId === currentUser?.userId
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    <p className="text-sm whitespace-pre-wrap">{m.content}</p>
                    <p className={`text-xs mt-1 ${m.senderId === currentUser?.userId ? 'text-blue-200' : 'text-gray-400'}`}>
                      {formatDate(m.createdAt)}
                    </p>
                  </div>
                </div>
              ))}
              <div ref={bottomRef} />
            </div>
            <div className="border-t p-4 flex gap-2">
              <input
                type="text" value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="输入消息..."
                className="flex-1 px-4 py-2 border rounded-full outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button onClick={handleSend}
                className="p-2.5 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors">
                <Send size={18} />
              </button>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-400 text-center px-8">
            <div>
              <User size={48} className="mx-auto mb-4 text-gray-300" />
              <p>选择一个联系人开始聊天</p>
              <p className="text-xs mt-2 text-gray-300">
                在岗位详情页点击「联系HR」即可开始对话
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
