import React, { useState } from 'react';
import { useAuth } from '../components/AuthContext';
import { Send, Calendar, Type, AlignLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { ApiResponse } from '../types';
import { useNavigate } from 'react-router-dom';

export const CreatePostPage: React.FC = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [mainTitle, setMainTitle] = useState('');
  const [subtitle, setSubtitle] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [content, setContent] = useState('');
  const [neededDevelopers, setNeededDevelopers] = useState(0);
  const [neededDesigners, setNeededDesigners] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const res = await fetch('/api/projects', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({
          main_title: mainTitle,
          subtitle,
          start_date: startDate,
          end_date: endDate,
          content,
          needed_developers: neededDevelopers,
          needed_designers: neededDesigners,
        }),
      });
      const json: ApiResponse<unknown> = await res.json();
      if (res.ok && json.success) {
        navigate('/');
      } else {
        alert(json.message || '프로젝트 생성 실패');
      }
    } catch (err) {
      alert('오류가 발생했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-[2.5rem] p-10 border border-black/5 shadow-sm"
      >
        <div className="mb-10">
          <h2 className="text-3xl font-bold tracking-tight mb-2">모집 시작하기</h2>
          <p className="text-gray-400">다음 프로젝트를 위한 완벽한 파트너를 찾아보세요.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">메인 제목 (회사/이벤트)</label>
            <div className="relative">
              <Type className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                required
                value={mainTitle}
                onChange={e => setMainTitle(e.target.value)}
                placeholder="예: 엔트로픽 / 매일경제신문"
                className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">부제목 (프로젝트 이름)</label>
            <div className="relative">
              <AlignLeft className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
              <input
                type="text"
                required
                value={subtitle}
                onChange={e => setSubtitle(e.target.value)}
                placeholder="예: 해커톤 참가자 모집"
                className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">시작일</label>
              <div className="relative">
                <Calendar className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                  type="date"
                  required
                  value={startDate}
                  onChange={e => setStartDate(e.target.value)}
                  className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
                />
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">종료일</label>
              <div className="relative">
                <Calendar className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                  type="date"
                  required
                  value={endDate}
                  onChange={e => setEndDate(e.target.value)}
                  className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">모집 개발자 수</label>
              <input
                type="number"
                min="0"
                value={neededDevelopers}
                onChange={e => setNeededDevelopers(parseInt(e.target.value) || 0)}
                className="w-full bg-gray-50 border-none rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">모집 디자이너 수</label>
              <input
                type="number"
                min="0"
                value={neededDesigners}
                onChange={e => setNeededDesigners(parseInt(e.target.value) || 0)}
                className="w-full bg-gray-50 border-none rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">일정 및 준비 방식</label>
            <textarea
              required
              value={content}
              onChange={e => setContent(e.target.value)}
              rows={6}
              placeholder="프로젝트 일정, 준비 계획, 그리고 어떤 팀원을 찾고 있는지 상세히 적어주세요..."
              className="w-full bg-gray-50 border-none rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all resize-none"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-black text-white py-4 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-black/80 transition-all disabled:opacity-50 shadow-lg shadow-black/10"
          >
            {isLoading ? '게시 중...' : '모집 공고 올리기'}
            {!isLoading && <Send size={18} />}
          </button>
        </form>
      </motion.div>
    </div>
  );
};
