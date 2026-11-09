import { create } from 'zustand';

const usePlaylistStore = create((set, get) => ({
  playlists: [],
  channels: [],
  filteredChannels: [],
  selectedGroup: 'All',
  groups: ['All'],
  searchQuery: '',
  loading: false,
  error: null,

  setPlaylists: (playlists) => {
    const allChannels = playlists.flatMap((p) => p.channels || []);
    const groups = ['All', ...new Set(allChannels.map((c) => c.group).filter(Boolean))];
    set({
      playlists,
      channels: allChannels,
      filteredChannels: allChannels,
      groups,
      selectedGroup: 'All',
    });
  },

  addPlaylist: async (url) => {
    const { playlists } = get();
    const existing = playlists.find((p) => p.url === url);
    if (existing) throw new Error('Playlist already added');

    set({ loading: true, error: null });
    try {
      const { fetchM3U } = await import('../utils/m3uParser');
      const playlist = await fetchM3U(url);
      const updated = [...playlists, playlist];
      set({ playlists: updated, loading: false });
      get().setChannels(updated);
      get().saveToDisk();
      return playlist;
    } catch (e) {
      set({ error: e.message, loading: false });
      throw e;
    }
  },

  removePlaylist: (url) => {
    const { playlists } = get();
    const updated = playlists.filter((p) => p.url !== url);
    set({ playlists: updated });
    get().setChannels(updated);
    get().saveToDisk();
  },

  setChannels: (playlists) => {
    const allChannels = playlists.flatMap((p) => p.channels || []);
    const groups = ['All', ...new Set(allChannels.map((c) => c.group).filter(Boolean))];
    set({
      channels: allChannels,
      filteredChannels: allChannels,
      groups,
      selectedGroup: 'All',
    });
  },

  setSelectedGroup: (group) => {
    const { channels, searchQuery } = get();
    set({ selectedGroup: group });
    get()._applyFilters(group, searchQuery);
  },

  setSearchQuery: (query) => {
    const { channels, selectedGroup } = get();
    set({ searchQuery: query });
    get()._applyFilters(selectedGroup, query);
  },

  _applyFilters: (group, query) => {
    const { channels } = get();
    let filtered = channels;

    if (group && group !== 'All') {
      filtered = filtered.filter((c) => c.group === group);
    }

    if (query) {
      const q = query.toLowerCase();
      filtered = filtered.filter(
        (c) =>
          c.name.toLowerCase().includes(q) ||
          c.group.toLowerCase().includes(q) ||
          c.channelNumber.toString().includes(q)
      );
    }

    set({ filteredChannels: filtered });
  },

  saveToDisk: () => {
    const { playlists } = get();
    try {
      if (window.electronAPI?.storageSave) {
        window.electronAPI.storageSave({ playlists });
      } else {
        localStorage.setItem('streamlite-playlists', JSON.stringify(playlists));
      }
    } catch (e) { /* ignore */ }
  },

  loadFromDisk: async () => {
    try {
      let data;
      if (window.electronAPI?.storageLoad) {
        data = await window.electronAPI.storageLoad();
      } else {
        const stored = localStorage.getItem('streamlite-playlists');
        if (stored) data = { playlists: JSON.parse(stored) };
      }
      if (data?.playlists) {
        get().setPlaylists(data.playlists);
      }
    } catch (e) { /* ignore */ }
  },
}));

export default usePlaylistStore;
