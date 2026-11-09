using System.IO;
using LibVLCSharp.Shared;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.Services;

public class MediaPlayerService : IMediaPlayerService
{
    readonly LibVLC _libVlc;
    readonly MediaPlayer _player;
    MediaItem? _currentMedia;
    bool _disposed;

    public event EventHandler<PlaybackStatus>? StatusChanged;
    public event EventHandler<TimeSpan>? PositionChanged;
    public event EventHandler<TimeSpan>? DurationChanged;
    public event EventHandler<double>? VolumeChanged;
    public event EventHandler<bool>? MuteChanged;
    public event EventHandler<double>? PlaybackRateChanged;
    public event EventHandler<string>? ErrorOccurred;
    public event EventHandler? MediaEnded;
    public event EventHandler? MediaLoaded;
    public event EventHandler<IReadOnlyList<MediaTrackInfo>>? TracksChanged;
    public event EventHandler<float[]>? EqualizerChanged;

    public PlaybackStatus Status { get; private set; } = PlaybackStatus.Stopped;
    public TimeSpan Position => TimeSpan.FromMilliseconds(_player.Time);
    public TimeSpan Duration => TimeSpan.FromMilliseconds(_player.Length);
    public double Volume
    {
        get => _player.Volume / 100.0;
        private set => _player.Volume = (int)(Math.Clamp(value, 0, 1) * 100);
    }
    public bool IsMuted => _player.Mute;
    public double PlaybackRate => _player.Rate;
    public MediaItem? CurrentMedia => _currentMedia;

    public MediaPlayerService()
    {
        Core.Initialize();
        _libVlc = new LibVLC("--no-video-title-show", "--network-caching=2000", "--file-caching=1000");
        _player = new MediaPlayer(_libVlc);
        AttachEvents();
    }

    void AttachEvents()
    {
        _player.Playing += (_, _) => { Status = PlaybackStatus.Playing; StatusChanged?.Invoke(this, Status); };
        _player.Paused += (_, _) => { Status = PlaybackStatus.Paused; StatusChanged?.Invoke(this, Status); };
        _player.Stopped += (_, _) => { Status = PlaybackStatus.Stopped; StatusChanged?.Invoke(this, Status); };
        _player.EndReached += (_, _) => { Status = PlaybackStatus.Ended; StatusChanged?.Invoke(this, Status); MediaEnded?.Invoke(this, EventArgs.Empty); };
        _player.EncounteredError += (_, _) => { Status = PlaybackStatus.Error; StatusChanged?.Invoke(this, Status); ErrorOccurred?.Invoke(this, "Playback error"); };
        _player.Buffering += (_, args) => { if (args.Cache > 0 && Status is not PlaybackStatus.Playing) { Status = PlaybackStatus.Buffering; StatusChanged?.Invoke(this, Status); } };
        _player.LengthChanged += (_, _) => DurationChanged?.Invoke(this, Duration);
        _player.PositionChanged += (_, _) => PositionChanged?.Invoke(this, Position);
        _player.Muted += (_, _) => MuteChanged?.Invoke(this, true);
        _player.Unmuted += (_, _) => MuteChanged?.Invoke(this, false);
    }

    public bool LoadMedia(MediaItem media)
    {
        if (_disposed) return false;
        _currentMedia = media;
        var m = new Media(_libVlc, new Uri(media.FilePath));
        _player.Play(m);
        m.Dispose();
        MediaLoaded?.Invoke(this, EventArgs.Empty);
        return true;
    }

    public bool Play() { if (_disposed) return false; _player.Play(); return true; }
    public bool Pause() { if (_disposed) return false; _player.Pause(); return true; }
    public bool Stop() { if (_disposed) return false; _player.Stop(); _currentMedia = null; return true; }

    public bool Seek(TimeSpan position)
    {
        if (_disposed) return false;
        _player.Time = (long)position.TotalMilliseconds;
        return true;
    }

    public bool SetVolume(double volume)
    {
        if (_disposed) return false;
        _player.Volume = (int)(Math.Clamp(volume, 0, 1) * 100);
        VolumeChanged?.Invoke(this, Math.Clamp(volume, 0, 1));
        return true;
    }

    public bool ToggleMute() { if (_disposed) return false; _player.Mute = !_player.Mute; return true; }

    public bool SetPlaybackRate(double rate)
    {
        if (_disposed) return false;
        rate = Math.Clamp(rate, 0.125, 4.0);
        _player.SetRate((float)rate);
        PlaybackRateChanged?.Invoke(this, rate);
        return true;
    }

    public bool StepFrame(int frames = 1) { if (_disposed) return false; _player.NextFrame(); return true; }

    public bool SetAudioTrack(int trackId) { if (_disposed) return false; return _player.SetAudioTrack(trackId); }
    public bool SetSubtitleTrack(int trackId) { if (_disposed) return false; return _player.SetSpu(trackId); }
    public bool SetVideoTrack(int trackId) { if (_disposed) return false; return _player.SetVideoTrack(trackId); }

    Equalizer? _currentEq;

    public bool SetEqualizerEnabled(bool enable)
    {
        if (_disposed) return false;
        _currentEq?.Dispose();
        _currentEq = new Equalizer();
        _player.SetEqualizer(_currentEq);
        return true;
    }

    public bool SetEqualizerBand(int bandIndex, float value)
    {
        if (_disposed) return false;
        _currentEq ??= new Equalizer();
        _currentEq.SetAmp(Math.Clamp(value, -20f, 20f), (uint)bandIndex);
        _player.SetEqualizer(_currentEq);
        return true;
    }

    public bool SetPreamp(float preamp)
    {
        if (_disposed) return false;
        _currentEq ??= new Equalizer();
        _currentEq.SetPreamp(Math.Clamp(preamp, -20f, 20f));
        _player.SetEqualizer(_currentEq);
        return true;
    }

    public bool TakeScreenshot(string filePath)
    {
        if (_disposed) return false;
        try { _player.TakeSnapshot(0u, filePath, 0u, 0u); return true; }
        catch { return false; }
    }

    public IReadOnlyList<MediaTrackInfo> GetAudioTracks()
    {
        var tracks = _player.Media?.Tracks;
        if (tracks == null) return [];
        return tracks.Where(t => t.TrackType == LibVLCSharp.Shared.TrackType.Audio)
            .Select(t => new MediaTrackInfo { Id = (int)t.Id, Name = t.Description ?? $"Track {t.Id}", Type = Interfaces.TrackType.Audio })
            .ToList();
    }

    public IReadOnlyList<MediaTrackInfo> GetVideoTracks()
    {
        var tracks = _player.Media?.Tracks;
        if (tracks == null) return [];
        return tracks.Where(t => t.TrackType == LibVLCSharp.Shared.TrackType.Video)
            .Select(t => new MediaTrackInfo { Id = (int)t.Id, Name = t.Description ?? $"Track {t.Id}", Type = Interfaces.TrackType.Video })
            .ToList();
    }

    public IReadOnlyList<MediaTrackInfo> GetSubtitleTracks()
    {
        var tracks = _player.Media?.Tracks;
        if (tracks == null) return [];
        return tracks.Where(t => t.TrackType == LibVLCSharp.Shared.TrackType.Text)
            .Select(t => new MediaTrackInfo { Id = (int)t.Id, Name = t.Description ?? $"Track {t.Id}", Type = Interfaces.TrackType.Subtitle })
            .ToList();
    }

    public bool LoadSubtitle(string filePath)
    {
        if (_disposed) return false;
        return _player.AddSlave(MediaSlaveType.Subtitle, filePath, true);
    }

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        _player.Stop();
        _currentEq?.Dispose();
        _player.Dispose();
        _libVlc.Dispose();
    }
}
