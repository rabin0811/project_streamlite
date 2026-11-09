using CommunityToolkit.Mvvm.ComponentModel;

namespace StreamLite.Models;

public enum PlaybackStatus { Stopped, Playing, Paused, Buffering, Error, Ended }

public class PlaybackState : ObservableObject
{
    private PlaybackStatus _status = PlaybackStatus.Stopped;
    private TimeSpan _position;
    private TimeSpan _duration;
    private double _volume = 0.8;
    private bool _isMuted;
    private double _playbackRate = 1.0;
    private int _audioTrack = -1;
    private int _subtitleTrack = -1;
    private bool _isFullScreen;
    private bool _isTheaterMode;
    private bool _isMiniPlayer;
    private MediaItem? _currentMedia;
    private int _currentMediaIndex = -1;
    private bool _isSeeking;
    private float _brightness = 1.0f;
    private int? _videoTrack;

    public PlaybackStatus Status { get => _status; set => SetProperty(ref _status, value); }
    public TimeSpan Position { get => _position; set => SetProperty(ref _position, value); }
    public TimeSpan Duration { get => _duration; set => SetProperty(ref _duration, value); }
    public double Volume { get => _volume; set => SetProperty(ref _volume, Math.Clamp(value, 0, 1)); }
    public bool IsMuted { get => _isMuted; set => SetProperty(ref _isMuted, value); }
    public double PlaybackRate { get => _playbackRate; set => SetProperty(ref _playbackRate, value); }
    public int AudioTrack { get => _audioTrack; set => SetProperty(ref _audioTrack, value); }
    public int SubtitleTrack { get => _subtitleTrack; set => SetProperty(ref _subtitleTrack, value); }
    public int? VideoTrack { get => _videoTrack; set => SetProperty(ref _videoTrack, value); }
    public bool IsFullScreen { get => _isFullScreen; set => SetProperty(ref _isFullScreen, value); }
    public bool IsTheaterMode { get => _isTheaterMode; set => SetProperty(ref _isTheaterMode, value); }
    public bool IsMiniPlayer { get => _isMiniPlayer; set => SetProperty(ref _isMiniPlayer, value); }
    public MediaItem? CurrentMedia { get => _currentMedia; set => SetProperty(ref _currentMedia, value); }
    public int CurrentMediaIndex { get => _currentMediaIndex; set => SetProperty(ref _currentMediaIndex, value); }
    public bool IsSeeking { get => _isSeeking; set => SetProperty(ref _isSeeking, value); }
    public float Brightness { get => _brightness; set => SetProperty(ref _brightness, Math.Clamp(value, 0.1f, 2.0f)); }

    public bool IsPlaying => Status == PlaybackStatus.Playing;
    public bool IsPaused => Status == PlaybackStatus.Paused;
    public bool CanPlay => CurrentMedia?.IsPlayable == true;
    public double PositionPercent => Duration.TotalSeconds > 0 ? Position.TotalSeconds / Duration.TotalSeconds * 100 : 0;
    public string PositionFormatted => FormatTimeSpan(Position);
    public string DurationFormatted => FormatTimeSpan(Duration);
    public string RemainFormatted => FormatTimeSpan(Duration - Position);

    static string FormatTimeSpan(TimeSpan ts)
    {
        if (ts <= TimeSpan.Zero) return "00:00";
        if (ts.TotalHours >= 1) return ts.ToString(@"h\:mm\:ss");
        return ts.ToString(@"mm\:ss");
    }
}
