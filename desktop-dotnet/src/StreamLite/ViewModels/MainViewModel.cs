using System.Collections.ObjectModel;
using System.IO;
using System.Windows.Input;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Models;
using StreamLite.Services;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class MainViewModel : ObservableObject
{
    readonly IMediaPlayerService _player;
    readonly ILibraryService _library;
    readonly IPlaylistService _playlistService;
    readonly ISettingsService _settings;
    readonly HotkeyService _hotkeys;
    readonly IThumbnailService _thumbnails;

    [ObservableProperty] bool _isPlayerVisible;
    [ObservableProperty] string _searchQuery = string.Empty;
    [ObservableProperty] bool _isLibraryView = true;
    [ObservableProperty] string? _statusMessage;
    [ObservableProperty] bool _isLoading;
    [ObservableProperty] string _currentPage = "home";

    public event EventHandler<string>? PageChanged;

    public PlayerViewModel PlayerVM { get; }
    public LibraryViewModel LibraryVM { get; }
    public PlaylistViewModel PlaylistVM { get; }
    public SettingsViewModel SettingsVM { get; }

    public MainViewModel(
        IMediaPlayerService player,
        ILibraryService library,
        IPlaylistService playlistService,
        ISettingsService settings,
        HotkeyService hotkeys,
        IThumbnailService thumbnails,
        PlayerViewModel playerVm,
        LibraryViewModel libraryVm,
        PlaylistViewModel playlistVm,
        SettingsViewModel settingsVm)
    {
        _player = player;
        _library = library;
        _playlistService = playlistService;
        _settings = settings;
        _hotkeys = hotkeys;
        _thumbnails = thumbnails;
        PlayerVM = playerVm;
        LibraryVM = libraryVm;
        PlaylistVM = playlistVm;
        SettingsVM = settingsVm;

        _player.MediaLoaded += (_, _) => IsPlayerVisible = true;
        _player.MediaEnded += (_, _) => PlayNext();
        _player.ErrorOccurred += (_, msg) => StatusMessage = msg;
        _player.StatusChanged += (_, status) =>
        {
            StatusMessage = status switch
            {
                PlaybackStatus.Playing => $"Playing: {_player.CurrentMedia?.DisplayName}",
                PlaybackStatus.Paused => "Paused",
                PlaybackStatus.Buffering => "Buffering...",
                PlaybackStatus.Error => "Playback error",
                _ => ""
            };
        };

        _hotkeys.HotkeyPressed += (_, action) =>
        {
            switch (action)
            {
                case "play_pause": PlayerVM.TogglePlayCommand.Execute(null); break;
                case "seek_backward": PlayerVM.SeekRelativeCommand.Execute(-10); break;
                case "seek_forward": PlayerVM.SeekRelativeCommand.Execute(10); break;
                case "volume_up": PlayerVM.Volume += 0.1; break;
                case "volume_down": PlayerVM.Volume -= 0.1; break;
                case "fullscreen": PlayerVM.ToggleFullScreenCommand.Execute(null); break;
                case "mute": PlayerVM.ToggleMuteCommand.Execute(null); break;
                case "exit_fullscreen": PlayerVM.IsFullScreen = false; break;
                case "frame_step": _player.StepFrame(); break;
                case "screenshot": TakeScreenshot(); break;
            }
        };
    }

    public async Task InitializeAsync()
    {
        await _settings.LoadAsync();
        await LibraryVM.LoadAsync();
        PlaylistVM.Playlists = new ObservableCollection<Playlist>(
            await _playlistService.GetPlaylistsAsync());
    }

    [RelayCommand]
    async Task OpenFiles()
    {
        var dialog = new Microsoft.Win32.OpenFileDialog
        {
            Multiselect = true,
            Filter = "Media Files|*.mp4;*.mkv;*.avi;*.mov;*.webm;*.flv;*.wmv;*.ts;*.mp3;*.aac;*.flac;*.ogg;*.wav;*.m4a|All Files|*.*"
        };
        if (dialog.ShowDialog() == true)
        {
            foreach (var file in dialog.FileNames)
            {
                var media = await _library.GetMetadataAsync(file);
                if (media != null) PlayerVM.PlayMedia(media);
            }
        }
    }

    [RelayCommand]
    async Task OpenFolder()
    {
        var dialog = new Ookii.Dialogs.Wpf.VistaFolderBrowserDialog();
        if (dialog.ShowDialog() == true)
        {
            IsLoading = true;
            await _library.ScanFolderAsync(dialog.SelectedPath);
            await LibraryVM.LoadAsync();
            IsLoading = false;
        }
    }

    [RelayCommand]
    async Task OpenUrl()
    {
        var dialog = new Views.Dialogs.OpenUrlDialog();
        if (dialog.ShowDialog() == true)
        {
            var url = dialog.StreamUrl;
            if (string.IsNullOrWhiteSpace(url)) return;

            if (url.Contains("youtube.com") || url.Contains("youtu.be"))
            {
                try { System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo(url) { UseShellExecute = true }); }
                catch { StatusMessage = "Unable to open browser"; }
                return;
            }

            var media = new MediaItem
            {
                FilePath = url,
                FileName = url.Split('/').LastOrDefault() ?? "Network Stream",
                Type = MediaType.Stream,
            };
            PlayerVM.PlayMedia(media);
        }
    }

    [RelayCommand]
    void Navigate(string page)
    {
        CurrentPage = page;
        PageChanged?.Invoke(this, page);
    }

    [RelayCommand]
    void PlayMedia(MediaItem media) => PlayerVM.PlayMedia(media);

    [RelayCommand]
    async Task Search()
    {
        if (string.IsNullOrWhiteSpace(SearchQuery)) return;
        var results = await _library.SearchAsync(SearchQuery);
        LibraryVM.MediaItems = new ObservableCollection<MediaItem>(results);
    }

    void PlayNext()
    {
        if (PlayerVM.Playlist.Count > 0)
        {
            var nextIdx = PlayerVM.CurrentIndex + 1;
            if (nextIdx < PlayerVM.Playlist.Count)
                PlayerVM.PlayMedia(PlayerVM.Playlist[nextIdx]);
        }
    }

    async void TakeScreenshot()
    {
        var folder = _settings.Settings.ScreenshotFolder ??
            System.IO.Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "StreamLite");
        Directory.CreateDirectory(folder);
        var name = $"StreamLite_{DateTime.Now:yyyyMMdd_HHmmss}.{_settings.Settings.ScreenshotFormat.ToLower()}";
        var path = System.IO.Path.Combine(folder, name);
        _player.TakeScreenshot(path);
        StatusMessage = $"Screenshot saved: {name}";
    }
}

