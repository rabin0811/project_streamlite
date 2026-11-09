using CommunityToolkit.Mvvm.ComponentModel;
using System.IO;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class SettingsViewModel : ObservableObject
{
    readonly ISettingsService _settings;
    readonly IThumbnailService _thumbnails;

    [ObservableProperty] bool _darkTheme = true;
    [ObservableProperty] double _defaultVolume = 0.8;
    [ObservableProperty] double _defaultSpeed = 1.0;
    [ObservableProperty] bool _hardwareDecoding = true;
    [ObservableProperty] bool _autoResume = true;
    [ObservableProperty] bool _rememberPosition = true;
    [ObservableProperty] bool _autoLoadSubtitles = true;
    [ObservableProperty] int _subtitleFontSize = 24;
    [ObservableProperty] bool _minimizeToTray;
    [ObservableProperty] bool _showNotifications = true;
    [ObservableProperty] bool _enableEqualizer;
    [ObservableProperty] float _preamp;
    [ObservableProperty] float[] _equalizerBands = new float[10];
    [ObservableProperty] long _thumbnailCacheSize;
    [ObservableProperty] string _thumbnailCacheFormatted = "";
    [ObservableProperty] string _librarySizeFormatted = "";
    [ObservableProperty] int _libraryFileCount;
    [ObservableProperty] string _databaseSizeFormatted = "";

    public SettingsViewModel(ISettingsService settings, IThumbnailService thumbnails)
    {
        _settings = settings;
        _thumbnails = thumbnails;
        _settings.SettingsChanged += (_, s) => LoadFromSettings(s);
        LoadFromSettings(_settings.Settings);
    }

    void LoadFromSettings(StreamLite.Models.AppSettings s)
    {
        DarkTheme = s.DarkTheme;
        DefaultVolume = s.DefaultVolume;
        DefaultSpeed = s.DefaultSpeed;
        HardwareDecoding = s.HardwareDecoding;
        AutoResume = s.AutoResume;
        RememberPosition = s.RememberPosition;
        AutoLoadSubtitles = s.AutoLoadSubtitles;
        SubtitleFontSize = s.SubtitleFontSize;
        MinimizeToTray = s.MinimizeToTray;
        ShowNotifications = s.ShowNotifications;
        EnableEqualizer = s.EnableEqualizer;
        Preamp = s.Preamp;
        EqualizerBands = s.EqualizerBands;
    }

    public async Task LoadStorageInfoAsync()
    {
        var cacheSize = _thumbnails.GetCacheSize();
        ThumbnailCacheSize = cacheSize;
        ThumbnailCacheFormatted = FormatSize(cacheSize);

        var dbPath = System.IO.Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "StreamLite", "streamlite.db");
        if (File.Exists(dbPath))
            DatabaseSizeFormatted = FormatSize(new FileInfo(dbPath).Length);
    }

    [RelayCommand]
    async Task ClearThumbnailCache()
    {
        _thumbnails.ClearCache();
        await LoadStorageInfoAsync();
    }

    [RelayCommand]
    async Task ResetSettings()
    {
        await _settings.ResetAsync();
        LoadFromSettings(_settings.Settings);
    }

    partial void OnDarkThemeChanged(bool value) => _ = _settings.SetValueAsync(nameof(DarkTheme), value);
    partial void OnDefaultVolumeChanged(double value) => _ = _settings.SetValueAsync(nameof(DefaultVolume), value);
    partial void OnDefaultSpeedChanged(double value) => _ = _settings.SetValueAsync(nameof(DefaultSpeed), value);
    partial void OnHardwareDecodingChanged(bool value) => _ = _settings.SetValueAsync(nameof(HardwareDecoding), value);
    partial void OnAutoResumeChanged(bool value) => _ = _settings.SetValueAsync(nameof(AutoResume), value);
    partial void OnRememberPositionChanged(bool value) => _ = _settings.SetValueAsync(nameof(RememberPosition), value);
    partial void OnAutoLoadSubtitlesChanged(bool value) => _ = _settings.SetValueAsync(nameof(AutoLoadSubtitles), value);
    partial void OnSubtitleFontSizeChanged(int value) => _ = _settings.SetValueAsync(nameof(SubtitleFontSize), value);
    partial void OnMinimizeToTrayChanged(bool value) => _ = _settings.SetValueAsync(nameof(MinimizeToTray), value);
    partial void OnShowNotificationsChanged(bool value) => _ = _settings.SetValueAsync(nameof(ShowNotifications), value);
    partial void OnEnableEqualizerChanged(bool value) => _ = _settings.SetValueAsync(nameof(EnableEqualizer), value);
    partial void OnPreampChanged(float value) => _ = _settings.SetValueAsync(nameof(Preamp), value);

    static string FormatSize(long bytes) => bytes switch
    {
        < 1024 => $"{bytes} B",
        < 1024 * 1024 => $"{bytes / 1024.0:F1} KB",
        < 1024L * 1024 * 1024 => $"{bytes / (1024.0 * 1024):F1} MB",
        _ => $"{bytes / (1024.0 * 1024 * 1024):F2} GB"
    };
}

