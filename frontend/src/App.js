import React, { useState, useEffect } from 'react';
import './App.css';
import ChartList from './components/ChartList';
import CrawlerControl from './components/CrawlerControl';
import { Music, TrendingUp, Crown, Database, Activity } from 'lucide-react';
import { motion } from 'framer-motion';
import { songApi } from './services/api';

function App() {
  const [activeTab, setActiveTab] = useState('billboard-japan');
  const [stats, setStats] = useState({ totalSongs: 0, charts: 2 });

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await songApi.getAllSongs();
      setStats({
        totalSongs: response.data.length,
        charts: 2
      });
    } catch (error) {
      console.error('통계 로딩 실패:', error);
    }
  };

  const renderContent = () => {
    if (activeTab === 'crawler') {
      return <CrawlerControl />;
    }
    return <ChartList chartName={activeTab} onDataUpdate={fetchStats} />;
  };

  const tabs = [
    { 
      id: 'billboard-japan', 
      label: 'Billboard Japan', 
      icon: '🎯'
    },
    { 
      id: 'oricon', 
      label: 'Oricon', 
      icon: '🏆'
    },
    { 
      id: 'crawler', 
      label: '크롤링 관리', 
      icon: '⚙️'
    }
  ];

  return (
      <div className="App min-h-screen bg-gray-50">
        {/* 히어로 헤더 */}
        <header className="relative overflow-hidden bg-gradient-to-br from-purple-600 via-blue-600 to-indigo-600">
          <div className="relative z-10 text-white px-8 py-12">
            <div className="max-w-6xl mx-auto text-center">
              <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6 }}
              >
                <div className="flex items-center justify-center mb-6">
                  {/* 로고 이미지와 텍스트를 함께 */}
                  <motion.div
                    className="flex items-center"
                    animate={{ 
                      scale: [1, 1.02, 1]
                    }}
                    transition={{ duration: 3, repeat: Infinity, repeatDelay: 2 }}
                  >
                    {/* 로고 이미지 (있으면 표시) */}
                    <div 
                      className="w-20 h-20 mr-4 bg-contain bg-center bg-no-repeat"
                      style={{
                        backgroundImage: "url('/logo.png')",
                        filter: 'drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3))'
                      }}
                    />
                    
                    {/* 텍스트 로고 */}
                    <h1 className="text-6xl font-black bg-gradient-to-r from-white to-blue-200 bg-clip-text text-transparent">
                      JPop Ranker
                    </h1>
                  </motion.div>
                </div>
                <p className="text-2xl opacity-90 mb-12 font-light">
                  🎵 실시간 일본 음악 차트 랭킹 서비스 🎵
                </p>
              </motion.div>

              {/* 통계 카드들 - 가로 배열 */}
              <motion.div 
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.3 }}
                  style={{
                    display: 'flex',
                    flexDirection: 'row',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '24px',
                    flexWrap: 'wrap',
                    maxWidth: '800px',
                    margin: '0 auto 60px auto'
                  }}
              >
                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px',
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  backdropFilter: 'blur(10px)',
                  borderRadius: '16px',
                  padding: '16px',
                  boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                  minWidth: '110px'
                }}>
                  <TrendingUp className="text-yellow-300" size={24} />
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{stats.totalSongs}</div>
                    <div style={{ fontSize: '12px', opacity: 0.8, whiteSpace: 'nowrap' }}>총 등록곡</div>
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px',
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  backdropFilter: 'blur(10px)',
                  borderRadius: '16px',
                  padding: '16px',
                  boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                  minWidth: '110px'
                }}>
                  <Crown className="text-yellow-300" size={24} />
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{stats.charts}</div>
                    <div style={{ fontSize: '12px', opacity: 0.8, whiteSpace: 'nowrap' }}>지원 차트</div>
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px',
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  backdropFilter: 'blur(10px)',
                  borderRadius: '16px',
                  padding: '16px',
                  boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                  minWidth: '110px'
                }}>
                  <Activity className="text-green-300 animate-pulse" size={24} />
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold' }}>LIVE</div>
                    <div style={{ fontSize: '12px', opacity: 0.8, whiteSpace: 'nowrap' }}>실시간 업데이트</div>
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px',
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  backdropFilter: 'blur(10px)',
                  borderRadius: '16px',
                  padding: '16px',
                  boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)',
                  minWidth: '110px'
                }}>
                  <Database className="text-blue-300" size={24} />
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '24px', fontWeight: 'bold' }}>HOT</div>
                    <div style={{ fontSize: '12px', opacity: 0.8, whiteSpace: 'nowrap' }}>인기 차트</div>
                  </div>
                </div>
              </motion.div>
            </div>
          </div>
        </header>

        {/* 네비게이션 */}
        <nav className="bg-white shadow-xl sticky top-0 z-50">
          <div className="max-w-6xl mx-auto px-6 py-8">
            <div className="flex justify-center gap-4">
              {tabs.map((tab) => (
                <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    style={{
                      backgroundColor: activeTab === tab.id ? '#87CEEB' : '#F9FAFB',
                      color: activeTab === tab.id ? 'white' : '#374151',
                      padding: '12px 24px',
                      borderRadius: '12px',
                      fontWeight: '600',
                      border: 'none',
                      cursor: 'pointer',
                      boxShadow: activeTab === tab.id ? '0 4px 6px rgba(0, 0, 0, 0.1)' : '0 2px 4px rgba(0, 0, 0, 0.05)',
                      transform: activeTab === tab.id ? 'scale(1.05)' : 'scale(1)',
                      transition: 'all 0.3s ease'
                    }}
                    onMouseEnter={(e) => {
                      if (activeTab !== tab.id) {
                        e.target.style.backgroundColor = '#E0F2FE';
                        e.target.style.transform = 'scale(1.05) translateY(-2px)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (activeTab !== tab.id) {
                        e.target.style.backgroundColor = '#F9FAFB';
                        e.target.style.transform = 'scale(1) translateY(0px)';
                      }
                    }}
                >
                  <span style={{ marginRight: '8px' }}>{tab.icon}</span>
                  <span>{tab.label}</span>
                </button>
              ))}
            </div>
          </div>
        </nav>

        {/* 메인 컨텐츠 */}
        <main className="min-h-screen py-8">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.3 }}
          >
            {renderContent()}
          </motion.div>
        </main>

        {/* 푸터 */}
        <footer className="bg-gray-100 text-gray-600 py-8">
          <div className="text-center">
            <div className="flex items-center justify-center gap-2 mb-2">
              <Music size={20} className="text-purple-500" />
              <p className="text-lg font-semibold">© 2025 JPop Ranker</p>
            </div>
            <p className="text-sm opacity-75">Made with ❤️ for J-Pop lovers</p>
          </div>
        </footer>
      </div>
  );
}

export default App;