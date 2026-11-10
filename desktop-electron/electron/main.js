const { app, BrowserWindow, ipcMain, dialog, screen } = require('electron');
const path = require('path');
const fs = require('fs');
const { VideoEngine } = require('./engine/index');

let mainWindow;
let videoEngine;

const userDataPath = app.getPath('userData');
const storePath = path.join(userDataPath, 'streamlite-data.json');

function loadStore() {
  try {
    if (fs.existsSync(storePath)) {
      return JSON.parse(fs.readFileSync(storePath, 'utf-8'));
    }
  } catch (e) { /* ignore */ }
  return { playlists: [], favorites: [], recent: [], settings: {} };
}

function saveStore(data) {
  try {
    fs.writeFileSync(storePath, JSON.stringify(data, null, 2));
  } catch (e) { /* ignore */ }
}

function createWindow() {
  const { width, height } = screen.getPrimaryDisplay().workAreaSize;
  mainWindow = new BrowserWindow({
    width: Math.min(1400, width),
    height: Math.min(900, height),
    minWidth: 900,
    minHeight: 600,
    frame: false,
    titleBarStyle: 'hidden',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: true,
    },
    backgroundColor: '#1a1a2e',
  });

  const isDev = process.argv.includes('--dev') || process.env.NODE_ENV === 'development';

  if (isDev) {
    mainWindow.loadURL('http://localhost:5173');
    mainWindow.webContents.openDevTools({ mode: 'detach' });
  } else {
    mainWindow.loadFile(path.join(__dirname, '..', 'dist', 'index.html'));
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
    if (videoEngine) videoEngine.destroy();
  });

  videoEngine = new VideoEngine(mainWindow);
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (videoEngine) videoEngine.destroy();
  if (process.platform !== 'darwin') app.quit();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});

ipcMain.handle('window:minimize', () => mainWindow?.minimize());
ipcMain.handle('window:maximize', () => {
  if (mainWindow?.isMaximized()) mainWindow.unmaximize();
  else mainWindow?.maximize();
});
ipcMain.handle('window:close', () => mainWindow?.close());
ipcMain.handle('window:isMaximized', () => mainWindow?.isMaximized());

ipcMain.handle('dialog:openFile', async (_, filters) => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile', 'multiSelections'],
    filters: filters || [
      { name: 'Media', extensions: ['mp4', 'mkv', 'avi', 'mov', 'wmv', 'flv', 'webm', 'm4v', 'mpg', 'mpeg', 'ts', 'm3u', 'm3u8'] },
      { name: 'Video', extensions: ['mp4', 'mkv', 'avi', 'mov', 'wmv', 'flv', 'webm', 'm4v'] },
      { name: 'Playlist', extensions: ['m3u', 'm3u8'] },
      { name: 'All Files', extensions: ['*'] },
    ],
  });
  return result;
});

ipcMain.handle('dialog:openFolder', async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openDirectory'],
  });
  return result;
});

ipcMain.handle('storage:load', () => loadStore());
ipcMain.handle('storage:save', (_, data) => saveStore(data));

ipcMain.handle('media:scanFolder', async (_, folderPath) => {
  const mediaFiles = [];
  const extensions = new Set(['.mp4', '.mkv', '.avi', '.mov', '.wmv', '.flv', '.webm', '.m4v', '.mpg', '.mpeg', '.ts', '.m3u', '.m3u8', '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']);

  async function scan(dir) {
    try {
      const entries = fs.readdirSync(dir, { withFileTypes: true });
      for (const entry of entries) {
        const fullPath = path.join(dir, entry.name);
        if (entry.isDirectory()) {
          if (!entry.name.startsWith('.')) await scan(fullPath);
        } else if (entry.isFile()) {
          const ext = path.extname(entry.name).toLowerCase();
          if (extensions.has(ext)) {
            const stat = fs.statSync(fullPath);
            mediaFiles.push({
              name: entry.name,
              path: fullPath,
              size: stat.size,
              modifiedAt: stat.mtime.toISOString(),
              ext,
              type: ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(ext) ? 'image' : 'video',
            });
          }
        }
      }
    } catch (e) { /* skip inaccessible dirs */ }
  }

  await scan(folderPath);
  return mediaFiles;
});

ipcMain.handle('media:getMetadata', async (_, filePath) => {
  try {
    const stat = fs.statSync(filePath);
    const ext = path.extname(filePath).toLowerCase();
    return {
      name: path.basename(filePath),
      path: filePath,
      size: stat.size,
      modifiedAt: stat.mtime.toISOString(),
      ext,
      type: ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(ext) ? 'image' : 'video',
    };
  } catch (e) {
    return null;
  }
});

ipcMain.handle('player:load', (_, uri) => videoEngine?.load(uri));
ipcMain.handle('player:play', () => videoEngine?.play());
ipcMain.handle('player:pause', () => videoEngine?.pause());
ipcMain.handle('player:stop', () => videoEngine?.stop());
ipcMain.handle('player:seek', (_, time) => videoEngine?.seek(time));
ipcMain.handle('player:setVolume', (_, vol) => videoEngine?.setVolume(vol));
ipcMain.handle('player:setSpeed', (_, speed) => videoEngine?.setSpeed(speed));
ipcMain.handle('player:getState', () => videoEngine?.getState());
ipcMain.handle('player:setFullscreen', (_, fs) => {
  if (mainWindow) mainWindow.setFullScreen(fs);
});
ipcMain.handle('player:isFullscreen', () => mainWindow?.isFullScreen());
