import React, { useState, useEffect, useRef } from 'react';
import { Post, Message, ApiResponse } from '../types';
import { useAuth } from '../components/AuthContext';
import { Send, ArrowLeft, MessageSquare } from 'lucide-react';
import { motion } from 'motion/react';
import { cn } from '../lib/utils';
import { format } from 'date-fns';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface ChatPageProps {
  postId: number;
  onBack: () => void;
}

export const ChatPage: React.FC<ChatPageProps> = ({ postId, onBack }) => {
  const { user, token } = useAuth();
  const [post, setPost] = useState<Post | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMsg, setNewMsg] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [groupChatId, setGroupChatId] = useState<number | null>(null);
  const stompClientRef = useRef<Client | null>(null);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchPostAndMessages();
    return () => {
      stompClientRef.current?.deactivate();
    };
  }, [postId]);

  // Connect STOMP once we have the groupChatId
  useEffect(() => {
    if (!groupChatId || !token) return;
    connectStomp(groupChatId);
  }, [groupChatId, token]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const fetchPostAndMessages = async () => {
    try {
      // Fetch project detail (includes group_chat_id)
      const postRes = await fetch(`/api/projects/${postId}`);
      if (postRes.ok) {
        const json: ApiResponse<Post> = await postRes.json();
        setPost(json.data);
        setGroupChatId(json.data.group_chat_id ?? null);
      }

      // Fetch chat history
      const msgRes = await fetch(`/api/projects/${postId}/messages`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (msgRes.ok) {
        const json: ApiResponse<Message[]> = await msgRes.json();
        setMessages(json.data);
      } else {
        alert('채팅방 접근 권한이 없습니다');
        onBack();
        return;
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const connectStomp = (chatId: number) => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/sub/chat/${chatId}`, (frame) => {
          const msg: Message = JSON.parse(frame.body);
          setMessages(prev => [...prev, msg]);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
      },
    });
    client.activate();
    stompClientRef.current = client;
  };

  const handleSendChat = () => {
    if (!newMsg.trim() || !stompClientRef.current?.connected || !groupChatId) return;
    stompClientRef.current.publish({
      destination: `/pub/chat/${groupChatId}`,
      body: JSON.stringify({ content: newMsg }),
    });
    setNewMsg('');
  };

  if (isLoading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-12 h-12 border-4 border-black border-t-transparent rounded-full animate-spin" />
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto h-[calc(100vh-12rem)] flex flex-col bg-white rounded-[2.5rem] shadow-2xl shadow-black/5 border border-black/5 overflow-hidden">
      {/* Header */}
      <div className="p-6 border-b border-black/5 flex items-center gap-4 bg-gray-50/50">
        <button onClick={onBack} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
          <ArrowLeft size={20} />
        </button>
        <div className="flex-1">
          <h2 className="text-xl font-bold tracking-tight">{post?.main_title}</h2>
          <p className="text-xs text-gray-400 font-medium uppercase tracking-widest">그룹 채팅 • {post?.subtitle}</p>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-8 space-y-6">
        {messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-gray-300 space-y-4">
            <MessageSquare size={48} />
            <p className="font-medium">대화를 시작해 보세요!</p>
          </div>
        ) : (
          messages.map((msg) => {
            const isMe = msg.user_id === user?.id;
            return (
              <div key={msg.id} className={cn("flex flex-col", isMe ? "items-end" : "items-start")}>
                <div className="flex items-center gap-2 mb-1 px-1">
                  <span className="text-[10px] font-bold uppercase tracking-tighter text-gray-400">
                    {msg.user_name} ({msg.user_role === 'designer' ? '디자이너' : '개발자'})
                  </span>
                </div>
                <div className={cn(
                  "max-w-[70%] px-5 py-3 rounded-2xl text-sm shadow-sm",
                  isMe ? "bg-black text-white rounded-tr-none" : "bg-gray-100 text-black rounded-tl-none"
                )}>
                  {msg.content}
                </div>
                <span className="text-[9px] text-gray-300 mt-1">
                  {format(new Date(msg.created_at), 'HH:mm')}
                </span>
              </div>
            );
          })
        )}
        <div ref={chatEndRef} />
      </div>

      {/* Input */}
      <div className="p-6 border-t border-black/5 bg-gray-50/50">
        <div className="flex gap-3">
          <input
            type="text"
            value={newMsg}
            onChange={e => setNewMsg(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSendChat()}
            placeholder="메시지를 입력하세요..."
            className="flex-1 bg-white border border-black/5 rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all shadow-sm"
          />
          <button
            onClick={handleSendChat}
            className="bg-black text-white p-4 rounded-2xl hover:bg-black/80 transition-all shadow-lg shadow-black/10"
          >
            <Send size={20} />
          </button>
        </div>
      </div>
    </div>
  );
};
