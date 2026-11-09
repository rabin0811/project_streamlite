import { Play, Heart, Clock, FolderOpen, Image } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { formatFileSize } from '../utils/formatTime';
import useLibraryStore from '../stores/libraryStore';
import usePlayerStore from '../stores/playerStore';

export default function MediaCard({ media, showSize = false, showDate = false, compact = false }) {
  const navigate = useNavigate();
  const isFavorite = useLibraryStore((s) => s.isFavorite);
  const toggleFavorite = useLibraryStore((s) => s.toggleFavorite);
  const loadMedia = usePlayerStore((s) => s.loadMedia);
  const fav = isFavorite(media);

  const handlePlay = (e) => {
    e.stopPropagation();
    loadMedia(media);
    navigate('/player');
  };

  const handleFav = (e) => {
    e.stopPropagation();
    toggleFavorite(media);
  };

  const isImage = media.type === 'image' || /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(media.name || media.path);

  if (compact) {
    return (
      <div
        onClick={handlePlay}
        className="glass-card p-3 flex items-center gap-3 cursor-pointer group min-w-[220px] shrink-0"
      >
        <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#4FC3F7]/20 to-[#F48FB1]/20 flex items-center justify-center shrink-0">
          {isImage ? <Image size={18} className="text-[#F48FB1]" /> : <Play size={18} className="text-[#4FC3F7] ml-0.5" />}
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-sm text-white/90 truncate">{media.name}</p>
          {media.duration && (
            <p className="text-xs text-[#8892b0] mt-0.5">{media.duration}</p>
          )}
        </div>
        <button onClick={handleFav} className="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
          <Heart size={14} className={fav ? 'text-[#F48FB1] fill-[#F48FB1]' : 'text-[#8892b0]'} />
        </button>
      </div>
    );
  }

  if (isImage) {
    return (
      <div className="glass-card overflow-hidden group cursor-pointer">
        <div className="aspect-video bg-gradient-to-br from-[#16213e] to-[#253354] flex items-center justify-center">
          <Image size={40} className="text-[#F48FB1]/40" />
          <span className="absolute text-xs text-white/30">Preview N/A</span>
        </div>
        <div className="p-3">
          <p className="text-sm text-white/90 truncate">{media.name}</p>
          <div className="flex items-center justify-between mt-1">
            <span className="text-xs text-[#8892b0]">Image</span>
            <button onClick={handleFav}>
              <Heart size={14} className={fav ? 'text-[#F48FB1] fill-[#F48FB1]' : 'text-[#8892b0]'} />
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="glass-card overflow-hidden group cursor-pointer" onClick={handlePlay}>
      <div className="aspect-video bg-gradient-to-br from-[#16213e] to-[#253354] flex items-center justify-center relative">
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/40 transition-all flex items-center justify-center">
          <div className="w-14 h-14 rounded-full bg-[#4FC3F7]/90 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all transform group-hover:scale-100 scale-90 shadow-lg">
            <Play size={24} className="text-white ml-1" />
          </div>
        </div>
        {media.duration && (
          <div className="absolute bottom-2 right-2 px-2 py-0.5 rounded-md bg-black/60 text-xs text-white/80 backdrop-blur-sm">
            {media.duration}
          </div>
        )}
      </div>
      <div className="p-3">
        <p className="text-sm text-white/90 truncate font-medium">{media.name}</p>
        <div className="flex items-center justify-between mt-1.5">
          <span className="text-xs text-[#8892b0]">
            {showSize && media.size ? formatFileSize(media.size) : 'Video'}
          </span>
          <div className="flex items-center gap-2">
            {showDate && media.modifiedAt && (
              <span className="text-xs text-[#8892b0] flex items-center gap-1">
                <Clock size={11} />
                {new Date(media.modifiedAt).toLocaleDateString()}
              </span>
            )}
            <button onClick={handleFav} className="opacity-0 group-hover:opacity-100 transition-opacity">
              <Heart size={14} className={fav ? 'text-[#F48FB1] fill-[#F48FB1]' : 'text-[#8892b0]'} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
