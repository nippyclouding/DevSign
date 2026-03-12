import React, { useState, useEffect } from 'react';
import { Post, ProjectStats, ApiResponse } from '../types';
import { PostModal } from '../components/PostModal';
import { ChevronLeft, ChevronRight, Search, ArrowRight } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from '../lib/utils';
import { format } from 'date-fns';
import { useAuth } from '../components/AuthContext';
import { useNavigate } from 'react-router-dom';

interface PageData {
  content: Post[];
  total_pages: number;
}

export const MainPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState('all');
  const [sectionFilter, setSectionFilter] = useState('all');
  const [keyword, setKeyword] = useState('');
  const [debouncedKeyword, setDebouncedKeyword] = useState('');
  const [totalPages, setTotalPages] = useState(1);
  const [selectedPost, setSelectedPost] = useState<Post | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [stats, setStats] = useState<ProjectStats>({ active_projects: 0, today_projects: 0 });

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedKeyword(keyword), 400);
    return () => clearTimeout(timer);
  }, [keyword]);

  useEffect(() => {
    setPage(1);
  }, [statusFilter, sectionFilter, debouncedKeyword]);

  useEffect(() => {
    fetchPosts();
  }, [page, statusFilter, sectionFilter, debouncedKeyword]);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const res = await fetch('/api/projects/stats');
      const json: ApiResponse<ProjectStats> = await res.json();
      if (json.success) setStats(json.data);
    } catch {}
  };

  const fetchPosts = async () => {
    setIsLoading(true);
    try {
      const params = new URLSearchParams({ page: String(page - 1), size: '10' });
      if (statusFilter !== 'all') params.set('status', statusFilter.toUpperCase());
      if (sectionFilter !== 'all') params.set('section', sectionFilter.toUpperCase());
      if (debouncedKeyword) params.set('keyword', debouncedKeyword);
      const res = await fetch(`/api/projects?${params}`);
      const json: ApiResponse<PageData> = await res.json();
      if (json.success) {
        setPosts(json.data.content);
        setTotalPages(json.data.total_pages);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <div className="bg-black rounded-[2rem] p-8 md:p-12 text-white overflow-hidden relative">
        <div className="relative z-10 max-w-2xl">
          <h1 className="text-4xl md:text-6xl font-bold tracking-tight leading-none mb-6">
            Find Your <br />
            <span className="text-gray-400 italic font-serif">Perfect Team</span>
          </h1>
          <p className="text-gray-400 text-lg mb-8 max-w-md">
            재능 있는 디자이너, 개발자들과 협업하여 멋진 포트폴리오를 만들고 공모전에서 우승하세요.
          </p>
          <div className="flex gap-4 mb-8">
            {user && (
              <button
                onClick={() => navigate('/create')}
                className="bg-white text-black px-8 py-4 rounded-2xl font-bold hover:bg-gray-100 transition-all flex items-center gap-2"
              >
                프로젝트 시작하기 <ArrowRight size={20} />
              </button>
            )}
          </div>
          <div className="flex gap-4">
            <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 flex-1 border border-white/10">
              <p className="text-xs font-bold uppercase tracking-widest opacity-50 mb-1">Active Projects</p>
              <p className="text-2xl font-bold">{stats.active_projects}</p>
            </div>
            <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 flex-1 border border-white/10">
              <p className="text-xs font-bold uppercase tracking-widest opacity-50 mb-1">New Today</p>
              <p className="text-2xl font-bold">{stats.today_projects}</p>
            </div>
          </div>
        </div>
        <div className="absolute top-0 right-0 w-1/2 h-full opacity-20 pointer-events-none">
          <div className="absolute inset-0 bg-gradient-to-l from-black to-transparent z-10" />
          <img
            src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=800&q=80"
            alt="Collaboration"
            className="w-full h-full object-cover grayscale"
            referrerPolicy="no-referrer"
          />
        </div>
      </div>

      {/* Filter Bar */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-center">
        <div className="relative w-full md:w-96">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="프로젝트 검색..."
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
            className="w-full bg-white border-none rounded-2xl pl-12 pr-4 py-4 shadow-sm focus:ring-2 focus:ring-black transition-all"
          />
        </div>
        <div className="flex gap-2 w-full md:w-auto overflow-x-auto pb-2 md:pb-0">
          {(['all', 'recruiting', 'progress', 'completed'] as const).map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={cn(
                "px-6 py-3 rounded-2xl text-sm font-bold transition-all whitespace-nowrap",
                statusFilter === s ? "bg-black text-white" : "bg-white text-gray-500 border border-black/5"
              )}
            >
              {s === 'all' ? '모든 프로젝트' : s === 'recruiting' ? '모집 중' : s === 'progress' ? '진행 중' : '완료됨'}
            </button>
          ))}
        </div>
      </div>

      {/* Section Filter */}
      <div className="flex gap-2">
        {[['all', '전체'], ['developer', '개발자 모집'], ['designer', '디자이너 모집']].map(([val, label]) => (
          <button
            key={val}
            onClick={() => setSectionFilter(val)}
            className={cn(
              "px-5 py-2 rounded-xl text-sm font-bold transition-all",
              sectionFilter === val ? "bg-black text-white" : "bg-white text-gray-500 border border-black/5"
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Post Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {isLoading ? (
          Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="bg-white rounded-3xl p-6 h-64 animate-pulse border border-black/5" />
          ))
        ) : posts.length === 0 ? (
          <div className="col-span-full py-20 text-center text-gray-400">
            프로젝트가 없습니다. 첫 번째 프로젝트를 시작해 보세요!
          </div>
        ) : (
          posts.map((post) => (
            <motion.div
              key={post.id}
              whileHover={{ y: -4 }}
              onClick={() => setSelectedPost(post)}
              className="bg-white rounded-[2rem] p-8 border border-black/5 shadow-sm hover:shadow-xl hover:shadow-black/5 transition-all cursor-pointer group flex flex-col justify-between h-full"
            >
              <div>
                <div className="flex justify-between items-start mb-6">
                  <div className="flex flex-col gap-2">
                    <div className="flex gap-1 flex-wrap">
                      {post.needed_developers > 0 && (
                        <span className="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest bg-emerald-100 text-emerald-600">
                          개발자 {post.needed_developers}명
                        </span>
                      )}
                      {post.needed_designers > 0 && (
                        <span className="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest bg-indigo-100 text-indigo-600">
                          디자이너 {post.needed_designers}명
                        </span>
                      )}
                    </div>
                    <span className={cn(
                      "w-fit px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-widest",
                      post.status === 'recruiting' ? "bg-blue-100 text-blue-600" :
                      post.status === 'progress' ? "bg-orange-100 text-orange-600" :
                      "bg-gray-100 text-gray-600"
                    )}>
                      {post.status === 'recruiting' ? '모집 중' : post.status === 'progress' ? '진행 중' : '완료됨'}
                    </span>
                  </div>
                  <span className="text-[10px] font-bold text-gray-300 uppercase tracking-widest">
                    {format(new Date(post.created_at), 'MMM dd')}
                  </span>
                </div>
                <h3 className="text-2xl font-bold leading-tight mb-2 group-hover:text-black transition-colors">
                  {post.main_title}
                </h3>
                <p className="text-gray-500 text-sm line-clamp-2 mb-6">
                  {post.subtitle}
                </p>
              </div>

              <div className="pt-6 border-t border-black/5 flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-xs font-bold">
                    {post.author_name[0]}
                  </div>
                  <span className="text-xs font-bold text-gray-600">{post.author_name}</span>
                </div>
                <div className="text-xs font-bold text-black group-hover:translate-x-1 transition-transform flex items-center gap-1">
                  상세보기 <ChevronRight size={14} />
                </div>
              </div>
            </motion.div>
          ))
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 pt-8">
          <button
            disabled={page === 1}
            onClick={() => setPage(p => p - 1)}
            className="p-3 rounded-2xl bg-white border border-black/5 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-all"
          >
            <ChevronLeft size={20} />
          </button>
          <span className="font-bold text-sm">
            {page} / {totalPages} 페이지
          </span>
          <button
            disabled={page === totalPages}
            onClick={() => setPage(p => p + 1)}
            className="p-3 rounded-2xl bg-white border border-black/5 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 transition-all"
          >
            <ChevronRight size={20} />
          </button>
        </div>
      )}

      {/* Modal */}
      <AnimatePresence>
        {selectedPost && (
          <PostModal
            post={selectedPost}
            onClose={() => setSelectedPost(null)}
            onDeleted={() => { setSelectedPost(null); fetchPosts(); fetchStats(); }}
          />
        )}
      </AnimatePresence>
    </div>
  );
};
