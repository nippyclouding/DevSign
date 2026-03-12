import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, ApiResponse } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (token: string, user: User) => void;
  logout: () => void;
  updateUser: (user: User) => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verify session via httpOnly cookie on mount
    fetch('/api/auth/me', { credentials: 'include' })
      .then(res => res.ok ? res.json() : null)
      .then((json: ApiResponse<User> | null) => {
        if (json?.data) setUser(json.data);
      })
      .catch(() => {})
      .finally(() => setIsLoading(false));
  }, []);

  // Auto-logout when in-memory token expires (fresh login case)
  useEffect(() => {
    if (!token) return;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const msUntilExpiry = payload.exp * 1000 - Date.now();
      if (msUntilExpiry <= 0) { logout(); return; }
      const timer = setTimeout(logout, msUntilExpiry);
      return () => clearTimeout(timer);
    } catch {}
  }, [token]);

  // Periodic session check for cookie-based sessions (page refresh case)
  useEffect(() => {
    if (!user) return;
    const interval = setInterval(async () => {
      const res = await fetch('/api/auth/me', { credentials: 'include' });
      if (res.status === 401) logout();
    }, 5 * 60 * 1000); // 5분마다 확인
    return () => clearInterval(interval);
  }, [user]);

  const login = (newToken: string, newUser: User) => {
    // Token kept in memory only (httpOnly cookie set by backend)
    setToken(newToken);
    setUser(newUser);
  };

  const logout = () => {
    fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }).catch(() => {});
    setToken(null);
    setUser(null);
  };

  const updateUser = (newUser: User) => {
    setUser(newUser);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, updateUser, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
