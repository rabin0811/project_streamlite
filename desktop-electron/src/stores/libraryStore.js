import { create } from 'zustand';

const useLibraryStore = create((set, get) => ({
  mediaFiles: [],
  currentFolder: null,
  favorites: [],
  recentFiles: [],
  viewMode: 'grid',
  activeTab: 'overview',
  loading: false,
  error: null,

  setViewMode: (mode) => set({ viewMode: mode }),
  setActiveTab: (tab) => set({ activeTab: tab }),

  scanFolder: async (folderPath) => {
    set({ loading: true, error: null });
    try {
      let files;
      if (window.electronAPI?.scanFolder) {
        files = await window.electronAPI.scanFolder(folderPath);
      }
      if (!files) files = [];
      set({
        mediaFiles: files,
        currentFolder: folderPath,
        loading: false,
      });
      return files;
    } catch (e) {
      set({ error: e.message, loading: false });
      return [];
    }
  },

  toggleFavorite: (media) => {
    const { favorites } = get();
    const exists = favorites.find((f) => f.path === media.path && f.url === media.url);
    let updated;
    if (exists) {
      updated = favorites.filter((f) => f.path !== media.path || f.url !== media.url);
    } else {
      updated = [{ ...media, favoritedAt: new Date().toISOString() }, ...favorites];
    }
    set({ favorites: updated });
    get().saveToDisk();
  },

  isFavorite: (media) => {
    const { favorites } = get();
    return favorites.some((f) => f.path === media.path && f.url === media.url);
  },

  addRecent: (media) => {
    const { recentFiles } = get();
    const updated = [
      { ...media, playedAt: new Date().toISOString() },
      ...recentFiles.filter((f) => f.path !== media.path || f.url !== media.url),
    ].slice(0, 50);
    set({ recentFiles: updated });
    get().saveToDisk();
  },

  saveToDisk: () => {
    const { favorites, recentFiles } = get();
    try {
      if (window.electronAPI?.storageSave) {
        window.electronAPI.storageSave({ favorites, recentFiles });
      } else {
        localStorage.setItem('streamlite-favorites', JSON.stringify(favorites));
        localStorage.setItem('streamlite-recent', JSON.stringify(recentFiles));
      }
    } catch (e) { /* ignore */ }
  },

  loadFromDisk: async () => {
    try {
      let data;
      if (window.electronAPI?.storageLoad) {
        data = await window.electronAPI.storageLoad();
      } else {
        const fav = localStorage.getItem('streamlite-favorites');
        const rec = localStorage.getItem('streamlite-recent');
        if (fav || rec) {
          data = {
            favorites: fav ? JSON.parse(fav) : [],
            recentFiles: rec ? JSON.parse(rec) : [],
          };
        }
      }
      if (data) {
        set({
          favorites: data.favorites || [],
          recentFiles: data.recentFiles || [],
        });
      }
    } catch (e) { /* ignore */ }
  },
}));

export default useLibraryStore;
