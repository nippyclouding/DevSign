import React, { useState, useEffect } from 'react';
import { useAuth } from '../components/AuthContext';
import { Award, Edit3, Trash2, Save, Folder, Star } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from '../lib/utils';
import { Post, Review, ApiResponse } from '../types';
import { PostModal } from '../components/PostModal';

export interface MyPageProps {
  onGoToChat: (postId: number) => void;
}

interface MyProjectsData {
  created: Post[];
  joined: Post[];
}

export const MyPage: React.FC<MyPageProps> = ({ onGoToChat }) => {
  const { user, token, login, logout } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(user?.name || '');
  const [profileData, setProfileData] = useState(user?.profile_data || '');
  const [isLoading, setIsLoading] = useState(false);

  const [projects, setProjects] = useState<{ created: Post[], joined: Post[] }>({ created: [], joined: [] });
  const [reviews, setReviews] = useState<Review[]>([]);
  const [selectedPost, setSelectedPost] = useState<Post | null>(null);

  useEffect(() => {
    if (user && token) {
      fetchProjects();
      fetchReviews();
    }
  }, [user?.id]);

  const fetchProjects = async () => {
    const res = await fetch('/api/members/me/projects', {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) {
      const json: ApiResponse<MyProjectsData> = await res.json();
      if (json.success) setProjects(json.data);
    }
  };

  const fetchReviews = async () => {
    const res = await fetch(`/api/members/${user?.id}/reviews`);
    if (res.ok) {
      const json: ApiResponse<Review[]> = await res.json();
      if (json.success) setReviews(json.data);
    }
  };

  const handleSave = async () => {
    setIsLoading(true);
    try {
      const res = await fetch('/api/members/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ name, profile_data: profileData }),
      });
      if (res.ok) {
        login(token!, { ...user!, name, profile_data: profileData });
        setIsEditing(false);
      }
    } catch (err) {
      alert('프로필 저장 실패');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (!confirm('계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
    try {
      const res = await fetch('/api/members/me', {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) logout();
    } catch (err) {
      alert('계정 삭제 실패');
    }
  };

  if (!user) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Profile Header */}
      <div className="bg-white rounded-[2.5rem] p-10 border border-black/5 shadow-sm flex flex-col md:flex-row gap-8 items-center md:items-start text-center md:text-left">
        <div className="w-32 h-32 bg-black rounded-[2rem] flex items-center justify-center text-white text-5xl font-bold">
          {user.name[0]}
        </div>
        <div className="flex-1 space-y-4">
          <div className="flex flex-col md:flex-row justify-between items-center md:items-start gap-4">
            <div>
              <h2 className="text-3xl font-bold tracking-tight">{user.name}</h2>
              <p className="text-gray-400 font-medium uppercase tracking-widest text-xs mt-1">
                {user.role === 'designer' ? '디자이너' : '개발자'} • {user.email}
              </p>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setIsEditing(!isEditing)}
                className="p-3 rounded-2xl bg-gray-50 hover:bg-gray-100 transition-all border border-black/5"
              >
                <Edit3 size={20} />
              </button>
              <button
                onClick={handleDeleteAccount}
                className="p-3 rounded-2xl bg-red-50 text-red-500 hover:bg-red-100 transition-all border border-red-100"
              >
                <Trash2 size={20} />
              </button>
            </div>
          </div>

          <div className="flex flex-wrap gap-4 justify-center md:justify-start">
            <div className="bg-gray-50 px-6 py-3 rounded-2xl flex items-center gap-3 border border-black/5">
              <Award className="text-yellow-500" size={20} />
              <div>
                <p className="text-[10px] font-bold uppercase tracking-widest opacity-50">평판 점수</p>
                <p className="font-bold">{user.reputation}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Profile Details */}
      <div className="bg-white rounded-[2.5rem] p-10 border border-black/5 shadow-sm">
        <div className="flex justify-between items-center mb-8">
          <h3 className="text-xl font-bold">포트폴리오 & 소개</h3>
          {isEditing && (
            <button
              onClick={handleSave}
              disabled={isLoading}
              className="bg-black text-white px-6 py-2 rounded-xl text-sm font-bold flex items-center gap-2 hover:bg-black/80 transition-all disabled:opacity-50"
            >
              <Save size={16} /> {isLoading ? '저장 중...' : '변경 사항 저장'}
            </button>
          )}
        </div>

        {isEditing ? (
          <div className="space-y-6">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">표시 이름</label>
              <input
                type="text"
                value={name}
                onChange={e => setName(e.target.value)}
                className="w-full bg-gray-50 border-none rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest opacity-50 px-1">포트폴리오 / 자기소개</label>
              <textarea
                value={profileData}
                onChange={e => setProfileData(e.target.value)}
                rows={6}
                placeholder="경력, 작업물 링크, 또는 찾고 있는 협업 기회에 대해 공유해 주세요..."
                className="w-full bg-gray-50 border-none rounded-2xl px-6 py-4 text-sm focus:ring-2 focus:ring-black transition-all resize-none"
              />
            </div>
          </div>
        ) : (
          <div className="bg-gray-50 p-8 rounded-3xl min-h-[200px] whitespace-pre-wrap text-gray-600 leading-relaxed">
            {user.profile_data || "아직 프로필 정보가 없습니다. 편집 버튼을 눌러 포트폴리오를 공유해 보세요!"}
          </div>
        )}
      </div>

      {/* Projects Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-white rounded-[2.5rem] p-10 border border-black/5 shadow-sm">
          <div className="flex items-center gap-3 mb-8">
            <Folder className="text-black" size={24} />
            <h3 className="text-xl font-bold">내 프로젝트</h3>
          </div>

          <div className="space-y-4">
            {[...projects.created, ...projects.joined].length === 0 ? (
              <p className="text-gray-400 text-sm">참여 중인 프로젝트가 없습니다.</p>
            ) : (
              [...projects.created, ...projects.joined].map(project => (
                <div
                  key={project.id}
                  onClick={() => setSelectedPost(project)}
                  className="p-4 bg-gray-50 rounded-2xl border border-black/5 hover:border-black transition-all cursor-pointer group"
                >
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-bold group-hover:text-black">{project.main_title}</h4>
                    <span className={cn(
                      "text-[9px] font-bold uppercase tracking-widest px-2 py-1 rounded-full",
                      project.status === 'recruiting' ? "bg-blue-100 text-blue-600" :
                      project.status === 'progress' ? "bg-orange-100 text-orange-600" :
                      "bg-gray-100 text-gray-600"
                    )}>
                      {project.status === 'recruiting' ? '모집 중' : project.status === 'progress' ? '진행 중' : '완료됨'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 line-clamp-1">{project.subtitle}</p>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="bg-white rounded-[2.5rem] p-10 border border-black/5 shadow-sm">
          <div className="flex items-center gap-3 mb-8">
            <Star className="text-yellow-500" size={24} />
            <h3 className="text-xl font-bold">받은 평판 리뷰</h3>
          </div>

          <div className="space-y-6">
            {reviews.length === 0 ? (
              <p className="text-gray-400 text-sm">아직 리뷰가 없습니다.</p>
            ) : (
              reviews.map(review => (
                <div key={review.id} className="space-y-2 border-b border-black/5 pb-4 last:border-0">
                  <div className="flex justify-between items-start">
                    <div>
                      <p className="text-xs font-bold">{review.reviewer_name}</p>
                      <p className="text-[10px] text-gray-400 italic">프로젝트: {review.project_title}</p>
                    </div>
                    <div className="flex gap-0.5">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <Star
                          key={i}
                          size={10}
                          className={cn(i < review.rating ? "fill-yellow-500 text-yellow-500" : "text-gray-200")}
                        />
                      ))}
                    </div>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed">"{review.content}"</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {selectedPost && (
          <PostModal
            post={selectedPost}
            onClose={() => setSelectedPost(null)}
            onGoToChat={onGoToChat}
          />
        )}
      </AnimatePresence>
    </div>
  );
};
