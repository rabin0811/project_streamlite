import { create } from 'zustand';

const usePlayerStore = create((set, get) => ({
  // State
  playing: false,
  paused: false,
  currentTime: 0,
  duration: 0,
  volume: 1,
  muted: false,
  speed: 1,
  mediaLoaded: false,
  currentMedia: null,
  fullscreen: false,
  controlsVisible: true,
  screenLocked: false,
  sleepTimer: null,
  isPiP: false,
  videoTracks: [],
  audioTracks: [],
  subtitleTracks: [],
  selectedAudio: null,
  selectedSubtitle: null,
  selectedVideo: null,
  error: null,
  loading: false,

  // Brightness/volume gesture
  brightness: 1,
  playbackHistory: [],

  // Actions
  setCurrentTime: (time) => set({ currentTime: time }),
  setDuration: (duration) => set({ duration }),
  setVolume: (volume) => {
    set({ volume: Math.max(0, Math.min(1, volume)), muted: volume === 0 });
    window.electronAPI?.playerSetVolume(volume);
  },
  toggleMute: () => {
    const { muted, volume } = get();
    if (muted) {
      set({ muted: false });
      window.electronAPI?.playerSetVolume(volume || 0.5);
    } else {
      set({ muted: true });
      window.electronAPI?.playerSetVolume(0);
    }
  },
  setSpeed: (speed) => {
    set({ speed });
    window.electronAPI?.playerSetSpeed(speed);
  },
  setPlaying: (playing) => set({ playing, paused: !playing }),
  setPaused: (paused) => set({ paused, playing: !paused }),
  setFullscreen: (fs) => {
    set({ fullscreen: fs });
    window.electronAPI?.playerSetFullscreen(fs);
  },
  toggleFullscreen: () => {
    const { fullscreen } = get();
    const next = !fullscreen;
    set({ fullscreen: next });
    window.electronAPI?.playerSetFullscreen(next);
  },
  setControlsVisible: (visible) => set({ controlsVisible: visible }),
  toggleControls: () => set((s) => ({ controlsVisible: !s.controlsVisible })),
  setScreenLocked: (locked) => set({ screenLocked: locked }),
  toggleScreenLock: () => set((s) => ({ screenLocked: !s.screenLocked })),
  setBrightness: (brightness) => set({ brightness: Math.max(0.1, Math.min(2, brightness)) }),
  setError: (error) => set({ error }),
  setLoading: (loading) => set({ loading }),
  setIsPiP: (isPiP) => set({ isPiP }),
  setSleepTimer: (minutes) => set({ sleepTimer: minutes ? Date.now() + minutes * 60000 : null }),

  loadMedia: (media) => {
    const { playbackHistory } = get();
    set({
      currentMedia: media,
      playing: false,
      paused: false,
      currentTime: 0,
      duration: 0,
      error: null,
      loading: true,
      controlsVisible: true,
      mediaLoaded: true,
      playbackHistory: [
        { ...media, playedAt: new Date().toISOString() },
        ...playbackHistory.filter((m) => m.path !== media.path || m.url !== media.url),
      ].slice(0, 50),
    });
    window.electronAPI?.playerLoad(media.url || media.path);
  },

  play: () => {
    set({ playing: true, paused: false });
    window.electronAPI?.playerPlay();
  },

  pause: () => {
    set({ playing: false, paused: true });
    window.electronAPI?.playerPause();
  },

  togglePlay: () => {
    const { playing, paused } = get();
    if (playing) {
      get().pause();
    } else {
      get().play();
    }
  },

  seek: (time) => {
    set({ currentTime: Math.max(0, time) });
    window.electronAPI?.playerSeek(time);
  },

  seekRelative: (delta) => {
    const { currentTime, duration } = get();
    const newTime = Math.max(0, Math.min(currentTime + delta, duration || Infinity));
    get().seek(newTime);
  },

  stop: () => {
    set({
      playing: false,
      paused: false,
      currentTime: 0,
      mediaLoaded: false,
      loading: false,
    });
    window.electronAPI?.playerStop();
  },

  updateFromEngine: (data) => {
    set({
      currentTime: data.currentTime ?? get().currentTime,
      duration: data.duration ?? get().duration,
    });
  },

  closePlayer: () => {
    get().stop();
    set({
      fullscreen: false,
      currentMedia: null,
      mediaLoaded: false,
    });
  },

  addToHistory: (media) => {
    set((s) => ({
      playbackHistory: [
        { ...media, playedAt: new Date().toISOString() },
        ...s.playbackHistory.filter((m) => m.path !== media.path || m.url !== media.url),
      ].slice(0, 50),
    }));
  },
}));

export default usePlayerStore;
