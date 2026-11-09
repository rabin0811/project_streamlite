using System.Collections.ObjectModel;
using System.Windows.Input;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class PlayerViewModel : ObservableObject
{
    readonly IMediaPlayerService _player;
    readonly ILibraryService _library;
    readonly ISettingsService _settings;

    [ObservableProperty] bool _isPlaying;
    [ObservableProperty] bool _isPaused;
    [ObservableProperty] bool _isFullScreen;
    [ObservableProperty] bool _isTheaterMode;
    [ObservableProperty] bool _isMiniPlayer;
    [ObservableProperty] bool _controlsVisible = true;
    [ObservableProperty] bool _isSeeking;
    [ObservableProperty] MediaItem? _currentMedia;
    [ObservableProperty] string _currentPosition = "00:00";
    [ObservableProperty] string _totalDuration = "00:00";
    [ObservableProperty] double _positionPercent;
    [ObservableProperty] double _volume = 0.8;
    [ObservableProperty] bool _isMuted;
    [ObservableProperty] double _playbackRate = 1.0;
    [ObservableProperty] double _brightness = 1.0;
    [ObservableProperty] int _currentIndex = -1;
    [ObservableProperty] string? _statusText;

    public ObservableCollection<MediaItem> Playlist { get; } = [];
    public ObservableCollection<MediaTrackInfo> AudioTracks { get; } = [];
    public ObservableCollection<MediaTrackInfo> SubtitleTracks { get; } = [];
    public ObservableCollection<MediaTrackInfo> VideoTracks { get; } = [];

    public PlayerViewModel(IMediaPlayerService player, ILibraryService library, ISettingsService settings)
    {
        _player = player;
        _library = library;
        _settings = settings;
        Volume = _settings.Settings.DefaultVolume;
        PlaybackRate = _settings.Settings.DefaultSpeed;

        _player.PositionChanged += (_, pos) =>
        {
            CurrentPosition = FormatTimeSpan(pos);
            TotalDuration = FormatTimeSpan(_player.Duration);
            PositionPercent = _player.Duration.TotalSeconds > 0
                ? pos.TotalSeconds / _player.Duration.TotalSeconds * 100 : 0;
        };
        _player.StatusChanged += (_, status) =>
        {
            IsPlaying = status == PlaybackStatus.Playing;
            IsPaused = status == PlaybackStatus.Paused;
            StatusText = status switch
            {
                PlaybackStatus.Buffering => "Buffering...",
                PlaybackStatus.Error => "Error",
                _ => "",
            };
        };
        _player.MediaLoaded += (_, _) =>
        {
            CurrentMedia = _player.CurrentMedia;
            UpdateTracks();
        };
        _player.TracksChanged += (_, _) => UpdateTracks();
        _player.VolumeChanged += (_, vol) => Volume = vol;
        _player.MuteChanged += (_, muted) => IsMuted = muted;
    }

    void UpdateTracks()
    {
        AudioTracks.Clear();
        foreach (var t in _player.GetAudioTracks()) AudioTracks.Add(t);
        SubtitleTracks.Clear();
        foreach (var t in _player.GetSubtitleTracks()) SubtitleTracks.Add(t);
        VideoTracks.Clear();
        foreach (var t in _player.GetVideoTracks()) VideoTracks.Add(t);
    }

    public void PlayMedia(MediaItem media)
    {
        CurrentMedia = media;
        if (_player.LoadMedia(media))
        {
            _ = _library.UpdatePlaybackAsync(media);
        }
    }

    [RelayCommand] void TogglePlay() { if (IsPlaying) _player.Pause(); else _player.Play(); }
    [RelayCommand] void Play() => _player.Play();
    [RelayCommand] void Pause() => _player.Pause();
    [RelayCommand] void Stop() { _player.Stop(); CurrentMedia = null; }

    [RelayCommand]
    void Seek(double position)
    {
        var ts = TimeSpan.FromSeconds(position);
        _player.Seek(ts);
        IsSeeking = false;
    }

    [RelayCommand]
    void SeekRelative(object seconds) => _player.Seek(_player.Position + TimeSpan.FromSeconds(Convert.ToDouble(seconds)));

    [RelayCommand]
    void ToggleMute() => _player.ToggleMute();

    [RelayCommand]
    void ToggleFullScreen() => IsFullScreen = !IsFullScreen;

    [RelayCommand]
    void ToggleTheaterMode() => IsTheaterMode = !IsTheaterMode;

    [RelayCommand]
    void ToggleMiniPlayer() => IsMiniPlayer = !IsMiniPlayer;

    [RelayCommand]
    void FrameStep() => _player.StepFrame();

    [RelayCommand]
    void SetVolume(double vol)
    {
        Volume = Math.Clamp(vol, 0, 1);
        _player.SetVolume(Volume);
    }

    [RelayCommand]
    void SetPlaybackRate(double rate)
    {
        PlaybackRate = Math.Clamp(rate, 0.125, 4.0);
        _player.SetPlaybackRate(PlaybackRate);
    }

    [RelayCommand]
    void SetBrightness(double value) => Brightness = Math.Clamp(value, 0.1, 2.0);

    partial void OnVolumeChanged(double value) => _player.SetVolume(value);
    partial void OnPlaybackRateChanged(double value) => _player.SetPlaybackRate(value);
    partial void OnIsFullScreenChanged(bool value)
    {
        if (value) IsTheaterMode = false;
    }
    partial void OnIsTheaterModeChanged(bool value)
    {
        if (value) IsFullScreen = false;
    }

    static string FormatTimeSpan(TimeSpan ts)
    {
        if (ts <= TimeSpan.Zero) return "00:00";
        return ts.TotalHours >= 1 ? ts.ToString(@"h\:mm\:ss") : ts.ToString(@"mm\:ss");
    }
}
