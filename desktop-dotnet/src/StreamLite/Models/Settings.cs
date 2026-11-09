using CommunityToolkit.Mvvm.ComponentModel;

namespace StreamLite.Models;

public class AppSettings : ObservableObject
{
    private bool _darkTheme = true;
    private bool _autoResume = true;
    private bool _rememberPosition = true;
    private double _defaultVolume = 0.8;
    private double _defaultSpeed = 1.0;
    private bool _hardwareDecoding = true;
    private string? _subtitleLanguage;
    private int _subtitleFontSize = 24;
    private bool _autoLoadSubtitles = true;
    private bool _minimizeToTray;
    private bool _showNotifications = true;
    private string _screenshotFormat = "PNG";
    private string? _screenshotFolder;
    private bool _monitorFolderChanges;
    private int _thumbnailQuality = 80;
    private bool _startFullScreen;
    private bool _rememberWindowPosition = true;
    private double _windowLeft;
    private double _windowTop;
    private double _windowWidth = 1280;
    private double _windowHeight = 720;
    private int _maxRecentFiles = 50;
    private bool _enableEqualizer;
    private float _preamp;
    private float[] _equalizerBands = new float[10];

    public bool DarkTheme { get => _darkTheme; set => SetProperty(ref _darkTheme, value); }
    public bool AutoResume { get => _autoResume; set => SetProperty(ref _autoResume, value); }
    public bool RememberPosition { get => _rememberPosition; set => SetProperty(ref _rememberPosition, value); }
    public double DefaultVolume { get => _defaultVolume; set => SetProperty(ref _defaultVolume, Math.Clamp(value, 0, 1)); }
    public double DefaultSpeed { get => _defaultSpeed; set => SetProperty(ref _defaultSpeed, value); }
    public bool HardwareDecoding { get => _hardwareDecoding; set => SetProperty(ref _hardwareDecoding, value); }
    public string? SubtitleLanguage { get => _subtitleLanguage; set => SetProperty(ref _subtitleLanguage, value); }
    public int SubtitleFontSize { get => _subtitleFontSize; set => SetProperty(ref _subtitleFontSize, Math.Clamp(value, 10, 72)); }
    public bool AutoLoadSubtitles { get => _autoLoadSubtitles; set => SetProperty(ref _autoLoadSubtitles, value); }
    public bool MinimizeToTray { get => _minimizeToTray; set => SetProperty(ref _minimizeToTray, value); }
    public bool ShowNotifications { get => _showNotifications; set => SetProperty(ref _showNotifications, value); }
    public string ScreenshotFormat { get => _screenshotFormat; set => SetProperty(ref _screenshotFormat, value); }
    public string? ScreenshotFolder { get => _screenshotFolder; set => SetProperty(ref _screenshotFolder, value); }
    public bool MonitorFolderChanges { get => _monitorFolderChanges; set => SetProperty(ref _monitorFolderChanges, value); }
    public int ThumbnailQuality { get => _thumbnailQuality; set => SetProperty(ref _thumbnailQuality, Math.Clamp(value, 10, 100)); }
    public bool StartFullScreen { get => _startFullScreen; set => SetProperty(ref _startFullScreen, value); }
    public bool RememberWindowPosition { get => _rememberWindowPosition; set => SetProperty(ref _rememberWindowPosition, value); }
    public double WindowLeft { get => _windowLeft; set => SetProperty(ref _windowLeft, value); }
    public double WindowTop { get => _windowTop; set => SetProperty(ref _windowTop, value); }
    public double WindowWidth { get => _windowWidth; set => SetProperty(ref _windowWidth, Math.Max(800, value)); }
    public double WindowHeight { get => _windowHeight; set => SetProperty(ref _windowHeight, Math.Max(600, value)); }
    public int MaxRecentFiles { get => _maxRecentFiles; set => SetProperty(ref _maxRecentFiles, Math.Clamp(value, 10, 200)); }
    public bool EnableEqualizer { get => _enableEqualizer; set => SetProperty(ref _enableEqualizer, value); }
    public float Preamp { get => _preamp; set => SetProperty(ref _preamp, Math.Clamp(value, -20f, 20f)); }
    public float[] EqualizerBands { get => _equalizerBands; set => SetProperty(ref _equalizerBands, value); }
}
