import { useEffect, useState } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import Layout from './components/Layout';
import HomeScreen from './screens/HomeScreen';
import LibraryScreen from './screens/LibraryScreen';
import NetworkScreen from './screens/NetworkScreen';
import SettingsScreen from './screens/SettingsScreen';
import VideoPlayerScreen from './screens/VideoPlayerScreen';
import Toast from './components/Toast';
import usePlayerStore from './stores/playerStore';
import usePlaylistStore from './stores/playlistStore';
import useLibraryStore from './stores/libraryStore';
import useSettingsStore from './stores/settingsStore';

export default function App() {
  const [toast, setToast] = useState(null);
  const location = useLocation();
  const isPlayerRoute = location.pathname === '/player';
  const currentMedia = usePlayerStore((s) => s.currentMedia);
  const closePlayer = usePlayerStore((s) => s.closePlayer);

  useEffect(() => {
    useSettingsStore.getState().loadFromDisk();
    usePlaylistStore.getState().loadFromDisk();
    useLibraryStore.getState().loadFromDisk();
  }, []);

  useEffect(() => {
    const showToast = (message, type = 'info') => {
      setToast({ message, type });
      setTimeout(() => setToast(null), 3000);
    };

    window.addEventListener('toast', (e) => showToast(e.detail.message, e.detail.type));
    return () => window.removeEventListener('toast', (e) => showToast(e.detail.message, e.detail.type));
  }, []);

  const handleClosePlayer = () => {
    closePlayer();
  };

  if (isPlayerRoute && currentMedia) {
    return (
      <>
        <VideoPlayerScreen media={currentMedia} onClose={handleClosePlayer} />
        <Toast toast={toast} onClose={() => setToast(null)} />
      </>
    );
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<HomeScreen />} />
        <Route path="/library" element={<LibraryScreen />} />
        <Route path="/network" element={<NetworkScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
      </Routes>
      <Toast toast={toast} onClose={() => setToast(null)} />
    </Layout>
  );
}
