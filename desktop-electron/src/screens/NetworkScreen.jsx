import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Globe, Tv, Link, FolderOpen, Upload, X, Search, Play,
  ChevronDown, Save, Trash2, ExternalLink, Server, HardDrive,
} from 'lucide-react';
import SearchBar from '../components/SearchBar';
import usePlaylistStore from '../stores/playlistStore';
import usePlayerStore from '../stores/playerStore';

const connectionTabs = [
  { id: 'url', label: 'URL', icon: Link },
  { id: 'smb', label: 'SMB', icon: Server },
  { id: 'ftp', label: 'FTP', icon: HardDrive },
  { id: 'webdav', label: 'WebDAV', icon: Upload },
];

export default function NetworkScreen() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('url');
  const [streamUrl, setStreamUrl] = useState('');
  const [playlistUrl, setPlaylistUrl] = useState('');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [importMode, setImportMode] = useState('stream');
  const [adding, setAdding] = useState(false);

  const {
    playlists, channels, filteredChannels, selectedGroup, groups,
    searchQuery, loading, error,
    addPlaylist, removePlaylist, setSelectedGroup, setSearchQuery,
    loadFromDisk,
  } = usePlaylistStore();

  useEffect(() => { loadFromDisk(); }, []);

  const handlePlayStream = () => {
    if (!streamUrl.trim()) return;
    usePlayerStore.getState().loadMedia({
      name: streamUrl.split('/').pop() || 'Network Stream',
      url: streamUrl.trim(),
      type: 'stream',
    });
    navigate('/player');
  };

  const handleAddPlaylist = async () => {
    if (!playlistUrl.trim()) return;
    setAdding(true);
    try {
      await addPlaylist(playlistUrl.trim());
      setPlaylistUrl('');
      setShowAddDialog(false);
    } catch (e) {
      // error handled by store
    }
    setAdding(false);
  };

  const handlePlayChannel = (ch) => {
    usePlayerStore.getState().loadMedia({
      name: ch.name,
      url: ch.url,
      logo: ch.logo,
      group: ch.group,
      type: 'channel',
    });
    navigate('/player');
  };

  const handlePlaylistFile = async () => {
    const result = await window.electronAPI?.openFileDialog([{ name: 'Playlist', extensions: ['m3u', 'm3u8'] }]);
    if (result?.filePaths?.length) {
      setAdding(true);
      try {
        const { parseM3U } = await import('../utils/m3uParser');
        const filePath = result.filePaths[0];
        const response = await fetch(`file://${filePath}`);
        const text = await response.text();
        const channels = parseM3U(text, filePath);
        const { addPlaylist } = usePlaylistStore.getState();
        if (channels.length > 0) {
          const playlist = {
            url: filePath,
            name: filePath.split('\\').pop()?.replace(/\.m3u8?$/i, '') || 'Local Playlist',
            channels,
            channelCount: channels.length,
            addedAt: new Date().toISOString(),
          };
          const current = usePlaylistStore.getState().playlists;
          usePlaylistStore.getState().setPlaylists([...current, playlist]);
          usePlaylistStore.getState().saveToDisk();
        }
      } catch (e) { /* ignore */ }
      setAdding(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Network & IPTV</h1>
          <p className="text-sm text-[#8892b0] mt-1">Stream from network or add IPTV playlists</p>
        </div>
        {playlists.length > 0 && (
          <button onClick={() => setShowAddDialog(true)} className="btn-primary flex items-center gap-2">
            <Globe size={16} />
            Add Playlist
          </button>
        )}
      </div>

      {error && (
        <div className="glass-card p-4 border-[#F48FB1]/30 flex items-center gap-3">
          <X size={18} className="text-[#F48FB1]" />
          <span className="text-sm text-[#F48FB1]">{error}</span>
        </div>
      )}

      {/* Connection tabs */}
      <div className="flex gap-2">
        {connectionTabs.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => setActiveTab(id)}
            className={`tab flex items-center gap-2 ${activeTab === id ? 'tab-active' : ''}`}
          >
            <Icon size={16} />
            {label}
          </button>
        ))}
      </div>

      {/* URL Connection Tab */}
      {activeTab === 'url' && (
        <div className="space-y-4">
          {!showAddDialog && playlists.length === 0 && (
            <div className="glass-card p-8 text-center space-y-6">
              <Globe size={48} className="text-[#4FC3F7]/30 mx-auto" />
              <div>
                <h3 className="text-lg font-medium text-white/70 mb-1">No Playlists Added</h3>
                <p className="text-sm text-[#8892b0]">Enter a stream URL or add an M3U playlist to get started</p>
              </div>
              <div className="flex items-center justify-center gap-3">
                <button
                  onClick={() => { setShowAddDialog(true); setImportMode('stream'); }}
                  className="btn-primary flex items-center gap-2"
                >
                  <Link size={16} />
                  Add Stream
                </button>
                <button
                  onClick={() => { setShowAddDialog(true); setImportMode('playlist'); }}
                  className="btn-secondary flex items-center gap-2"
                >
                  <Globe size={16} />
                  Add Playlist
                </button>
              </div>
            </div>
          )}

          {/* Add dialog */}
          {showAddDialog && (
            <div className="glass-card p-6 space-y-4 animate-scale-in">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold text-white/90">Add Network Source</h3>
                <button onClick={() => setShowAddDialog(false)} className="btn-ghost p-1">
                  <X size={18} />
                </button>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setImportMode('stream')}
                  className={`tab flex items-center gap-2 ${importMode === 'stream' ? 'tab-active' : ''}`}
                >
                  <Link size={16} />
                  Stream URL
                </button>
                <button
                  onClick={() => setImportMode('playlist')}
                  className={`tab flex items-center gap-2 ${importMode === 'playlist' ? 'tab-active' : ''}`}
                >
                  <Globe size={16} />
                  M3U Playlist
                </button>
              </div>

              {importMode === 'stream' ? (
                <div className="space-y-3">
                  <label className="text-sm text-[#8892b0]">Stream URL</label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={streamUrl}
                      onChange={(e) => setStreamUrl(e.target.value)}
                      placeholder="https://example.com/stream.m3u8"
                      className="input-field"
                    />
                    <button onClick={handlePlayStream} disabled={!streamUrl.trim()} className="btn-primary">
                      Play
                    </button>
                  </div>
                </div>
              ) : (
                <div className="space-y-3">
                  <label className="text-sm text-[#8892b0]">M3U Playlist URL</label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={playlistUrl}
                      onChange={(e) => setPlaylistUrl(e.target.value)}
                      placeholder="https://example.com/playlist.m3u"
                      className="input-field"
                    />
                    <button onClick={handleAddPlaylist} disabled={!playlistUrl.trim() || adding} className="btn-primary">
                      {adding ? 'Adding...' : 'Add'}
                    </button>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-[#8892b0]">
                    <span className="w-full border-t border-[#4FC3F7]/10" />
                    <span>or</span>
                    <span className="w-full border-t border-[#4FC3F7]/10" />
                  </div>
                  <button onClick={handlePlaylistFile} className="btn-secondary flex items-center gap-2 w-full justify-center">
                    <FolderOpen size={16} />
                    Browse Local File
                  </button>
                </div>
              )}
            </div>
          )}

          {/* Saved Playlists */}
          {playlists.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-white/90 flex items-center gap-2">
                <Tv size={18} className="text-[#81C784]" />
                Saved Playlists
              </h2>

              {playlists.map((pl, idx) => (
                <div key={idx} className="glass-card p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#81C784]/20 to-[#66BB6A]/20 flex items-center justify-center">
                        <Tv size={20} className="text-[#81C784]" />
                      </div>
                      <div>
                        <p className="text-sm font-medium text-white/90">{pl.name}</p>
                        <p className="text-xs text-[#8892b0]">{pl.channelCount} channels &middot; {new Date(pl.addedAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                    <button onClick={() => removePlaylist(pl.url)} className="btn-ghost p-2 text-[#F48FB1]">
                      <Trash2 size={16} />
                    </button>
                  </div>

                  <div className="flex gap-2 flex-wrap mb-4">
                    {groups.map((g) => (
                      <button
                        key={g}
                        onClick={() => setSelectedGroup(g)}
                        className={`chip ${selectedGroup === g ? 'chip-active' : 'chip-inactive'}`}
                      >
                        {g} {g !== 'All' && `(${channels.filter((c) => c.group === g).length})`}
                      </button>
                    ))}
                  </div>

                  <div className="max-w-md mb-4">
                    <SearchBar
                      value={searchQuery}
                      onChange={setSearchQuery}
                      placeholder="Search channels..."
                    />
                  </div>

                  {loading ? (
                    <div className="flex items-center justify-center py-8">
                      <div className="w-8 h-8 border-2 border-[#4FC3F7] border-t-transparent rounded-full animate-spin" />
                    </div>
                  ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 max-h-[500px] overflow-y-auto pr-1">
                      {filteredChannels.length === 0 ? (
                        <div className="col-span-full text-center py-8 text-[#8892b0] text-sm">
                          No channels found
                        </div>
                      ) : (
                        filteredChannels.map((ch, i) => (
                          <div
                            key={i}
                            onClick={() => handlePlayChannel(ch)}
                            className="glass-card p-3 flex items-center gap-3 cursor-pointer hover:border-[#4FC3F7]/25 group"
                          >
                            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#16213e] to-[#253354] flex items-center justify-center shrink-0 overflow-hidden">
                              {ch.logo ? (
                                <img src={ch.logo} alt="" className="w-full h-full object-contain" onError={(e) => { e.target.style.display = 'none'; }} />
                              ) : (
                                <Tv size={18} className="text-[#8892b0]" />
                              )}
                            </div>
                            <div className="min-w-0 flex-1">
                              <p className="text-sm text-white/90 truncate">{ch.name}</p>
                              <p className="text-xs text-[#8892b0] truncate">{ch.group}</p>
                            </div>
                            <Play size={16} className="text-[#4FC3F7] opacity-0 group-hover:opacity-100 transition-all shrink-0" />
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* SMB Tab */}
      {activeTab === 'smb' && (
        <div className="glass-card p-8 text-center">
          <Server size={48} className="text-[#8892b0]/30 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-white/70 mb-2">SMB Network Shares</h3>
          <p className="text-sm text-[#8892b0] mb-6">Browse network shares and stream media files directly</p>
          <div className="space-y-3 max-w-md mx-auto">
            <input type="text" placeholder="//server/share" className="input-field" />
            <div className="flex gap-2">
              <input type="text" placeholder="Username (optional)" className="input-field" />
              <input type="password" placeholder="Password" className="input-field" />
            </div>
            <button className="btn-primary w-full">Connect</button>
          </div>
        </div>
      )}

      {/* FTP Tab */}
      {activeTab === 'ftp' && (
        <div className="glass-card p-8 text-center">
          <HardDrive size={48} className="text-[#8892b0]/30 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-white/70 mb-2">FTP Connection</h3>
          <p className="text-sm text-[#8892b0] mb-6">Connect to FTP servers and stream media</p>
          <div className="space-y-3 max-w-md mx-auto">
            <input type="text" placeholder="ftp://server.com" className="input-field" />
            <div className="flex gap-2">
              <input type="text" placeholder="Username" className="input-field" />
              <input type="password" placeholder="Password" className="input-field" />
            </div>
            <div className="flex gap-2">
              <input type="number" placeholder="Port (21)" className="input-field" />
            </div>
            <button className="btn-primary w-full">Connect</button>
          </div>
        </div>
      )}

      {/* WebDAV Tab */}
      {activeTab === 'webdav' && (
        <div className="glass-card p-8 text-center">
          <Upload size={48} className="text-[#8892b0]/30 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-white/70 mb-2">WebDAV Connection</h3>
          <p className="text-sm text-[#8892b0] mb-6">Connect to WebDAV servers and stream media</p>
          <div className="space-y-3 max-w-md mx-auto">
            <input type="text" placeholder="https://webdav.server.com" className="input-field" />
            <div className="flex gap-2">
              <input type="text" placeholder="Username" className="input-field" />
              <input type="password" placeholder="Password" className="input-field" />
            </div>
            <button className="btn-primary w-full">Connect</button>
          </div>
        </div>
      )}
    </div>
  );
}
