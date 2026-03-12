import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './components/AuthContext';
import { Layout } from './components/Layout';
import { MainPage } from './pages/MainPage';
import { AuthPage } from './pages/AuthPage';
import { MyPage } from './pages/MyPage';
import { CreatePostPage } from './pages/CreatePostPage';
import { ChatPage } from './pages/ChatPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<MainPage />} />
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="/create" element={<CreatePostPage />} />
            <Route path="/chat/:postId" element={<ChatPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </AuthProvider>
  );
}
