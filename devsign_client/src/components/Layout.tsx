import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { LogOut, User as UserIcon, Home, PlusSquare } from 'lucide-react';
import { cn } from '../lib/utils';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-[#F5F5F5] text-[#1A1A1A] font-sans">
      <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-black/5 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-black rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xl">D</span>
              </div>
              <span className="font-bold text-xl tracking-tight">DevSign</span>
            </Link>

            <div className="flex items-center gap-6">
              <Link
                to="/"
                className={cn(
                  "flex items-center gap-2 text-sm font-medium transition-colors hover:text-black",
                  location.pathname === '/' ? "text-black" : "text-gray-500"
                )}
              >
                <Home size={18} />
                <span className="hidden sm:inline">홈</span>
              </Link>

              {user ? (
                <>
                  <Link
                    to="/create"
                    className={cn(
                      "flex items-center gap-2 text-sm font-medium transition-colors hover:text-black",
                      location.pathname === '/create' ? "text-black" : "text-gray-500"
                    )}
                  >
                    <PlusSquare size={18} />
                    <span className="hidden sm:inline">모집하기</span>
                  </Link>
                  <Link
                    to="/mypage"
                    className={cn(
                      "flex items-center gap-2 text-sm font-medium transition-colors hover:text-black",
                      location.pathname === '/mypage' ? "text-black" : "text-gray-500"
                    )}
                  >
                    <UserIcon size={18} />
                    <span className="hidden sm:inline">마이페이지</span>
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 text-sm font-medium text-red-500 hover:text-red-600 transition-colors"
                  >
                    <LogOut size={18} />
                    <span className="hidden sm:inline">로그아웃</span>
                  </button>
                </>
              ) : (
                <Link
                  to="/auth"
                  className="bg-black text-white px-4 py-2 rounded-full text-sm font-medium hover:bg-black/80 transition-all"
                >
                  로그인 / 회원가입
                </Link>
              )}
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
};
