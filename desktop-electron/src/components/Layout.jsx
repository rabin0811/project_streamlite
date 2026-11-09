import { useNavigate, useLocation } from 'react-router-dom';
import { Film, Home, Network, Settings } from 'lucide-react';

const navItems = [
  { path: '/', icon: Home, label: 'Home' },
  { path: '/library', icon: Film, label: 'Library' },
  { path: '/network', icon: Network, label: 'Network' },
  { path: '/settings', icon: Settings, label: 'Settings' },
];

export default function Layout({ children }) {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div className="h-screen w-screen flex flex-col bg-[#1a1a2e] overflow-hidden">
      <div className="titlebar h-10 flex items-center justify-between px-4 bg-[#16213e] border-b border-[#4FC3F7]/10 shrink-0">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-full bg-gradient-to-br from-[#4FC3F7] to-[#F48FB1] flex items-center justify-center">
            <span className="text-white text-xs font-bold">S</span>
          </div>
          <span className="text-sm font-semibold text-white/80 tracking-wide">StreamLite</span>
        </div>
        <div className="flex items-center gap-1 titlebar-button">
          <button onClick={() => window.electronAPI?.minimize()} className="w-3 h-3 rounded-full bg-yellow-500 hover:brightness-110 transition-all" />
          <button onClick={() => window.electronAPI?.maximize()} className="w-3 h-3 rounded-full bg-green-500 hover:brightness-110 transition-all" />
          <button onClick={() => window.electronAPI?.close()} className="w-3 h-3 rounded-full bg-red-500 hover:brightness-110 transition-all" />
        </div>
      </div>
      <div className="flex flex-1 overflow-hidden">
        <nav className="w-16 bg-[#16213e] flex flex-col items-center py-3 gap-1 shrink-0 border-r border-[#4FC3F7]/10">
          {navItems.map(({ path, icon: Icon, label }) => {
            const isActive = location.pathname === path;
            return (
              <button
                key={path}
                onClick={() => navigate(path)}
                className={`w-12 h-12 rounded-xl flex flex-col items-center justify-center gap-0.5 transition-all ${
                  isActive
                    ? 'bg-[#4FC3F7]/15 text-[#4FC3F7]'
                    : 'text-[#8892b0] hover:text-white hover:bg-white/5'
                }`}
                title={label}
              >
                <Icon size={20} />
                <span className="text-[10px] font-medium">{label}</span>
              </button>
            );
          })}
        </nav>
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
