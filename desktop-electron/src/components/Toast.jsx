import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function Toast({ toast, onClose }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (toast) {
      setVisible(true);
      const timer = setTimeout(() => {
        setVisible(false);
        setTimeout(onClose, 300);
      }, 2800);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  if (!toast) return null;

  const icons = {
    success: <CheckCircle size={18} className="text-[#81C784]" />,
    error: <AlertCircle size={18} className="text-[#F48FB1]" />,
    info: <Info size={18} className="text-[#4FC3F7]" />,
  };

  return (
    <div
      className={`fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999] transition-all duration-300 ${
        visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
      }`}
    >
      <div className="glass-strong rounded-xl px-5 py-3 flex items-center gap-3 shadow-xl min-w-[280px] max-w-md">
        {icons[toast.type] || icons.info}
        <span className="text-sm text-white/90 flex-1">{toast.message}</span>
        <button onClick={() => { setVisible(false); setTimeout(onClose, 300); }} className="text-white/40 hover:text-white/70 transition-colors">
          <X size={16} />
        </button>
      </div>
    </div>
  );
}

export function showToast(message, type = 'info') {
  window.dispatchEvent(new CustomEvent('toast', { detail: { message, type } }));
}
