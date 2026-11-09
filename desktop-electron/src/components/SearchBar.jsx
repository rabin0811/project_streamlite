import { Search, X } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';

export default function SearchBar({ value, onChange, placeholder = 'Search...' }) {
  const [focused, setFocused] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    const handler = (e) => {
      if (e.key === 'Escape') {
        inputRef.current?.blur();
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  return (
    <div
      className={`flex items-center gap-2 rounded-xl transition-all duration-200 ${
        focused
          ? 'bg-[#253354] border border-[#4FC3F7]/30 shadow-lg shadow-[#4FC3F7]/5'
          : 'bg-[#253354]/60 border border-transparent'
      }`}
    >
      <Search size={18} className="text-[#8892b0] ml-3 shrink-0" />
      <input
        ref={inputRef}
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        placeholder={placeholder}
        className="bg-transparent border-none outline-none text-sm text-white/90 py-2.5 w-full placeholder:text-[#8892b0]"
      />
      {value && (
        <button onClick={() => onChange('')} className="mr-2 text-[#8892b0] hover:text-white transition-colors p-1">
          <X size={16} />
        </button>
      )}
    </div>
  );
}
