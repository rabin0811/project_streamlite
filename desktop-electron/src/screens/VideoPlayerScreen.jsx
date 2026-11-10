import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Play, Pause, SkipBack, SkipForward, Volume2, VolumeX,
  Maximize, Minimize, Lock, Unlock, Clock, Monitor,
  ChevronDown, Settings, Subtitles, Video,
  Sun, Heart,
} from 'lucide-react';
import usePlayerStore from '../stores/playerStore';
import useLibraryStore from '../stores/libraryStore';
import { formatTime } from '../utils/formatTime';
import SleepTimer from '../components/SleepTimer';

const SPEEDS = [0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2, 2.5, 3, 4];

function getMediaUrl(media) {
  if (!media) return '';
  if (media.url) return media.url;
  if (!media.path) return '';
  const normalized = media.path.replace(/\\/g, '/');
  const encoded = normalized.startsWith('//')
    ? 'file:' + encodeURI(normalized)
    : 'file:///' + encodeURI(normalized.replace(/^\/+/, ''));
  return encoded.replace(/#/g, '%23').replace(/\?/g, '%3F');
}

export default function VideoPlayerScreen({ media, onClose }) {
  const navigate = useNavigate();
  const containerRef = useRef(null);
  const videoRef = useRef(null);
  const hideTimerRef = useRef(null);
  const gestureRef = useRef(null);

  const [showControls, setShowControls] = useState(true);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);
  const [showTrackMenu, setShowTrackMenu] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [showSleepTimer, setShowSleepTimer] = useState(false);
  const [screenLocked, setScreenLocked] = useState(false);
  const [gestureHint, setGestureHint] = useState(null);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const {
    playing, paused, currentTime, duration, volume, muted, speed,
    brightness, setCurrentTime, setDuration, setPlaying, setError,
    setVolume, toggleMute, setSpeed, togglePlay, seek, seekRelative,
    setFullscreen, stop, setBrightness, setSleepTimer, sleepTimer,
    addToHistory,
  } = usePlayerStore();

  const videoUrl = getMediaUrl(media);
  const [videoError, setVideoError] = useState(null);

  const addRecent = useLibraryStore((s) => s.addRecent);
  const isFavorite = useLibraryStore((s) => s.isFavorite);
  const toggleFavorite = useLibraryStore((s) => s.toggleFavorite);

  useEffect(() => {
    if (media) {
      addToHistory(media);
      addRecent(media);
    }
  }, [media?.path, media?.url]);

  useEffect(() => {
    const handleKey = (e) => {
      if (screenLocked) return;
      switch (e.code) {
        case 'Space': e.preventDefault(); togglePlay(); break;
        case 'ArrowLeft': seekRelative(-10); break;
        case 'ArrowRight': seekRelative(10); break;
        case 'ArrowUp': e.preventDefault(); setVolume(volume + 0.1); break;
        case 'ArrowDown': e.preventDefault(); setVolume(volume - 0.1); break;
        case 'KeyF': toggleFullscreen(); break;
        case 'KeyM': toggleMute(); break;
        case 'Escape': if (isFullscreen) toggleFullscreen(); else handleClose(); break;
      }
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [playing, volume, isFullscreen, screenLocked, currentTime]);

  useEffect(() => {
    const checkSleep = setInterval(() => {
      const st = usePlayerStore.getState().sleepTimer;
      if (st && Date.now() >= st) {
        handleClose();
        setSleepTimer(null);
      }
    }, 1000);
    return () => clearInterval(checkSleep);
  }, []);

  useEffect(() => {
    setVideoError(null);
  }, [media?.path, media?.url]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    if (playing) {
      const playPromise = video.play();
      if (playPromise && typeof playPromise.catch === 'function') playPromise.catch(() => {});
    } else {
      video.pause();
    }
  }, [playing, videoUrl]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    video.volume = muted ? 0 : volume;
    video.muted = muted;
  }, [volume, muted]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    video.playbackRate = speed;
  }, [speed]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    if (Number.isFinite(currentTime) && Math.abs(video.currentTime - currentTime) > 0.75) {
      const max = Number.isFinite(video.duration) ? video.duration : currentTime;
      video.currentTime = Math.max(0, Math.min(currentTime, max));
    }
  }, [currentTime]);

  const handleVideoMetadata = () => {
    const video = videoRef.current;
    if (!video) return;
    setDuration(Number.isFinite(video.duration) ? video.duration : 0);
    setCurrentTime(video.currentTime || 0);
    if (playing) video.play().catch(() => {});
  };

  const handleVideoTimeUpdate = () => {
    const video = videoRef.current;
    if (!video) return;
    setCurrentTime(video.currentTime);
    if (Number.isFinite(video.duration) && Math.abs(video.duration - duration) > 0.1) {
      setDuration(video.duration);
    }
  };

  const handleVideoPlay = () => setPlaying(true);

  const handleVideoEnded = () => {
    const video = videoRef.current;
    setCurrentTime(video && Number.isFinite(video.duration) ? video.duration : duration);
    setPlaying(false);
  };

  const handleVideoError = () => {
    const video = videoRef.current;
    const message = video?.error?.message || 'Unable to play this media file.';
    setVideoError(message);
    setError(message);
    setPlaying(false);
  };

  const startHideTimer = useCallback(() => {
    if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    if (playing && !paused) {
      hideTimerRef.current = setTimeout(() => {
        if (!screenLocked) setShowControls(false);
      }, 3000);
    }
  }, [playing, paused, screenLocked]);

  useEffect(() => {
    startHideTimer();
    return () => { if (hideTimerRef.current) clearTimeout(hideTimerRef.current); };
  }, [playing, paused, startHideTimer]);

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
    setFullscreen(!isFullscreen);
  };

  const handleClose = () => {
    stop();
    onClose();
    if (isFullscreen) setFullscreen(false);
    navigate('/');
  };

  const handleControlsInteraction = () => {
    setShowControls(true);
    startHideTimer();
  };

  // Gesture handling
  const gestureState = useRef({ startX: 0, startY: 0, type: null, startValue: 0, seeking: false });

  const handlePointerDown = (e) => {
    if (screenLocked) {
      setShowControls(!showControls);
      return;
    }
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect) return;

    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const w = rect.width;
    const h = rect.height;

    gestureState.current = {
      startX: x,
      startY: y,
      type: null,
      startValue: 0,
      seeking: false,
      moved: false,
    };

    if (e.target?.closest('.controls-area')) return;

    containerRef.current?.setPointerCapture(e.pointerId);
  };

  const handlePointerMove = (e) => {
    const gs = gestureState.current;
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect) return;

    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const dx = x - gs.startX;
    const dy = gs.startY - y;
    const absDx = Math.abs(dx);
    const absDy = Math.abs(dy);

    if (!gs.type) {
      if (absDx < 10 && absDy < 10) return;
      gs.moved = true;

      const inLeftThird = gs.startX < rect.width / 3;
      const inRightThird = gs.startX > (rect.width * 2) / 3;

      if (absDy > absDx && absDy > 15) {
        if (inLeftThird) {
          gs.type = 'brightness';
          gs.startValue = brightness;
        } else if (inRightThird) {
          gs.type = 'volume';
          gs.startValue = volume;
        } else {
          gs.type = 'volume';
          gs.startValue = volume;
        }
      } else if (absDx > absDy && absDx > 15) {
        gs.type = 'seek';
        gs.startValue = currentTime;
        gs.startX = x;
      }
    } else {
      gs.moved = true;
      if (gs.type === 'brightness') {
        const delta = dy / (rect.height * 0.5);
        const val = Math.max(0.1, Math.min(2, gs.startValue + delta));
        setBrightness(val);
        setGestureHint({ icon: Sun, label: `${Math.round(val * 50)}%` });
      } else if (gs.type === 'volume') {
        const delta = dy / (rect.height * 0.5);
        const val = Math.max(0, Math.min(1, gs.startValue + delta));
        setVolume(val);
        setGestureHint({ icon: muted ? VolumeX : Volume2, label: `${Math.round(val * 100)}%` });
      } else if (gs.type === 'seek') {
        const delta = (x - gs.startX) / rect.width;
        const dur = duration || 120;
        const seekDelta = delta * dur;
        const val = Math.max(0, Math.min(dur, gs.startValue + seekDelta));
        setCurrentTime(val);
        setGestureHint({ icon: null, label: formatTime(val) });
      }
    }
  };

  const handlePointerUp = (e) => {
    const gs = gestureState.current;
    if (!gs.moved && !screenLocked) {
      togglePlay();
    }
    if (gs.moved && gs.type === 'seek' && duration) {
      seek(currentTime);
    }
    gestureState.current = { startX: 0, startY: 0, type: null, startValue: 0, seeking: false };
    setGestureHint(null);
    containerRef.current?.releasePointerCapture(e.pointerId);
  };

  const handleDoubleClick = () => {
    if (screenLocked) return;
    toggleFullscreen();
  };

  const handleSeekChange = (e) => {
    const val = parseFloat(e.target.value);
    setCurrentTime(val);
  };

  const handleSeekEnd = () => {
    seek(currentTime);
  };

  const handleVolumeChange = (e) => {
    setVolume(parseFloat(e.target.value));
  };

  const totalTime = duration || 120;
  const progress = totalTime > 0 ? (currentTime / totalTime) * 100 : 0;

  const isStream = media?.url?.startsWith('http') || media?.url?.startsWith('rtmp');

  return (
    <div
      ref={containerRef}
      className="fixed inset-0 bg-black z-[9999] flex flex-col"
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onDoubleClick={handleDoubleClick}
      onContextMenu={(e) => e.preventDefault()}
      style={{ filter: `brightness(${brightness})` }}
    >
      {/* Video background */}
      <div className="absolute inset-0 flex items-center justify-center bg-black">
        {videoUrl && !videoError ? (
          <video
            ref={videoRef}
            src={videoUrl}
            className="absolute inset-0 w-full h-full object-contain bg-black pointer-events-none"
            playsInline
            preload="metadata"
            onLoadedMetadata={handleVideoMetadata}
            onTimeUpdate={handleVideoTimeUpdate}
            onPlay={handleVideoPlay}
            onEnded={handleVideoEnded}
            onError={handleVideoError}
          />
        ) : (
          <div className="text-center px-6">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#4FC3F7]/20 to-[#F48FB1]/20 flex items-center justify-center mx-auto mb-4">
              <Play size={40} className="text-[#4FC3F7]/60 ml-2" />
            </div>
            <p className="text-white/40 text-sm font-medium">{media?.name || 'Media Player'}</p>
            {videoError ? (
              <p className="text-red-400/80 text-xs mt-2 max-w-md">{videoError}</p>
            ) : isStream ? (
              <p className="text-white/20 text-xs mt-2">Streaming from network</p>
            ) : null}
          </div>
        )}
      </div>

      {/* Gesture hint overlay */}
      {gestureHint && (
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-50">
          <div className="glass-strong rounded-2xl px-8 py-6 flex flex-col items-center gap-2 animate-scale-in">
            {gestureHint.icon && <gestureHint.icon size={32} className="text-[#4FC3F7]" />}
            <span className="text-white text-lg font-semibold">{gestureHint.label}</span>
          </div>
        </div>
      )}

      {/* Top bar */}
      <div className={`absolute top-0 left-0 right-0 z-40 transition-opacity duration-300 ${showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'}`}>
        <div className="bg-gradient-to-b from-black/80 to-transparent p-4 pb-8">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button onClick={handleClose} className="btn-ghost text-white p-1.5 hover:bg-white/10 rounded-lg transition-colors">
                <ChevronDown size={22} />
              </button>
              <div>
                <p className="text-sm text-white font-medium truncate max-w-[300px]">{media?.name || 'Player'}</p>
                {media?.group && <p className="text-xs text-white/50">{media.group}</p>}
              </div>
            </div>
            <div className="flex items-center gap-1">
              <button
                onClick={() => toggleFavorite(media)}
                className={`btn-ghost text-white p-2 ${isFavorite(media) ? 'text-[#F48FB1]' : ''}`}
              >
                <Heart size={18} className={isFavorite(media) ? 'fill-[#F48FB1]' : ''} />
              </button>
              <button
                onClick={() => { setShowSleepTimer(true); setShowControls(true); }}
                className="btn-ghost text-white p-2"
              >
                <Clock size={18} />
              </button>
              <button
                onClick={() => { setScreenLocked(!screenLocked); }}
                className={`btn-ghost p-2 ${screenLocked ? 'text-[#4FC3F7]' : 'text-white'}`}
              >
                {screenLocked ? <Lock size={18} /> : <Unlock size={18} />}
              </button>
              <button onClick={toggleFullscreen} className="btn-ghost text-white p-2">
                {isFullscreen ? <Minimize size={18} /> : <Maximize size={18} />}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom controls */}
      <div className={`absolute bottom-0 left-0 right-0 z-40 transition-opacity duration-300 ${showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'}`}>
        <div className="bg-gradient-to-t from-black/90 via-black/50 to-transparent pt-12 pb-4 px-4">
          {/* Seek bar */}
          <div className="flex items-center gap-3 mb-3">
            <span className="text-xs text-white/60 w-12 text-right tabular-nums">{formatTime(currentTime)}</span>
            <div className="flex-1 controls-area">
              <input
                type="range"
                min={0}
                max={totalTime}
                step={0.1}
                value={currentTime}
                onChange={handleSeekChange}
                onMouseUp={handleSeekEnd}
                onTouchEnd={handleSeekEnd}
                className="slider seek-slider w-full"
                style={{
                  background: `linear-gradient(to right, #4FC3F7 ${progress}%, rgba(255,255,255,0.15) ${progress}%)`,
                }}
              />
            </div>
            <span className="text-xs text-white/60 w-12 tabular-nums">{formatTime(totalTime)}</span>
          </div>

          {/* Controls row */}
          <div className="flex items-center justify-between controls-area">
            <div className="flex items-center gap-1">
              <button onClick={() => seekRelative(-10)} className="btn-ghost text-white p-2 hover:bg-white/10 rounded-lg">
                <SkipBack size={20} />
              </button>
              <button onClick={togglePlay} className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors mx-1">
                {playing ? <Pause size={22} className="text-white ml-0" /> : <Play size={22} className="text-white ml-0.5" />}
              </button>
              <button onClick={() => seekRelative(10)} className="btn-ghost text-white p-2 hover:bg-white/10 rounded-lg">
                <SkipForward size={20} />
              </button>

              {/* Volume */}
              <div className="flex items-center gap-1 ml-3 group/vol">
                <button onClick={toggleMute} className="btn-ghost text-white/80 p-1.5 hover:text-white">
                  {muted || volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
                </button>
                <div className="w-0 overflow-hidden group-hover/vol:w-20 transition-all duration-200">
                  <input
                    type="range"
                    min={0}
                    max={1}
                    step={0.01}
                    value={muted ? 0 : volume}
                    onChange={handleVolumeChange}
                    className="slider volume-slider w-20"
                    style={{
                      background: `linear-gradient(to right, #4FC3F7 ${(muted ? 0 : volume) * 100}%, rgba(255,255,255,0.15) ${(muted ? 0 : volume) * 100}%)`,
                    }}
                  />
                </div>
              </div>
            </div>

            <div className="flex items-center gap-1">
              {/* Speed */}
              <div className="relative">
                <button
                  onClick={() => { setShowSpeedMenu(!showSpeedMenu); setShowTrackMenu(false); setShowSettings(false); }}
                  className="btn-ghost text-white/80 p-2 hover:text-white text-sm"
                >
                  {speed}x
                </button>
                {showSpeedMenu && (
                  <div className="absolute bottom-full right-0 mb-2 glass-strong rounded-xl p-2 min-w-[100px] animate-scale-in">
                    {SPEEDS.map((s) => (
                      <button
                        key={s}
                        onClick={() => { setSpeed(s); setShowSpeedMenu(false); }}
                        className={`block w-full text-left px-3 py-1.5 rounded-lg text-sm transition-colors ${
                          speed === s ? 'text-[#4FC3F7] bg-[#4FC3F7]/10' : 'text-white/70 hover:text-white hover:bg-white/5'
                        }`}
                      >
                        {s}x
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Tracks */}
              <div className="relative">
                <button
                  onClick={() => { setShowTrackMenu(!showTrackMenu); setShowSpeedMenu(false); setShowSettings(false); }}
                  className="btn-ghost text-white/80 p-2 hover:text-white"
                >
                  <Subtitles size={18} />
                </button>
                {showTrackMenu && (
                  <div className="absolute bottom-full right-0 mb-2 glass-strong rounded-xl p-2 min-w-[150px] animate-scale-in">
                    <p className="text-xs text-[#8892b0] px-3 py-1">Track Selection</p>
                    <button className="block w-full text-left px-3 py-1.5 rounded-lg text-sm text-white/70 hover:text-white hover:bg-white/5">Auto</button>
                    <button className="block w-full text-left px-3 py-1.5 rounded-lg text-sm text-white/70 hover:text-white hover:bg-white/5">Track 1</button>
                  </div>
                )}
              </div>

              {/* Settings */}
              <div className="relative">
                <button
                  onClick={() => { setShowSettings(!showSettings); setShowSpeedMenu(false); setShowTrackMenu(false); }}
                  className="btn-ghost text-white/80 p-2 hover:text-white"
                >
                  <Settings size={18} />
                </button>
                {showSettings && (
                  <div className="absolute bottom-full right-0 mb-2 glass-strong rounded-xl p-2 min-w-[180px] animate-scale-in">
                    <p className="text-xs text-[#8892b0] px-3 py-1">Settings</p>
                    <button className="block w-full text-left px-3 py-1.5 rounded-lg text-sm text-white/70 hover:text-white hover:bg-white/5 flex items-center gap-2">
                      <Monitor size={14} /> Aspect Ratio
                    </button>
                    <button className="block w-full text-left px-3 py-1.5 rounded-lg text-sm text-white/70 hover:text-white hover:bg-white/5 flex items-center gap-2">
                      <Video size={14} /> Video Track
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Center play/pause indicator */}
      {!playing && !paused && (
        <div className="absolute inset-0 flex items-center justify-center z-30 pointer-events-none">
          <div className="w-20 h-20 rounded-full bg-[#4FC3F7]/20 backdrop-blur-sm flex items-center justify-center animate-scale-in">
            <Play size={36} className="text-white ml-2" />
          </div>
        </div>
      )}

      {/* Sleep timer dialog */}
      {showSleepTimer && (
        <SleepTimer
          onClose={() => setShowSleepTimer(false)}
          onSet={(mins) => { setSleepTimer(mins); setShowSleepTimer(false); }}
        />
      )}

      {/* Screen locked indicator */}
      {screenLocked && (
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-30 pointer-events-none">
          <div className="glass-strong rounded-2xl px-6 py-4 flex items-center gap-3">
            <Lock size={20} className="text-[#4FC3F7]" />
            <span className="text-white text-sm">Screen Locked</span>
          </div>
        </div>
      )}
    </div>
  );
}
