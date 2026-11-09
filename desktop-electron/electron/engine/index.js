class VideoEngine {
  constructor(mainWindow) {
    this.mainWindow = mainWindow;
    this.state = {
      playing: false,
      paused: false,
      duration: 0,
      currentTime: 0,
      volume: 1,
      speed: 1,
      mediaLoaded: false,
    };
    this.interval = null;
    this._useNative = false;
  }

  load(uri) {
    this.state.mediaLoaded = true;
    this.state.duration = 0;
    this.state.currentTime = 0;
    this.state.playing = false;
    this.state.paused = false;

    this.mainWindow.webContents.send('player:stateChange', {
      type: 'loaded',
      uri,
    });

    if (this._useNative && this.vlc) {
      // Native VLC playback
    } else {
      this.mainWindow.webContents.send('player:stateChange', {
        type: 'ready',
        uri,
      });
    }
  }

  play() {
    this.state.playing = true;
    this.state.paused = false;
    this._startTimeUpdates();
    this.mainWindow.webContents.send('player:stateChange', { type: 'playing' });
  }

  pause() {
    this.state.playing = false;
    this.state.paused = true;
    this._stopTimeUpdates();
    this.mainWindow.webContents.send('player:stateChange', { type: 'paused' });
  }

  stop() {
    this.state.playing = false;
    this.state.paused = false;
    this.state.mediaLoaded = false;
    this.state.currentTime = 0;
    this._stopTimeUpdates();
    this.mainWindow.webContents.send('player:stateChange', { type: 'stopped' });
  }

  seek(time) {
    this.state.currentTime = Math.max(0, Math.min(time, this.state.duration));
    this.mainWindow.webContents.send('player:stateChange', { type: 'seek', time: this.state.currentTime });
  }

  setVolume(vol) {
    this.state.volume = Math.max(0, Math.min(1, vol));
    this.mainWindow.webContents.send('player:stateChange', { type: 'volume', volume: this.state.volume });
  }

  setSpeed(speed) {
    this.state.speed = speed;
    this.mainWindow.webContents.send('player:stateChange', { type: 'speed', speed });
  }

  getState() {
    return { ...this.state };
  }

  _startTimeUpdates() {
    this._stopTimeUpdates();
    this.interval = setInterval(() => {
      if (this.state.playing) {
        this.state.currentTime += 0.25;
        if (this.state.currentTime >= this.state.duration && this.state.duration > 0) {
          this.state.currentTime = this.state.duration;
          this.state.playing = false;
          this._stopTimeUpdates();
          this.mainWindow.webContents.send('player:ended');
        } else {
          this.mainWindow.webContents.send('player:timeUpdate', {
            currentTime: this.state.currentTime,
            duration: this.state.duration,
          });
        }
      }
    }, 250);
  }

  _stopTimeUpdates() {
    if (this.interval) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }

  destroy() {
    this._stopTimeUpdates();
    this.state = null;
    this.mainWindow = null;
  }
}

module.exports = { VideoEngine };
