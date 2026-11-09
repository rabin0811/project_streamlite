import { Clock, X } from 'lucide-react';

const options = [
  { label: 'Off', value: 0 },
  { label: '15 min', value: 15 },
  { label: '30 min', value: 30 },
  { label: '45 min', value: 45 },
  { label: '1 hour', value: 60 },
  { label: '2 hours', value: 120 },
];

export default function SleepTimer({ onClose, onSet }) {
  return (
    <div className="absolute inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm" onClick={onClose}>
      <div className="glass-strong rounded-2xl p-6 min-w-[240px] animate-scale-in" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Clock size={18} className="text-[#4FC3F7]" />
            <span className="text-white font-medium">Sleep Timer</span>
          </div>
          <button onClick={onClose} className="btn-ghost p-1">
            <X size={18} className="text-[#8892b0]" />
          </button>
        </div>
        <div className="space-y-1">
          {options.map(({ label, value }) => (
            <button
              key={value}
              onClick={() => onSet(value)}
              className="block w-full text-left px-4 py-2.5 rounded-lg text-sm text-white/80 hover:text-white hover:bg-white/5 transition-colors"
            >
              {label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
