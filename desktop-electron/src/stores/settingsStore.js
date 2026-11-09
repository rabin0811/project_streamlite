import { create } from 'zustand';

const useSettingsStore = create((set, get) => ({
  theme: 'dark',
  defaultVolume: 0.8,
  defaultSpeed: 1,
  rememberPosition: true,
  autoResume: true,
  gestureSeekSensitivity: 1,
  sleepTimerDefault: 30,

  updateSetting: (key, value) => {
    set({ [key]: value });
    get().saveToDisk();
  },

  saveToDisk: () => {
    const { theme, defaultVolume, defaultSpeed, rememberPosition, autoResume, gestureSeekSensitivity, sleepTimerDefault } = get();
    const settings = { theme, defaultVolume, defaultSpeed, rememberPosition, autoResume, gestureSeekSensitivity, sleepTimerDefault };
    try {
      if (window.electronAPI?.storageSave) {
        window.electronAPI.storageSave({ settings });
      } else {
        localStorage.setItem('streamlite-settings', JSON.stringify(settings));
      }
    } catch (e) { /* ignore */ }
  },

  loadFromDisk: async () => {
    try {
      let data;
      if (window.electronAPI?.storageLoad) {
        data = await window.electronAPI.storageLoad();
      } else {
        const stored = localStorage.getItem('streamlite-settings');
        if (stored) data = { settings: JSON.parse(stored) };
      }
      if (data?.settings) {
        set(data.settings);
      }
    } catch (e) { /* ignore */ }
  },
}));

export default useSettingsStore;
