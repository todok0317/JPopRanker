import React, { useState, useEffect } from 'react';
import { songApi } from '../services/api';
import { Crown, Music, Play, Pause, RotateCcw, ChevronLeft, ChevronRight } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const ChartList = ({ chartName = 'billboard-japan', onDataUpdate }) => {
  const [songs, setSongs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [showAll, setShowAll] = useState(false);
  const [direction, setDirection] = useState(0); // -1: 왼쪽, 1: 오른쪽

  useEffect(() => {
    fetchSongs();
  }, [chartName]);

  useEffect(() => {
    let interval;
    if (isPlaying && currentIndex < songs.length && !showAll) {
      interval = setInterval(() => {
        setCurrentIndex(prev => {
          if (prev >= songs.length - 1) {
            setIsPlaying(false);
            return prev;
          }
          setDirection(1); // 오른쪽으로 이동
          return prev + 1;
        });
      }, 2000); // 2초마다 (좀 더 여유롭게)
    }
    return () => clearInterval(interval);
  }, [isPlaying, currentIndex, songs.length, showAll]);

  const fetchSongs = async () => {
    try {
      setLoading(true);
      const response = await songApi.getSongsByChart(chartName);
      setSongs(response.data);
      setCurrentIndex(0);
      setIsPlaying(false);
      setShowAll(false);
      if (onDataUpdate) onDataUpdate();
    } catch (err) {
      setError('데이터를 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const startAnimation = () => {
    setCurrentIndex(0);
    setIsPlaying(true);
    setShowAll(false);
    setDirection(1);
  };

  const pauseAnimation = () => {
    setIsPlaying(false);
  };

  const resetAnimation = () => {
    setCurrentIndex(0);
    setIsPlaying(false);
    setShowAll(false);
    setDirection(0);
  };

  const goToNext = () => {
    if (currentIndex < songs.length - 1) {
      setDirection(1);
      setCurrentIndex(prev => prev + 1);
      setIsPlaying(false);
    }
  };

  const goToPrev = () => {
    if (currentIndex > 0) {
      setDirection(-1);
      setCurrentIndex(prev => prev - 1);
      setIsPlaying(false);
    }
  };

  const showAllSongs = () => {
    setShowAll(true);
    setIsPlaying(false);
  };

  const getRankColor = (ranking) => {
    if (ranking === 1) return 'from-yellow-400 via-yellow-500 to-yellow-600';
    if (ranking === 2) return 'from-gray-300 via-gray-400 to-gray-500';
    if (ranking === 3) return 'from-amber-600 via-amber-700 to-amber-800';
    if (ranking <= 10) return 'from-purple-500 via-purple-600 to-purple-700';
    return 'from-blue-500 via-blue-600 to-blue-700';
  };

  const getRankIcon = (ranking) => {
    if (ranking <= 3) {
      return <Crown className={`${
        ranking === 1 ? 'text-yellow-200' :
        ranking === 2 ? 'text-gray-200' : 'text-amber-200'
      }`} size={48} />;
    }
    return <Music className="text-white" size={48} />;
  };

  // 슬라이드 애니메이션 설정
  const slideVariants = {
    enter: (direction) => ({
      x: direction > 0 ? 800 : -800,  // 좌우 이동만
      opacity: 0,
      scale: 0.9
    }),
    center: {
      zIndex: 1,
      x: 0,
      opacity: 1,
      scale: 1
    },
    exit: (direction) => ({
      zIndex: 0,
      x: direction < 0 ? 800 : -800,  // 좌우 이동만
      opacity: 0,
      scale: 0.9
    }),
  };

  const swipeConfidenceThreshold = 10000;
  const swipePower = (offset, velocity) => {
    return Math.abs(offset) * velocity;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-purple-600 mx-auto mb-4"></div>
          <p className="text-xl text-gray-600">차트 로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-8">
        <p className="text-red-500 text-xl mb-4">{error}</p>
        <button
          onClick={fetchSongs}
          className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (showAll) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <div className="text-center mb-8">
          <h2 className="text-4xl font-bold mb-4 bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent">
            {chartName === 'billboard-japan' ? 'Billboard Japan Hot 100' : 'Oricon 차트'} - 전체 목록
          </h2>
          <motion.button
            onClick={() => setShowAll(false)}
            className="px-8 py-4 rounded-xl font-semibold text-white bg-sky-300 hover:bg-sky-400 shadow-lg hover:scale-105 hover:shadow-xl transition-all duration-300"
            whileHover={{ y: -2 }}
            whileTap={{ scale: 0.98 }}
          >
            카드 뷰로 돌아가기
          </motion.button>
        </div>
        
        <div className="grid gap-4">
          {songs.map((song, index) => (
            <motion.div
              key={song.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.05 }}
              className={`bg-gradient-to-r ${getRankColor(song.ranking)} rounded-xl p-4 text-white shadow-lg flex items-center gap-4`}
            >
              <div className="flex items-center justify-center w-12 h-12 bg-white bg-opacity-20 rounded-full">
                {getRankIcon(song.ranking)}
              </div>
              <div className="text-2xl font-bold opacity-30">#{song.ranking}</div>
              <div className="flex-1">
                <h3 className="text-lg font-bold">{song.title}</h3>
                <p className="opacity-90">{song.artist}</p>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* 헤더 및 컨트롤 */}
      <div className="text-center mb-8">
        <motion.h2 
          className="text-4xl font-bold mb-6 bg-gradient-to-r from-purple-600 to-blue-600 bg-clip-text text-transparent"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          {chartName === 'billboard-japan' ? 'Billboard Japan Hot 100' : 'Oricon 차트'}
        </motion.h2>
        
        <div className="flex justify-center gap-3 mb-6 flex-wrap">
          <motion.button
            onClick={startAnimation}
            disabled={isPlaying}
            style={{
              backgroundColor: isPlaying ? '#9CA3AF' : '#87CEEB',
              color: 'white',
              padding: '16px 32px',
              borderRadius: '12px',
              fontWeight: '600',
              border: 'none',
              cursor: isPlaying ? 'not-allowed' : 'pointer',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              if (!isPlaying) {
                e.target.style.backgroundColor = '#6BB6FF';
                e.target.style.transform = 'translateY(-2px) scale(1.05)';
                e.target.style.boxShadow = '0 8px 15px rgba(0, 0, 0, 0.2)';
              }
            }}
            onMouseLeave={(e) => {
              if (!isPlaying) {
                e.target.style.backgroundColor = '#87CEEB';
                e.target.style.transform = 'translateY(0px) scale(1)';
                e.target.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
              }
            }}
            whileHover={!isPlaying ? { y: -2 } : {}}
            whileTap={!isPlaying ? { scale: 0.98 } : {}}
          >
            자동 재생
          </motion.button>
          
          <motion.button
            onClick={pauseAnimation}
            disabled={!isPlaying}
            style={{
              backgroundColor: !isPlaying ? '#9CA3AF' : '#FFB6C1',
              color: 'white',
              padding: '16px 32px',
              borderRadius: '12px',
              fontWeight: '600',
              border: 'none',
              cursor: !isPlaying ? 'not-allowed' : 'pointer',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              if (isPlaying) {
                e.target.style.backgroundColor = '#FF91A4';
                e.target.style.transform = 'translateY(-2px) scale(1.05)';
                e.target.style.boxShadow = '0 8px 15px rgba(0, 0, 0, 0.2)';
              }
            }}
            onMouseLeave={(e) => {
              if (isPlaying) {
                e.target.style.backgroundColor = '#FFB6C1';
                e.target.style.transform = 'translateY(0px) scale(1)';
                e.target.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
              }
            }}
            whileHover={isPlaying ? { y: -2 } : {}}
            whileTap={isPlaying ? { scale: 0.98 } : {}}
          >
            일시정지
          </motion.button>
          
          <motion.button
            onClick={resetAnimation}
            style={{
              backgroundColor: '#B8C5D1',
              color: 'white',
              padding: '16px 32px',
              borderRadius: '12px',
              fontWeight: '600',
              border: 'none',
              cursor: 'pointer',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              e.target.style.backgroundColor = '#9FB4C7';
              e.target.style.transform = 'translateY(-2px) scale(1.05)';
              e.target.style.boxShadow = '0 8px 15px rgba(0, 0, 0, 0.2)';
            }}
            onMouseLeave={(e) => {
              e.target.style.backgroundColor = '#B8C5D1';
              e.target.style.transform = 'translateY(0px) scale(1)';
              e.target.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
            }}
            whileHover={{ y: -2 }}
            whileTap={{ scale: 0.98 }}
          >
            처음부터
          </motion.button>
          
          <motion.button
            onClick={showAllSongs}
            style={{
              backgroundColor: '#DDA0DD',
              color: 'white',
              padding: '16px 32px',
              borderRadius: '12px',
              fontWeight: '600',
              border: 'none',
              cursor: 'pointer',
              boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              e.target.style.backgroundColor = '#D891D8';
              e.target.style.transform = 'translateY(-2px) scale(1.05)';
              e.target.style.boxShadow = '0 8px 15px rgba(0, 0, 0, 0.2)';
            }}
            onMouseLeave={(e) => {
              e.target.style.backgroundColor = '#DDA0DD';
              e.target.style.transform = 'translateY(0px) scale(1)';
              e.target.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
            }}
            whileHover={{ y: -2 }}
            whileTap={{ scale: 0.98 }}
          >
            전체 보기
          </motion.button>
        </div>

        {songs.length > 0 && (
          <div className="text-lg text-gray-600 mb-4">
            <span className="font-bold text-purple-600">#{currentIndex + 1}</span> / {songs.length}
            {isPlaying && <span className="ml-2 text-green-600 animate-pulse">⚡ 자동재생 중</span>}
          </div>
        )}
      </div>

      {/* 카드 슬라이더 영역 */}
      <div className="relative h-96 flex items-center justify-center overflow-hidden">
        {/* 수동 네비게이션 버튼 */}
        <motion.button
          onClick={goToPrev}
          disabled={currentIndex === 0}
          style={{
            position: 'absolute',
            left: '120px',  // 더 안쪽으로 이동 (80px → 120px)
            top: '50%',    
            transform: 'translateY(-50%)',  
            zIndex: 10,
            padding: '16px',
            borderRadius: '50%',
            backgroundColor: currentIndex === 0 ? '#E5E7EB' : '#B8E6F5',
            opacity: currentIndex === 0 ? 0.3 : 1,
            cursor: currentIndex === 0 ? 'not-allowed' : 'pointer',
            boxShadow: '0 8px 20px rgba(0, 0, 0, 0.15)',
            border: 'none',
            transition: 'all 0.3s ease'
          }}
          onMouseEnter={(e) => {
            if (currentIndex > 0) {
              e.target.style.backgroundColor = '#9DD9F0';
              e.target.style.transform = 'translateY(-50%) scale(1.1) translateX(-5px)';
              e.target.style.boxShadow = '0 12px 25px rgba(0, 0, 0, 0.2)';
            }
          }}
          onMouseLeave={(e) => {
            if (currentIndex > 0) {
              e.target.style.backgroundColor = '#B8E6F5';
              e.target.style.transform = 'translateY(-50%) scale(1) translateX(0px)';
              e.target.style.boxShadow = '0 8px 20px rgba(0, 0, 0, 0.15)';
            }
          }}
          whileHover={currentIndex > 0 ? { scale: 1.1, x: -5 } : {}}
          whileTap={currentIndex > 0 ? { scale: 0.9 } : {}}
        >
          <ChevronLeft size={28} style={{ color: '#4B5563' }} />
        </motion.button>

        <motion.button
          onClick={goToNext}
          disabled={currentIndex === songs.length - 1}
          style={{
            position: 'absolute',
            right: '120px',  // 더 안쪽으로 이동 (80px → 120px)
            top: '50%',     
            transform: 'translateY(-50%)',  
            zIndex: 10,
            padding: '16px',
            borderRadius: '50%',
            backgroundColor: currentIndex === songs.length - 1 ? '#E5E7EB' : '#B8E6F5',
            opacity: currentIndex === songs.length - 1 ? 0.3 : 1,
            cursor: currentIndex === songs.length - 1 ? 'not-allowed' : 'pointer',
            boxShadow: '0 8px 20px rgba(0, 0, 0, 0.15)',
            border: 'none',
            transition: 'all 0.3s ease'
          }}
          onMouseEnter={(e) => {
            if (currentIndex < songs.length - 1) {
              e.target.style.backgroundColor = '#9DD9F0';
              e.target.style.transform = 'translateY(-50%) scale(1.1) translateX(5px)';
              e.target.style.boxShadow = '0 12px 25px rgba(0, 0, 0, 0.2)';
            }
          }}
          onMouseLeave={(e) => {
            if (currentIndex < songs.length - 1) {
              e.target.style.backgroundColor = '#B8E6F5';
              e.target.style.transform = 'translateY(-50%) scale(1) translateX(0px)';
              e.target.style.boxShadow = '0 8px 20px rgba(0, 0, 0, 0.15)';
            }
          }}
          whileHover={currentIndex < songs.length - 1 ? { scale: 1.1, x: 5 } : {}}
          whileTap={currentIndex < songs.length - 1 ? { scale: 0.9 } : {}}
        >
          <ChevronRight size={28} style={{ color: '#4B5563' }} />
        </motion.button>

        {/* 카드 슬라이더 */}
        <div className="w-full max-w-lg h-full relative overflow-hidden">
          <AnimatePresence initial={false} custom={direction} mode="wait">
            {songs.length > 0 && (
              <motion.div
                key={currentIndex}
                custom={direction}
                variants={slideVariants}
                initial="enter"
                animate="center"
                exit="exit"
                transition={{
                  x: { type: "spring", stiffness: 200, damping: 25 },
                  opacity: { duration: 0.3 },
                  scale: { duration: 0.3 }
                }}
                drag="x"
                dragConstraints={{ left: 0, right: 0 }}
                dragElastic={1}
                onDragEnd={(e, { offset, velocity }) => {
                  const swipe = swipePower(offset.x, velocity.x);

                  if (swipe < -swipeConfidenceThreshold) {
                    goToNext();
                  } else if (swipe > swipeConfidenceThreshold) {
                    goToPrev();
                  }
                }}
                className={`absolute w-full h-full bg-gradient-to-br ${getRankColor(songs[currentIndex]?.ranking)} rounded-3xl shadow-2xl cursor-grab active:cursor-grabbing`}
              >
                {/* 카드 내용 */}
                <div className="h-full flex flex-col justify-center items-center text-white p-8 relative">
                  {/* 순위 표시 */}
                  <div className="absolute top-6 left-6 text-8xl font-black opacity-20">
                    #{songs[currentIndex]?.ranking}
                  </div>

                  {/* 재생 중 표시 */}
                  {isPlaying && (
                    <div className="absolute top-6 right-6 bg-white bg-opacity-20 px-3 py-1 rounded-full">
                      <span className="text-sm font-semibold animate-pulse">🎵 LIVE</span>
                    </div>
                  )}

                  {/* 순위 아이콘 */}
                  <div className="mb-6">
                    {getRankIcon(songs[currentIndex]?.ranking)}
                  </div>

                  {/* 곡 정보 */}
                  <div className="text-center space-y-4">
                    <h1 className="text-3xl font-bold leading-tight">
                      {songs[currentIndex]?.title}
                    </h1>
                    <p className="text-xl opacity-90">
                      {songs[currentIndex]?.artist}
                    </p>
                    <div className="flex items-center justify-center gap-4 text-sm opacity-70">
                      <span>#{songs[currentIndex]?.ranking} 위</span>
                      <span>•</span>
                      <span>{new Date(songs[currentIndex]?.chartDate).toLocaleDateString('ko-KR')}</span>
                    </div>
                  </div>

                  {/* 순위별 특별 효과 */}
                  {songs[currentIndex]?.ranking === 1 && (
                    <div className="absolute inset-0 bg-gradient-to-r from-yellow-400 to-yellow-600 opacity-20 rounded-3xl animate-pulse"></div>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* 페이지 인디케이터 */}
        <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 flex gap-3 bg-white bg-opacity-20 backdrop-blur-sm rounded-full px-4 py-2">
          {songs.slice(0, Math.min(10, songs.length)).map((_, index) => (
            <motion.button
              key={index}
              onClick={() => {
                setDirection(index > currentIndex ? 1 : -1);
                setCurrentIndex(index);
                setIsPlaying(false);
              }}
              className={`w-3 h-3 rounded-full transition-all duration-300 ${
                index === currentIndex 
                  ? 'bg-white scale-125 shadow-lg' 
                  : 'bg-white bg-opacity-50 hover:bg-opacity-75 hover:scale-110'
              }`}
              whileHover={{ scale: index === currentIndex ? 1.25 : 1.1 }}
              whileTap={{ scale: 0.9 }}
            />
          ))}
          {songs.length > 10 && (
            <span className="text-white text-xs ml-2 px-2 py-1 bg-white bg-opacity-20 rounded-full">
              +{songs.length - 10}
            </span>
          )}
        </div>
      </div>

      {/* 다음 곡 미리보기 */}
      {currentIndex < songs.length - 1 && (
        <motion.div
          className="mt-8 text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
        >
          <div className="bg-white bg-opacity-80 backdrop-blur-sm rounded-xl p-4 inline-block shadow-lg">
            <p className="text-gray-600">
              다음: <span className="font-semibold text-purple-600">
                #{songs[currentIndex + 1]?.ranking} {songs[currentIndex + 1]?.title}
              </span> - {songs[currentIndex + 1]?.artist}
            </p>
          </div>
        </motion.div>
      )}

      {/* 완료 메시지 */}
      {currentIndex === songs.length - 1 && !isPlaying && songs.length > 0 && (
        <motion.div
          className="mt-8 text-center"
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.5 }}
        >
          <div className="bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-xl p-6 inline-block shadow-xl">
            <h3 className="text-2xl font-bold mb-2">🎉 차트 완주!</h3>
            <p>모든 순위를 확인했습니다. 수고하셨어요!</p>
          </div>
        </motion.div>
      )}
    </div>
  );
};

export default ChartList;