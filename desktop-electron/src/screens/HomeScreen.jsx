import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Play, Heart, Clock, Globe, TrendingUp, Tv, Search } from 'lucide-react';
import SearchBar from '../components/SearchBar';
import MediaCard from '../components/MediaCard';
import FAB from '../components/FAB';
import usePlayerStore from '../stores/playerStore';
import useLibraryStore from '../stores/libraryStore';
import usePlaylistStore from '../stores/playlistStore';

const quickAccess = [
  { icon: Play, label: 'Videos', gradient: 'from-[#4FC3F7] to-[#29B6F6]', path: '/library' },
  { icon: Heart, label: 'Favorites', gradient: 'from-[#F48FB1] to-[#EC407A]', path: '/library' },
  { icon: Clock, label: 'Recent', gradient: 'from-[#81C784] to-[#66BB6A]', path: '/library' },
  { icon: Globe, label: 'Network', gradient: 'from-[#4FC3F7] to-[#F48FB1]', path: '/network' },
];

export default function HomeScreen() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const playbackHistory = usePlayerStore((s) => s.playbackHistory);
  const recentFiles = useLibraryStore((s) => s.recentFiles);
  const playlists = usePlaylistStore((s) => s.playlists);
  const recent = playbackHistory.length > 0 ? playbackHistory : recentFiles;

  const showSearchResults = searchQuery.trim().length > 0;

  const filteredRecent = recent.filter((m) =>
    (m.name || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Welcome to <span className="text-gradient">StreamLite</span></h1>
          <p className="text-sm text-[#8892b0] mt-1">Your media, everywhere</p>
        </div>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder="Search media, channels..."
        />
      </div>

      {showSearchResults ? (
        <div className="space-y-3">
          <h2 className="text-lg font-semibold text-white/90 flex items-center gap-2">
            <Search size={18} className="text-[#4FC3F7]" />
            Results for "{searchQuery}"
          </h2>
          {filteredRecent.length === 0 ? (
            <div className="glass-card p-8 text-center">
              <p className="text-[#8892b0]">No results found</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {filteredRecent.slice(0, 8).map((m, i) => (
                <MediaCard key={i} media={m} compact />
              ))}
            </div>
          )}
        </div>
      ) : (
        <>
          <div>
            <h2 className="text-lg font-semibold text-white/90 mb-4">Quick Access</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {quickAccess.map(({ icon: Icon, label, gradient, path }) => (
                <button
                  key={label}
                  onClick={() => navigate(path)}
                  className="glass-card p-5 flex items-center gap-4 hover:border-[#4FC3F7]/20 group"
                >
                  <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${gradient} flex items-center justify-center shadow-lg`}>
                    <Icon size={22} className="text-white" />
                  </div>
                  <span className="font-medium text-white/80 group-hover:text-white transition-colors">{label}</span>
                </button>
              ))}
            </div>
          </div>

          {recent.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-white/90 mb-4 flex items-center gap-2">
                <Clock size={18} className="text-[#4FC3F7]" />
                Recently Played
              </h2>
              <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                {recent.slice(0, 10).map((m, i) => (
                  <MediaCard key={i} media={m} compact />
                ))}
              </div>
            </div>
          )}

          {playlists.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-white/90 mb-4 flex items-center gap-2">
                <Tv size={18} className="text-[#81C784]" />
                IPTV Channels
              </h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {playlists.flatMap((p) => p.channels || []).slice(0, 8).map((ch, i) => (
                  <button
                    key={i}
                    onClick={() => {
                      usePlayerStore.getState().loadMedia({
                        name: ch.name,
                        url: ch.url,
                        logo: ch.logo,
                        group: ch.group,
                        type: 'channel',
                      });
                      navigate('/player');
                    }}
                    className="glass-card p-3 flex items-center gap-3 hover:border-[#81C784]/20 group"
                  >
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#81C784]/20 to-[#66BB6A]/20 flex items-center justify-center shrink-0 overflow-hidden">
                      {ch.logo ? (
                        <img src={ch.logo} alt="" className="w-full h-full object-contain" onError={(e) => { e.target.style.display = 'none'; }} />
                      ) : (
                        <Tv size={18} className="text-[#81C784]" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1 text-left">
                      <p className="text-sm text-white/90 truncate">{ch.name}</p>
                      <p className="text-xs text-[#8892b0] truncate">{ch.group}</p>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {recent.length === 0 && (
            <div className="glass-card p-12 text-center">
              <Play size={48} className="text-[#4FC3F7]/30 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-white/70 mb-2">Start Playing</h3>
              <p className="text-sm text-[#8892b0] mb-6">Open a file, stream from network, or add an IPTV playlist</p>
              <div className="flex items-center justify-center gap-3">
                <button
                  onClick={async () => {
                    const result = await window.electronAPI?.openFileDialog();
                    if (result?.filePaths?.length) {
                      const meta = await window.electronAPI?.getMetadata(result.filePaths[0]);
                      if (meta) {
                        usePlayerStore.getState().loadMedia(meta);
                        navigate('/player');
                      }
                    }
                  }}
                  className="btn-primary"
                >
                  Open File
                </button>
                <button onClick={() => navigate('/network')} className="btn-secondary">
                  Network Stream
                </button>
                <button onClick={() => navigate('/library')} className="btn-secondary">
                  Browse Library
                </button>
              </div>
            </div>
          )}
        </>
      )}

      <FAB />
    </div>
  );
}
