import React, { useState, useEffect } from 'react';
import { User, Award, X } from 'lucide-react';
import { motion } from 'motion/react';
import { User as UserType, Review } from '../types';

interface UserProfileModalProps {
  userId: number;
  onClose: () => void;
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({ userId, onClose }) => {
  const [user, setUser] = useState<UserType | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const [userRes, reviewsRes] = await Promise.all([
          fetch(`/api/users/${userId}`),
          fetch(`/api/users/${userId}/reviews`)
        ]);
        if (userRes.ok) setUser(await userRes.json());
        if (reviewsRes.ok) setReviews(await reviewsRes.json());
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchUserData();
  }, [userId]);

  if (isLoading) return null;
  if (!user) return null;

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.div 
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        className="bg-white w-full max-w-2xl rounded-[2.5rem] overflow-hidden shadow-2xl flex flex-col max-h-[80vh]"
        onClick={e => e.stopPropagation()}
      >
        <div className="p-8 border-b border-black/5 flex justify-between items-start">
          <div className="flex gap-6 items-center">
            <div className="w-20 h-20 bg-black rounded-3xl flex items-center justify-center text-white text-3xl font-bold">
              {user.name[0]}
            </div>
            <div>
              <h2 className="text-2xl font-bold tracking-tight">{user.name}</h2>
              <p className="text-gray-400 font-medium uppercase tracking-widest text-[10px] mt-1">
                {user.role === 'designer' ? '디자이너' : '개발자'}
              </p>
              <div className="flex items-center gap-2 mt-2 bg-yellow-50 text-yellow-700 px-3 py-1 rounded-full w-fit border border-yellow-100">
                <Award size={14} />
                <span className="text-xs font-bold">평판 점수: {user.reputation}</span>
              </div>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={20} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-8 space-y-8">
          <div className="space-y-3">
            <h3 className="text-sm font-bold uppercase tracking-widest opacity-40">포트폴리오 / 소개</h3>
            <div className="bg-gray-50 p-6 rounded-2xl text-gray-600 text-sm leading-relaxed whitespace-pre-wrap">
              {user.profile_data || "이 사용자는 아직 소개를 등록하지 않았습니다."}
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="text-sm font-bold uppercase tracking-widest opacity-40">최근 평판 리뷰</h3>
            {reviews.length === 0 ? (
              <p className="text-gray-400 text-xs italic">아직 리뷰가 없습니다.</p>
            ) : (
              <div className="space-y-4">
                {reviews.map(review => (
                  <div key={review.id} className="bg-gray-50 p-4 rounded-2xl border border-black/5">
                    <div className="flex justify-between mb-2">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-tighter">
                        From {review.reviewer_name} • 프로젝트: {review.project_title}
                      </span>
                      <div className="flex gap-0.5">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <Award key={i} size={10} className={i < review.rating ? "text-yellow-500 fill-yellow-500" : "text-gray-200"} />
                        ))}
                      </div>
                    </div>
                    <p className="text-sm text-gray-700">"{review.content}"</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </motion.div>
    </motion.div>
  );
};
