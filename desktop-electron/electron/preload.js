const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  // Window controls
  minimize: () => ipcRenderer.invoke('window:minimize'),
  maximize: () => ipcRenderer.invoke('window:maximize'),
  close: () => ipcRenderer.invoke('window:close'),
  isMaximized: () => ipcRenderer.invoke('window:isMaximized'),

  // Dialogs
  openFileDialog: (filters) => ipcRenderer.invoke('dialog:openFile', filters),
  openFolderDialog: () => ipcRenderer.invoke('dialog:openFolder'),

  // Storage
  storageLoad: () => ipcRenderer.invoke('storage:load'),
  storageSave: (data) => ipcRenderer.invoke('storage:save', data),

  // Media scanning
  scanFolder: (folderPath) => ipcRenderer.invoke('media:scanFolder', folderPath),
  getMetadata: (filePath) => ipcRenderer.invoke('media:getMetadata', filePath),

  // Player controls
  playerLoad: (uri) => ipcRenderer.invoke('player:load', uri),
  playerPlay: () => ipcRenderer.invoke('player:play'),
  playerPause: () => ipcRenderer.invoke('player:pause'),
  playerStop: () => ipcRenderer.invoke('player:stop'),
  playerSeek: (time) => ipcRenderer.invoke('player:seek', time),
  playerSetVolume: (vol) => ipcRenderer.invoke('player:setVolume', vol),
  playerSetSpeed: (speed) => ipcRenderer.invoke('player:setSpeed', speed),
  playerGetState: () => ipcRenderer.invoke('player:getState'),
  playerSetFullscreen: (fs) => ipcRenderer.invoke('player:setFullscreen', fs),
  playerIsFullscreen: () => ipcRenderer.invoke('player:isFullscreen'),

  // Events from main process
  onPlayerTimeUpdate: (callback) => {
    ipcRenderer.on('player:timeUpdate', (_, data) => callback(data));
  },
  onPlayerStateChange: (callback) => {
    ipcRenderer.on('player:stateChange', (_, data) => callback(data));
  },
  onPlayerEnded: (callback) => {
    ipcRenderer.on('player:ended', () => callback());
  },
  onPlayerError: (callback) => {
    ipcRenderer.on('player:error', (_, msg) => callback(msg));
  },

  removeAllListeners: (channel) => {
    ipcRenderer.removeAllListeners(channel);
  },
});
