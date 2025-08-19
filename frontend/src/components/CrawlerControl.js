import React, { useState } from 'react';
import { crawlerApi } from '../services/api';
import { Play, RefreshCw, Activity, CheckCircle, XCircle, Clock } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const CrawlerControl = () => {
  const [loading, setLoading] = useState({});
  const [status, setStatus] = useState('');
  const [lastCrawl, setLastCrawl] = useState(null);
  const [results, setResults] = useState([]);

  const addResult = (type, success, message) => {
    const newResult = {
      id: Date.now(),
      type,
      success,
      message,
      timestamp: new Date().toLocaleTimeString('ko-KR')
    };
    setResults(prev => [newResult, ...prev.slice(0, 4)]); // 최근 5개만 유지
  };

  const handleCrawl = async (type) => {
    try {
      setLoading(prev => ({ ...prev, [type]: true }));

      let response;
      let typeName = '';
      
      switch (type) {
        case 'billboard':
          response = await crawlerApi.crawlBillboardJapan();
          typeName = 'Billboard Japan';
          break;
        case 'oricon':
          response = await crawlerApi.crawlOricon();
          typeName = 'Oricon';
          break;
        case 'all':
          response = await crawlerApi.crawlAll();
          typeName = '전체 차트';
          break;
        default:
          return;
      }

      setStatus(response.data);
      setLastCrawl(new Date().toLocaleString('ko-KR'));
      addResult(typeName, true, '크롤링 성공!');

      // 3초 후 상태 메시지 자동 새로고침
      setTimeout(() => {
        setStatus('');
      }, 5000);

    } catch (error) {
      const errorMessage = '크롤링 실패: ' + error.message;
      setStatus(errorMessage);
      addResult(type, false, errorMessage);
    } finally {
      setLoading(prev => ({ ...prev, [type]: false }));
    }
  };

  const checkStatus = async () => {
    try {
      const response = await crawlerApi.getStatus();
      setStatus(response.data);
      addResult('시스템', true, '서비스 상태 정상');
    } catch (error) {
      setStatus('상태 확인 실패');
      addResult('시스템', false, '상태 확인 실패');
    }
  };

  const crawlButtons = [
    {
      type: 'billboard',
      label: 'Billboard Japan',
      icon: '🎯',
      color: 'from-blue-500 to-blue-700',
      hoverColor: 'hover:from-blue-600 hover:to-blue-800'
    },
    {
      type: 'oricon',
      label: 'Oricon',
      icon: '🏆',
      color: 'from-green-500 to-green-700',
      hoverColor: 'hover:from-green-600 hover:to-green-800'
    },
    {
      type: 'all',
      label: '전체 크롤링',
      icon: '🚀',
      color: 'from-purple-500 to-purple-700',
      hoverColor: 'hover:from-purple-600 hover:to-purple-800'
    }
  ];

  return (
      <div className="max-w-6xl mx-auto p-6">
        <motion.div 
          className="bg-white rounded-2xl shadow-xl p-8"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <div className="text-center mb-8">
            <h2 className="text-4xl font-bold mb-4 bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent flex items-center justify-center">
              <Activity className="mr-3 text-purple-600" size={36} />
              크롤링 컨트롤 센터
            </h2>
            <p className="text-gray-600">음악 차트 데이터를 실시간으로 수집하고 관리하세요</p>
          </div>

          {/* 크롤링 버튼들 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {crawlButtons.map((button) => (
              <motion.button
                key={button.type}
                onClick={() => handleCrawl(button.type)}
                disabled={loading[button.type]}
                className={`px-8 py-6 rounded-xl font-semibold text-lg text-white shadow-lg transition-all duration-300 ${
                  loading[button.type]
                    ? 'bg-gray-400 cursor-not-allowed'
                    : 'bg-sky-300 hover:bg-sky-400 hover:scale-105 hover:shadow-xl'
                }`}
                whileHover={{ scale: loading[button.type] ? 1 : 1.05, y: loading[button.type] ? 0 : -2 }}
                whileTap={{ scale: loading[button.type] ? 1 : 0.98 }}
              >
                <div className="flex flex-col items-center gap-3">
                  {loading[button.type] ? (
                    <RefreshCw className="animate-spin" size={32} />
                  ) : (
                    <div className="text-3xl">{button.icon}</div>
                  )}
                  <span>{button.label}</span>
                  {loading[button.type] && (
                    <div className="text-sm opacity-80">실행 중...</div>
                  )}
                </div>
              </motion.button>
            ))}
          </div>

          {/* 시스템 상태 확인 */}
          <div className="text-center mb-8">
            <motion.button
                onClick={checkStatus}
                className="px-8 py-4 rounded-xl font-semibold text-white bg-slate-300 hover:bg-slate-400 shadow-lg hover:scale-105 hover:shadow-xl transition-all duration-300"
                whileHover={{ y: -2 }}
                whileTap={{ scale: 0.98 }}
            >
              시스템 상태 확인
            </motion.button>
          </div>

          {/* 현재 상태 메시지 */}
          <AnimatePresence>
            {status && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="mb-6"
              >
                <div className={`p-4 rounded-xl ${
                  status.includes('실패') || status.includes('오류') 
                    ? 'bg-red-100 border border-red-300 text-red-700'
                    : 'bg-green-100 border border-green-300 text-green-700'
                }`}>
                  <div className="flex items-center">
                    {status.includes('실패') || status.includes('오류') ? (
                      <XCircle className="mr-2" size={20} />
                    ) : (
                      <CheckCircle className="mr-2" size={20} />
                    )}
                    <p className="font-medium">{status}</p>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* 실행 결과 히스토리 */}
          {results.length > 0 && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="bg-gray-50 rounded-xl p-6"
            >
              <h3 className="text-lg font-semibold mb-4 flex items-center">
                <Clock className="mr-2" size={20} />
                최근 실행 기록
              </h3>
              <div className="space-y-3">
                <AnimatePresence>
                  {results.map((result) => (
                    <motion.div
                      key={result.id}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 20 }}
                      className={`flex items-center justify-between p-3 rounded-lg ${
                        result.success 
                          ? 'bg-green-50 border border-green-200' 
                          : 'bg-red-50 border border-red-200'
                      }`}
                    >
                      <div className="flex items-center">
                        {result.success ? (
                          <CheckCircle className="text-green-600 mr-3" size={16} />
                        ) : (
                          <XCircle className="text-red-600 mr-3" size={16} />
                        )}
                        <div>
                          <span className="font-medium">{result.type}</span>
                          <p className={`text-sm ${
                            result.success ? 'text-green-700' : 'text-red-700'
                          }`}>
                            {result.message}
                          </p>
                        </div>
                      </div>
                      <span className="text-xs text-gray-500">{result.timestamp}</span>
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            </motion.div>
          )}

          {/* 마지막 크롤링 시간 */}
          {lastCrawl && (
            <motion.div 
              className="mt-6 text-center"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              <div className="inline-flex items-center gap-2 px-4 py-2 bg-blue-50 text-blue-700 rounded-lg">
                <Clock size={16} />
                <span className="text-sm">마지막 크롤링: {lastCrawl}</span>
              </div>
            </motion.div>
          )}

          {/* 도움말 */}
          <motion.div 
            className="mt-8 p-4 bg-yellow-50 border border-yellow-200 rounded-xl"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <h4 className="font-semibold text-yellow-800 mb-2">💡 사용 팁</h4>
            <ul className="text-sm text-yellow-700 space-y-1">
              <li>• Billboard Japan: 실시간 HOT 100 차트 데이터</li>
              <li>• Oricon: 주간 싱글 차트 데이터</li>
              <li>• 전체 크롤링: 모든 지원 차트를 한번에 업데이트</li>
              <li>• 크롤링 후 자동으로 중복 데이터가 정리됩니다</li>
            </ul>
          </motion.div>
        </motion.div>
      </div>
  );
};

export default CrawlerControl;