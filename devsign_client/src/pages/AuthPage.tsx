import React, { useState } from 'react';
import { useAuth } from '../components/AuthContext';
import { Mail, Lock, User, ArrowRight, Github } from 'lucide-react';
import { motion } from 'motion/react';
import { cn } from '../lib/utils';
import { Role } from '../types';

export const AuthPage: React.FC<{ onAuthSuccess: () => void }> = ({ onAuthSuccess }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState<Role>('developer');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    const endpoint = isLogin ? '/api/auth/login' : '/api/auth/signup';
    const body = isLogin ? { email, password } : { email, password, name, role };

    try {
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      if (res.ok) {
        login(data.token, data.user);
        onAuthSuccess();
      } else {
        alert(data.error);
      }
    } catch (err) {
      alert('Something went wrong');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto pt-12">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-[2.5rem] p-10 shadow-2xl shadow-black/5 border border-black/5"
      >
        <div className="text-center mb-10">
          <div className="w-16 h-16 bg-black rounded-2xl flex items-center justify-center mx-auto mb-6">
            <span className="text-white font-bold text-3xl">D</span>
          </div>
          <h2 className="text-3xl font-bold tracking-tight mb-2">
            {isLogin ? '다시 오신 것을 환영합니다' : '커뮤니티 가입'}
          </h2>
          <p className="text-gray-400 text-sm">
            {isLogin ? '계정에 로그인하세요' : '전문가 프로필을 생성하세요'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {!isLogin && (
            <>
              <div className="relative">
                <User className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input 
                  type="text" 
                  required
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="이름 (실명)" 
                  className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
                />
              </div>
              <div className="flex gap-2 p-1 bg-gray-50 rounded-2xl">
                <button 
                  type="button"
                  onClick={() => setRole('developer')}
                  className={cn(
                    "flex-1 py-3 rounded-xl text-xs font-bold uppercase tracking-widest transition-all",
                    role === 'developer' ? "bg-white shadow-sm text-black" : "text-gray-400"
                  )}
                >
                  개발자
                </button>
                <button 
                  type="button"
                  onClick={() => setRole('designer')}
                  className={cn(
                    "flex-1 py-3 rounded-xl text-xs font-bold uppercase tracking-widest transition-all",
                    role === 'designer' ? "bg-white shadow-sm text-black" : "text-gray-400"
                  )}
                >
                  디자이너
                </button>
              </div>
            </>
          )}
          
          <div className="relative">
            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input 
              type="email" 
              required
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="이메일 주소" 
              className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
            />
          </div>

          <div className="relative">
            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input 
              type="password" 
              required
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="비밀번호" 
              className="w-full bg-gray-50 border-none rounded-2xl pl-12 pr-4 py-4 text-sm focus:ring-2 focus:ring-black transition-all"
            />
          </div>

          <button 
            type="submit" 
            disabled={isLoading}
            className="w-full bg-black text-white py-4 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-black/80 transition-all disabled:opacity-50"
          >
            {isLoading ? '처리 중...' : isLogin ? '로그인' : '계정 생성'}
            {!isLoading && <ArrowRight size={18} />}
          </button>
        </form>

        <div className="mt-8 pt-8 border-t border-black/5 text-center">
          <button 
            onClick={() => setIsLogin(!isLogin)}
            className="text-sm font-bold text-gray-400 hover:text-black transition-colors"
          >
            {isLogin ? "계정이 없으신가요? 회원가입" : "이미 계정이 있으신가요? 로그인"}
          </button>
        </div>
      </motion.div>
    </div>
  );
};
