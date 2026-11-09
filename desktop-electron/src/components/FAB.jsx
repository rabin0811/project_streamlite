import { useState } from 'react';
import { Plus, Globe, FileUp, FolderOpen } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function FAB() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const actions = [
    { icon: Globe, label: 'Network Stream', action: () => navigate('/network') },
    { icon: FileUp, label: 'Pick File', action: async () => {
      try {
        const result = await window.electronAPI?.openFileDialog();
        if (result?.filePaths?.length) {
          const { default: usePlayerStore } = await import('../stores/playerStore');
          const meta = await window.electronAPI?.getMetadata(result.filePaths[0]);
          usePlayerStore.getState().loadMedia(meta || { name: result.filePaths[0].split('\\').pop(), path: result.filePaths[0] });
          navigate('/player');
        }
      } catch (e) { /* ignore */ }
    }},
    { icon: FolderOpen, label: 'Library', action: () => navigate('/library') },
  ];

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">
      {open && actions.map(({ icon: Icon, label, action }) => (
        <button
          key={label}
          onClick={() => { action(); setOpen(false); }}
          className="glass-strong rounded-xl px-4 py-2.5 flex items-center gap-3 text-sm text-white/90 hover:text-white shadow-lg animate-slide-up hover:border-[#4FC3F7]/30 transition-all"
        >
          <Icon size={18} className="text-[#4FC3F7]" />
          <span className="whitespace-nowrap">{label}</span>
        </button>
      ))}
      <button
        onClick={() => setOpen(!open)}
        className={`w-14 h-14 rounded-full bg-gradient-to-br from-[#4FC3F7] to-[#29B6F6] shadow-lg shadow-[#4FC3F7]/30 flex items-center justify-center transition-all duration-300 ${
          open ? 'rotate-45' : ''
        }`}
      >
        <Plus size={28} className="text-white" />
      </button>
    </div>
  );
}
