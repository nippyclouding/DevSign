import React, { useState } from 'react';
import { AuthProvider, useAuth } from './components/AuthContext';
import { Layout } from './components/Layout';
import { MainPage } from './pages/MainPage';
import { AuthPage } from './pages/AuthPage';
import { MyPage } from './pages/MyPage';
import { CreatePostPage } from './pages/CreatePostPage';
import { ChatPage } from './pages/ChatPage';

const AppContent: React.FC = () => {
  const [currentPage, setCurrentPage] = useState('main');
  const [currentPostId, setCurrentPostId] = useState<number | null>(null);
  const { isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F5F5F5]">
        <div className="w-12 h-12 border-4 border-black border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const renderPage = () => {
    switch (currentPage) {
      case 'main':
        return <MainPage onNavigate={setCurrentPage} onGoToChat={(id) => { setCurrentPostId(id); setCurrentPage('chat'); }} />;
      case 'auth':
        return <AuthPage onAuthSuccess={() => setCurrentPage('main')} />;
      case 'mypage':
        return <MyPage onGoToChat={(id) => { setCurrentPostId(id); setCurrentPage('chat'); }} />;
      case 'create':
        return <CreatePostPage onSuccess={() => setCurrentPage('main')} />;
      case 'chat':
        return currentPostId ? <ChatPage postId={currentPostId} onBack={() => setCurrentPage('main')} /> : <MainPage onNavigate={setCurrentPage} onGoToChat={(id) => { setCurrentPostId(id); setCurrentPage('chat'); }} />;
      default:
        return <MainPage onNavigate={setCurrentPage} onGoToChat={(id) => { setCurrentPostId(id); setCurrentPage('chat'); }} />;
    }
  };

  return (
    <Layout onNavigate={setCurrentPage} currentPage={currentPage}>
      {renderPage()}
    </Layout>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
