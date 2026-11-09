import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Film, Image, FolderOpen, Grid, List, Heart, Clock,
  ChevronRight, Search,
} from 'lucide-react';
import SearchBar from '../components/SearchBar';
import MediaCard from '../components/MediaCard';
import useLibraryStore from '../stores/libraryStore';
import usePlayerStore from '../stores/playerStore';

const tabs = [
  { id: 'overview', label: 'Overview', icon: Film },
  { id: 'videos', label: 'Videos', icon: Film },
  { id: 'images', label: 'Images', icon: Image },
];

export default function LibraryScreen() {
  const navigate = useNavigate();
  const [browsePath, setBrowsePath] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const {
    mediaFiles, favorites, recentFiles, viewMode, activeTab, loading,
    scanFolder, setViewMode, setActiveTab, loadFromDisk,
  } = useLibraryStore();

  useEffect(() => { loadFromDisk(); }, []);

  const handleBrowse = async () => {
    const result = await window.electronAPI?.openFolderDialog();
    if (result?.filePaths?.length) {
      setBrowsePath(result.filePaths[0]);
      scanFolder(result.filePaths[0]);
    }
  };

  const videos = mediaFiles.filter((f) => f.type === 'video' || !/\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(f.name));
  const images = mediaFiles.filter((f) => f.type === 'image' || /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(f.name));

  const filterBySearch = (items) => {
    if (!searchQuery) return items;
    const q = searchQuery.toLowerCase();
    return items.filter((m) => (m.name || '').toLowerCase().includes(q));
  };

  const renderMediaGrid = (items) => {
    const filtered = filterBySearch(items);
    if (filtered.length === 0) {
      return (
        <div className="col-span-full text-center py-12">
          <Film size={36} className="text-[#8892b0]/30 mx-auto mb-3" />
          <p className="text-[#8892b0] text-sm">No media found</p>
        </div>
      );
    }
    return filtered.map((m, i) => (
      <MediaCard key={i} media={m} showSize showDate />
    ));
  };

  const renderListView = (items) => {
    const filtered = filterBySearch(items);
    if (filtered.length === 0) {
      return (
        <div className="text-center py-12 text-[#8892b0] text-sm">No media found</div>
      );
    }
    return (
      <div className="space-y-1">
        {filtered.map((m, i) => (
          <div
            key={i}
            onClick={() => {
              usePlayerStore.getState().loadMedia(m);
              navigate('/player');
            }}
            className="flex items-center gap-4 p-3 rounded-lg hover:bg-white/5 cursor-pointer transition-colors group"
          >
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#16213e] to-[#253354] flex items-center justify-center shrink-0">
              <Film size={18} className="text-[#4FC3F7]" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm text-white/90 truncate">{m.name}</p>
              <p className="text-xs text-[#8892b0]">{m.size ? `${(m.size / 1024 / 1024).toFixed(1)} MB` : ''}</p>
            </div>
            <span className="text-xs text-[#8892b0]">{new Date(m.modifiedAt || Date.now()).toLocaleDateString()}</span>
            <ChevronRight size={16} className="text-[#8892b0] opacity-0 group-hover:opacity-100 transition-opacity" />
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Library</h1>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setViewMode(viewMode === 'grid' ? 'list' : 'grid')}
            className="btn-ghost p-2"
            title={viewMode === 'grid' ? 'List view' : 'Grid view'}
          >
            {viewMode === 'grid' ? <List size={18} /> : <Grid size={18} />}
          </button>
          <button onClick={handleBrowse} className="btn-primary flex items-center gap-2">
            <FolderOpen size={16} />
            Browse Folder
          </button>
        </div>
      </div>

      <div className="flex gap-2">
        {tabs.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => setActiveTab(id)}
            className={`tab flex items-center gap-2 ${activeTab === id ? 'tab-active' : ''}`}
          >
            <Icon size={16} />
            {label}
          </button>
        ))}
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder="Search library..."
        />
      </div>

      {browsePath && (
        <div className="glass-card p-3 flex items-center gap-2 text-sm text-[#8892b0]">
          <FolderOpen size={14} className="text-[#4FC3F7]" />
          {browsePath}
        </div>
      )}

      {activeTab === 'overview' && (
        <div className="space-y-8">
          {favorites.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-white/90 mb-4 flex items-center gap-2">
                <Heart size={18} className="text-[#F48FB1]" />
                Favorites
              </h2>
              <div className={viewMode === 'grid'
                ? 'grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4'
                : 'space-y-1'
              }>
                {viewMode === 'grid' ? renderMediaGrid(favorites) : renderListView(favorites)}
              </div>
            </div>
          )}

          {recentFiles.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-white/90 mb-4 flex items-center gap-2">
                <Clock size={18} className="text-[#4FC3F7]" />
                Recently Played
              </h2>
              <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                {recentFiles.slice(0, 10).map((m, i) => (
                  <MediaCard key={i} media={m} compact />
                ))}
              </div>
            </div>
          )}

          {!browsePath && favorites.length === 0 && (
            <div className="glass-card p-12 text-center">
              <FolderOpen size={48} className="text-[#4FC3F7]/30 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-white/70 mb-2">Browse Your Media</h3>
              <p className="text-sm text-[#8892b0] mb-6">Select a folder to scan for media files</p>
              <button onClick={handleBrowse} className="btn-primary">
                Browse Folder
              </button>
            </div>
          )}
        </div>
      )}

      {activeTab === 'videos' && (
        <div>
          {!browsePath ? (
            <div className="glass-card p-12 text-center">
              <Film size={48} className="text-[#4FC3F7]/30 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-white/70 mb-2">No Videos Scanned</h3>
              <p className="text-sm text-[#8892b0] mb-6">Browse a folder to discover video files</p>
              <button onClick={handleBrowse} className="btn-primary">
                Browse Folder
              </button>
            </div>
          ) : loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="w-10 h-10 border-2 border-[#4FC3F7] border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <div className={viewMode === 'grid'
              ? 'grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4'
              : 'space-y-1'
            }>
              {viewMode === 'grid' ? renderMediaGrid(videos) : renderListView(videos)}
            </div>
          )}
        </div>
      )}

      {activeTab === 'images' && (
        <div>
          {!browsePath ? (
            <div className="glass-card p-12 text-center">
              <Image size={48} className="text-[#F48FB1]/30 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-white/70 mb-2">No Images Scanned</h3>
              <p className="text-sm text-[#8892b0] mb-6">Browse a folder to discover image files</p>
              <button onClick={handleBrowse} className="btn-primary">
                Browse Folder
              </button>
            </div>
          ) : loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="w-10 h-10 border-2 border-[#4FC3F7] border-t-transparent rounded-full animate-spin" />
            </div>
          ) : (
            <div className="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
              {images.length === 0 ? (
                <div className="col-span-full text-center py-12 text-[#8892b0] text-sm">No images found</div>
              ) : (
                images.map((m, i) => (
                  <div key={i} className="glass-card overflow-hidden group cursor-pointer aspect-square">
                    <div className="w-full h-full bg-gradient-to-br from-[#16213e] to-[#253354] flex items-center justify-center">
                      <Image size={32} className="text-[#F48FB1]/30" />
                    </div>
                    <div className="absolute bottom-0 left-0 right-0 p-2 bg-gradient-to-t from-black/80 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
                      <p className="text-xs text-white/90 truncate">{m.name}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
