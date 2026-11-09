using StreamLite.Models;

namespace StreamLite.Services.Interfaces;

public interface IMediaPlayerService : IDisposable
{
    event EventHandler<PlaybackStatus>? StatusChanged;
    event EventHandler<TimeSpan>? PositionChanged;
    event EventHandler<TimeSpan>? DurationChanged;
    event EventHandler<double>? VolumeChanged;
    event EventHandler<bool>? MuteChanged;
    event EventHandler<double>? PlaybackRateChanged;
    event EventHandler<string>? ErrorOccurred;
    event EventHandler? MediaEnded;
    event EventHandler? MediaLoaded;
    event EventHandler<IReadOnlyList<MediaTrackInfo>>? TracksChanged;
    event EventHandler<float[]>? EqualizerChanged;

    PlaybackStatus Status { get; }
    TimeSpan Position { get; }
    TimeSpan Duration { get; }
    double Volume { get; }
    bool IsMuted { get; }
    double PlaybackRate { get; }
    MediaItem? CurrentMedia { get; }

    bool LoadMedia(MediaItem media);
    bool Play();
    bool Pause();
    bool Stop();
    bool Seek(TimeSpan position);
    bool SetVolume(double volume);
    bool ToggleMute();
    bool SetPlaybackRate(double rate);
    bool StepFrame(int frames = 1);
    bool SetAudioTrack(int trackId);
    bool SetSubtitleTrack(int trackId);
    bool SetVideoTrack(int trackId);
    bool SetEqualizerEnabled(bool enable);
    bool SetEqualizerBand(int bandIndex, float value);
    bool SetPreamp(float preamp);
    bool TakeScreenshot(string filePath);
    IReadOnlyList<MediaTrackInfo> GetAudioTracks();
    IReadOnlyList<MediaTrackInfo> GetVideoTracks();
    IReadOnlyList<MediaTrackInfo> GetSubtitleTracks();
    bool LoadSubtitle(string filePath);
}

public class MediaTrackInfo
{
    public int Id { get; set; }
    public string? Name { get; set; }
    public string? Language { get; set; }
    public string? Description { get; set; }
    public TrackType Type { get; set; }
}

public enum TrackType { Video, Audio, Subtitle, Unknown }
