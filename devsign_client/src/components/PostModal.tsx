import React, { useState, useEffect } from 'react';
import { Post, Application, Membership, ApiResponse } from '../types';
import { useAuth } from './AuthContext';
import { X, Calendar, User, Check, XCircle, MessageSquare, ExternalLink, Trash2 } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from '../lib/utils';
import { UserProfileModal } from './UserProfileModal';
import { useNavigate } from 'react-router-dom';

interface PostModalProps {
  post: Post;
  onClose: () => void;
  onDeleted?: () => void;
}

interface ReviewForm {
  content: string;
  rating: number;
}

export const PostModal: React.FC<PostModalProps> = ({ post, onClose, onDeleted }) => {
  const { user, token } = useAuth();
  const navigate = useNavigate();

  // Full detail (for content field missing in summary)
  const [detail, setDetail] = useState<Post | null>(null);

  const [applications, setApplications] = useState<Application[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [activeTab, setActiveTab] = useState<'details' | 'applicants'>('details');
  const [membership, setMembership] = useState<Membership>({
    is_author: false,
    is_approved: false,
    application_status: null,
    application_id: null,
  });
  const [postStatus, setPostStatus] = useState(post.status);
  const [reviewForms, setReviewForms] = useState<Record<number, ReviewForm>>({});

  const isAuthor = membership.is_author;
  const isApproved = membership.is_approved;
  const canChat = isAuthor || isApproved;
  const appStatus = membership.application_status; // 'pending' | 'approved' | 'rejected' | null
  const canReview = postStatus === 'completed' && (isAuthor || isApproved);

  useEffect(() => {
    fetchDetail();
    if (user) {
      fetchMembership();
    }
  }, [post.id, user]);

  useEffect(() => {
    // Only fetch applicants if author or approved (avoids unnecessary 403)
    if (user && (membership.is_author || membership.is_approved)) {
      fetchApplications();
    }
  }, [membership.is_author, membership.is_approved]);

  const fetchDetail = async () => {
    const res = await fetch(`/api/projects/${post.id}`);
    if (res.ok) {
      const json: ApiResponse<Post> = await res.json();
      if (json.success) {
        setDetail(json.data);
        setPostStatus(json.data.status);
      }
    }
  };

  const fetchMembership = async () => {
    const res = await fetch(`/api/projects/${post.id}/membership`, {
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (res.ok) {
      const json: ApiResponse<Membership> = await res.json();
      if (json.success) setMembership(json.data);
    }
  };

  const fetchApplications = async () => {
    const res = await fetch(`/api/projects/${post.id}/applicants`, {
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (res.ok) {
      const json: ApiResponse<Application[]> = await res.json();
      if (json.success) setApplications(json.data);
    }
  };

  const handleApply = async () => {
    if (!user) return alert('로그인이 필요합니다');
    const res = await fetch(`/api/projects/${post.id}/apply`, {
      method: 'POST',
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const json: ApiResponse<unknown> = await res.json();
    if (res.ok && json.success) {
      await fetchMembership();
      alert('신청이 완료되었습니다!');
    } else {
      alert(json.message || '신청 실패');
    }
  };

  const handleCancelApplication = async () => {
    if (!membership.application_id) return;
    if (!confirm('신청을 취소하시겠습니까?')) return;
    const res = await fetch(`/api/applicants/${membership.application_id}`, {
      method: 'DELETE',
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (res.ok) {
      await fetchMembership();
    } else {
      alert('신청 취소 실패');
    }
  };

  const handleUpdateStatus = async (appId: number, status: string) => {
    const res = await fetch(`/api/applicants/${appId}/status`, {
      method: 'PATCH',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ status }),
    });
    if (res.ok) fetchApplications();
  };

  const handlePostStatusUpdate = async (newStatus: string) => {
    const res = await fetch(`/api/projects/${post.id}/status`, {
      method: 'PATCH',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ status: newStatus }),
    });
    if (res.ok) {
      setPostStatus(newStatus as any);
    } else {
      const json = await res.json();
      alert(json.message || '상태 변경 실패');
    }
  };

  const handleDeleteProject = async () => {
    if (!confirm('프로젝트를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
    const res = await fetch(`/api/projects/${post.id}`, {
      method: 'DELETE',
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (res.ok || res.status === 204) {
      onDeleted?.();
    } else {
      const json = await res.json().catch(() => ({}));
      alert((json as any).message || '삭제 실패');
    }
  };

  const setReviewForm = (targetId: number, patch: Partial<ReviewForm>) => {
    setReviewForms(prev => ({
      ...prev,
      [targetId]: { content: '', rating: 5, ...prev[targetId], ...patch },
    }));
  };

  const handleSubmitReview = async (revieweeId: number) => {
    const form = reviewForms[revieweeId];
    if (!form?.content?.trim()) return;
    const res = await fetch('/api/reviews', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        post_id: post.id,
        reviewee_id: revieweeId,
        content: form.content,
        rating: form.rating,
      }),
    });
    if (res.ok) {
      alert('리뷰가 제출되었습니다!');
      setReviewForms(prev => { const n = { ...prev }; delete n[revieweeId]; return n; });
    } else {
      const json = await res.json().catch(() => ({}));
      alert((json as any).message || '리뷰 제출 실패');
    }
  };

  const renderReviewForm = (targetId: number) => {
    const form = reviewForms[targetId] ?? { content: '', rating: 5 };
    return (
      <div className="mt-4 pt-4 border-t border-black/5 space-y-3">
        <p className="text-xs font-bold uppercase tracking-widest opacity-50">평판 리뷰 작성</p>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="이 분과의 협업은 어땠나요?"
            value={form.content}
            onChange={e => setReviewForm(targetId, { content: e.target.value })}
            className="flex-1 bg-white border-none rounded-xl px-4 py-2 text-sm focus:ring-1 focus:ring-black"
          />
          <select
            value={form.rating}
            onChange={e => setReviewForm(targetId, { rating: parseInt(e.target.value) })}
            className="bg-white border-none rounded-xl px-2 py-2 text-sm"
          >
            <option value="5">5 Stars</option>
            <option value="4">4 Stars</option>
            <option value="3">3 Stars</option>
            <option value="2">2 Stars</option>
            <option value="1">1 Star</option>
          </select>
          <button
            onClick={() => handleSubmitReview(targetId)}
            className="bg-black text-white px-4 py-2 rounded-xl text-xs font-bold"
          >
            제출
          </button>
        </div>
      </div>
    );
  };

  // Use detail data when available, fall back to summary
  const displayPost = detail ?? post;
  const content = detail?.content;

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, y: 20 }}
        animate={{ scale: 1, y: 0 }}
        exit={{ scale: 0.95, y: 20 }}
        className="bg-white w-full max-w-4xl max-h-[90vh] rounded-3xl overflow-hidden shadow-2xl flex flex-col"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="p-6 border-b border-black/5 flex justify-between items-start">
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold tracking-tight">{displayPost.main_title}</h2>
              <div className="flex gap-1">
                {displayPost.needed_developers > 0 && (
                  <span className="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest bg-emerald-100 text-emerald-600">
                    개발자 {displayPost.needed_developers}명
                  </span>
                )}
                {displayPost.needed_designers > 0 && (
                  <span className="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest bg-indigo-100 text-indigo-600">
                    디자이너 {displayPost.needed_designers}명
                  </span>
                )}
                <span className={cn(
                  "px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest",
                  postStatus === 'recruiting' ? "bg-blue-100 text-blue-600" :
                  postStatus === 'progress' ? "bg-orange-100 text-orange-600" :
                  "bg-gray-100 text-gray-600"
                )}>
                  {postStatus === 'recruiting' ? '모집 중' : postStatus === 'progress' ? '진행 중' : '완료됨'}
                </span>
              </div>
            </div>
            <p className="text-gray-500">{displayPost.subtitle}</p>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={24} />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-black/5 px-6">
          <button
            onClick={() => setActiveTab('details')}
            className={cn(
              "px-4 py-4 text-sm font-medium border-b-2 transition-all",
              activeTab === 'details' ? "border-black text-black" : "border-transparent text-gray-400 hover:text-gray-600"
            )}
          >
            상세 정보
          </button>
          <button
            onClick={() => setActiveTab('applicants')}
            className={cn(
              "px-4 py-4 text-sm font-medium border-b-2 transition-all",
              activeTab === 'applicants' ? "border-black text-black" : "border-transparent text-gray-400 hover:text-gray-600"
            )}
          >
            {isAuthor ? '신청자 목록' : '참여자 목록'} ({applications.length})
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {activeTab === 'details' && (
            <div className="space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div className="flex items-center gap-3 text-gray-600">
                    <Calendar size={20} className="text-black" />
                    <div>
                      <p className="text-xs uppercase tracking-wider font-bold opacity-50">진행 기간</p>
                      <p className="font-medium">{displayPost.start_date} ~ {displayPost.end_date}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 text-gray-600">
                    <User size={20} className="text-black" />
                    <div>
                      <p className="text-xs uppercase tracking-wider font-bold opacity-50">모집자</p>
                      <p className="font-medium">{displayPost.author_name} ({displayPost.author_role === 'designer' ? '디자이너' : '개발자'})</p>
                    </div>
                  </div>
                </div>

                {isAuthor && (
                  <div className="bg-gray-50 p-4 rounded-2xl space-y-3">
                    <p className="text-xs font-bold uppercase tracking-widest opacity-50">프로젝트 상태 관리</p>
                    <div className="flex gap-2">
                      {(['recruiting', 'progress', 'completed'] as const).map(s => (
                        <button
                          key={s}
                          onClick={() => handlePostStatusUpdate(s)}
                          disabled={postStatus === s || postStatus === 'completed' || (postStatus === 'progress' && s === 'recruiting')}
                          className={cn("flex-1 py-2 rounded-xl text-[10px] font-bold uppercase transition-all disabled:cursor-not-allowed",
                            postStatus === s ? "bg-black text-white" : "bg-white text-gray-400 border border-black/5 hover:bg-gray-100")}
                        >
                          {s === 'recruiting' ? '모집 중' : s === 'progress' ? '진행 중' : '완료됨'}
                        </button>
                      ))}
                    </div>
                    {postStatus === 'recruiting' && (
                      <button
                        onClick={handleDeleteProject}
                        className="w-full flex items-center justify-center gap-2 py-2 rounded-xl text-[10px] font-bold uppercase bg-red-50 text-red-500 hover:bg-red-100 transition-all border border-red-100"
                      >
                        <Trash2 size={12} /> 프로젝트 삭제
                      </button>
                    )}
                  </div>
                )}
              </div>

              <div className="space-y-4">
                <h3 className="text-lg font-bold">일정 및 진행 방식</h3>
                <div className="bg-gray-50 p-6 rounded-2xl whitespace-pre-wrap leading-relaxed text-gray-700 min-h-[80px]">
                  {content === undefined ? (
                    <div className="h-8 bg-gray-200 rounded animate-pulse w-3/4" />
                  ) : content || '내용 없음'}
                </div>
              </div>

              <div className="pt-4 flex flex-col gap-3">
                {canChat && (
                  <button
                    onClick={() => { onClose(); navigate(`/chat/${post.id}`); }}
                    className="w-full bg-emerald-500 text-white py-4 rounded-2xl font-bold hover:bg-emerald-600 transition-all shadow-lg shadow-emerald-500/10 flex items-center justify-center gap-2"
                  >
                    <MessageSquare size={20} /> 그룹 채팅방 입장
                  </button>
                )}

                {!isAuthor && !isApproved && (
                  <>
                    {!user ? (
                      <button
                        onClick={() => alert('프로젝트 신청을 위해 로그인이 필요합니다.')}
                        className="w-full bg-gray-200 text-gray-500 py-4 rounded-2xl font-bold cursor-not-allowed"
                      >
                        로그인 후 신청 가능
                      </button>
                    ) : appStatus === 'pending' ? (
                      <div className="flex gap-3">
                        <div className="flex-1 bg-gray-100 text-gray-500 py-4 px-6 rounded-2xl text-center font-medium">
                          신청 대기 중...
                        </div>
                        <button
                          onClick={handleCancelApplication}
                          className="px-6 py-4 rounded-2xl font-bold text-sm bg-red-50 text-red-500 hover:bg-red-100 transition-all border border-red-100"
                        >
                          신청 취소
                        </button>
                      </div>
                    ) : appStatus === 'rejected' ? (
                      <div className="bg-red-50 text-red-400 py-4 px-6 rounded-2xl text-center font-medium text-sm border border-red-100">
                        지원이 거절되었습니다
                      </div>
                    ) : postStatus !== 'recruiting' ? (
                      <div className="bg-gray-100 text-gray-400 py-4 px-6 rounded-2xl text-center font-medium text-sm">
                        모집이 마감되었습니다
                      </div>
                    ) : (
                      <button
                        onClick={handleApply}
                        className="w-full bg-black text-white py-4 rounded-2xl font-bold hover:bg-black/80 transition-all shadow-lg shadow-black/10"
                      >
                        참여 신청하기
                      </button>
                    )}
                  </>
                )}
              </div>
            </div>
          )}

          {activeTab === 'applicants' && (
            <div className="space-y-4">
              {/* Approved member reviewing author */}
              {isApproved && !isAuthor && canReview && (
                <div className="bg-gray-50 p-6 rounded-2xl">
                  <div className="flex gap-4 items-center">
                    <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center border border-black/5 font-bold text-xl">
                      {displayPost.author_name[0]}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="font-bold">{displayPost.author_name}</h4>
                        <span className="text-[10px] font-bold uppercase tracking-widest bg-black text-white px-2 py-0.5 rounded-full">팀장</span>
                      </div>
                      <p className="text-sm text-gray-500">
                        {displayPost.author_role === 'designer' ? '디자이너' : '개발자'}
                      </p>
                    </div>
                  </div>
                  {renderReviewForm(displayPost.author_id)}
                </div>
              )}

              {applications.length === 0 ? (
                <div className="text-center py-12 text-gray-400">
                  아직 신청자가 없습니다.
                </div>
              ) : (
                applications.map(app => (
                  <div key={app.id} className="bg-gray-50 p-6 rounded-2xl flex flex-col gap-4">
                    <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                      <div
                        className="flex gap-4 items-center cursor-pointer group/user"
                        onClick={() => setSelectedUserId(app.applicant_id)}
                      >
                        <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center border border-black/5 font-bold text-xl group-hover/user:bg-black group-hover/user:text-white transition-all">
                          {app.applicant_name[0]}
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <h4 className="font-bold group-hover/user:text-black">{app.applicant_name}</h4>
                            <ExternalLink size={12} className="text-gray-300 group-hover/user:text-black" />
                          </div>
                          <p className="text-sm text-gray-500 capitalize">
                            {app.applicant_role === 'designer' ? '디자이너' : '개발자'} • 평판 점수: {app.applicant_reputation}
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-2 w-full md:w-auto">
                        {app.status === 'pending' ? (
                          isAuthor ? (
                            <>
                              <button
                                onClick={() => handleUpdateStatus(app.id, 'approved')}
                                className="flex-1 md:flex-none flex items-center justify-center gap-2 bg-black text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-black/80 transition-all"
                              >
                                <Check size={16} /> 승인
                              </button>
                              <button
                                onClick={() => handleUpdateStatus(app.id, 'rejected')}
                                className="flex-1 md:flex-none flex items-center justify-center gap-2 border border-red-200 text-red-500 px-4 py-2 rounded-xl text-sm font-bold hover:bg-red-50 transition-all"
                              >
                                <XCircle size={16} /> 거절
                              </button>
                            </>
                          ) : null
                        ) : (
                          <span className={cn(
                            "px-4 py-2 rounded-xl text-sm font-bold uppercase tracking-wider",
                            app.status === 'approved' ? "bg-green-100 text-green-600" : "bg-red-100 text-red-600"
                          )}>
                            {app.status === 'approved' ? '승인됨' : '거절됨'}
                          </span>
                        )}
                      </div>
                    </div>

                    {canReview && app.status === 'approved' && app.applicant_id !== user?.id && (
                      renderReviewForm(app.applicant_id)
                    )}
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </motion.div>

      <AnimatePresence>
        {selectedUserId && (
          <UserProfileModal
            userId={selectedUserId}
            onClose={() => setSelectedUserId(null)}
          />
        )}
      </AnimatePresence>
    </motion.div>
  );
};
