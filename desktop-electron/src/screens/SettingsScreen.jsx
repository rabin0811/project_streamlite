import {
  Settings, Volume2, Gauge, Monitor, Clock, Heart,
  HardDrive, Palette, Info,
} from 'lucide-react';
import useSettingsStore from '../stores/settingsStore';

export default function SettingsScreen() {
  const {
    defaultVolume, defaultSpeed, rememberPosition, autoResume,
    gestureSeekSensitivity, sleepTimerDefault,
    updateSetting,
  } = useSettingsStore();

  const settingsGroups = [
    {
      title: 'Playback',
      items: [
        {
          icon: Volume2,
          label: 'Default Volume',
          control: (
            <div className="flex items-center gap-3">
              <input
                type="range"
                min={0}
                max={1}
                step={0.05}
                value={defaultVolume}
                onChange={(e) => updateSetting('defaultVolume', parseFloat(e.target.value))}
                className="slider volume-slider w-24"
                style={{ background: `linear-gradient(to right, #4FC3F7 ${defaultVolume * 100}%, rgba(255,255,255,0.15) ${defaultVolume * 100}%)` }}
              />
              <span className="text-xs text-[#8892b0] w-10">{Math.round(defaultVolume * 100)}%</span>
            </div>
          ),
        },
        {
          icon: Gauge,
          label: 'Default Speed',
          control: (
            <select
              value={defaultSpeed}
              onChange={(e) => updateSetting('defaultSpeed', parseFloat(e.target.value))}
              className="input-field w-20 text-sm py-1.5"
            >
              {[0.5, 0.75, 1, 1.25, 1.5, 2].map((s) => (
                <option key={s} value={s}>{s}x</option>
              ))}
            </select>
          ),
        },
        {
          icon: Clock,
          label: 'Remember Position',
          control: (
            <button
              onClick={() => updateSetting('rememberPosition', !rememberPosition)}
              className={`w-12 h-6 rounded-full transition-colors relative ${rememberPosition ? 'bg-[#4FC3F7]' : 'bg-white/10'}`}
            >
              <div className={`w-5 h-5 rounded-full bg-white absolute top-0.5 transition-transform ${rememberPosition ? 'translate-x-6' : 'translate-x-0.5'}`} />
            </button>
          ),
        },
        {
          icon: Heart,
          label: 'Auto Resume',
          control: (
            <button
              onClick={() => updateSetting('autoResume', !autoResume)}
              className={`w-12 h-6 rounded-full transition-colors relative ${autoResume ? 'bg-[#4FC3F7]' : 'bg-white/10'}`}
            >
              <div className={`w-5 h-5 rounded-full bg-white absolute top-0.5 transition-transform ${autoResume ? 'translate-x-6' : 'translate-x-0.5'}`} />
            </button>
          ),
        },
      ],
    },
    {
      title: 'Controls',
      items: [
        {
          icon: Monitor,
          label: 'Gesture Seek Sensitivity',
          control: (
            <div className="flex items-center gap-3">
              <input
                type="range"
                min={0.5}
                max={2}
                step={0.25}
                value={gestureSeekSensitivity}
                onChange={(e) => updateSetting('gestureSeekSensitivity', parseFloat(e.target.value))}
                className="slider volume-slider w-24"
                style={{ background: `linear-gradient(to right, #4FC3F7 ${((gestureSeekSensitivity - 0.5) / 1.5) * 100}%, rgba(255,255,255,0.15) ${((gestureSeekSensitivity - 0.5) / 1.5) * 100}%)` }}
              />
              <span className="text-xs text-[#8892b0] w-10">{gestureSeekSensitivity}x</span>
            </div>
          ),
        },
        {
          icon: Clock,
          label: 'Default Sleep Timer',
          control: (
            <select
              value={sleepTimerDefault}
              onChange={(e) => updateSetting('sleepTimerDefault', parseInt(e.target.value))}
              className="input-field w-24 text-sm py-1.5"
            >
              <option value={0}>Off</option>
              <option value={15}>15 min</option>
              <option value={30}>30 min</option>
              <option value={45}>45 min</option>
              <option value={60}>60 min</option>
            </select>
          ),
        },
      ],
    },
    {
      title: 'Storage',
      items: [
        {
          icon: HardDrive,
          label: 'Manage Data',
          control: (
            <button
              onClick={async () => {
                try {
                  if (window.electronAPI?.storageSave) {
                    await window.electronAPI.storageSave({ playlists: [], favorites: [], recentFiles: [], settings: {} });
                  } else {
                    localStorage.clear();
                  }
                  window.dispatchEvent(new CustomEvent('toast', { detail: { message: 'Storage cleared', type: 'success' } }));
                } catch (e) { /* ignore */ }
              }}
              className="btn-secondary text-xs py-1.5 px-3"
            >
              Clear All Data
            </button>
          ),
        },
      ],
    },
    {
      title: 'About',
      items: [
        {
          icon: Info,
          label: 'StreamLite Desktop',
          control: <span className="text-xs text-[#8892b0]">v1.0.0</span>,
        },
      ],
    },
  ];

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in">
      <div className="flex items-center gap-3">
        <Settings size={24} className="text-[#4FC3F7]" />
        <h1 className="text-2xl font-bold text-white">Settings</h1>
      </div>

      {settingsGroups.map((group) => (
        <div key={group.title}>
          <h2 className="text-sm font-semibold text-[#8892b0] uppercase tracking-wider mb-3">{group.title}</h2>
          <div className="glass-card divide-y divide-[#4FC3F7]/5">
            {group.items.map(({ icon: Icon, label, control }) => (
              <div key={label} className="flex items-center justify-between p-4">
                <div className="flex items-center gap-3">
                  <Icon size={18} className="text-[#8892b0]" />
                  <span className="text-sm text-white/80">{label}</span>
                </div>
                {control}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
