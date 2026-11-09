using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Interop;
using StreamLite.Services;
using StreamLite.ViewModels;

namespace StreamLite;

public partial class MainWindow : Window
{
    readonly MainViewModel _vm;
    readonly HotkeyService _hotkeys;
    nint _hwnd;

    public MainWindow(MainViewModel viewModel, HotkeyService hotkeyService)
    {
        InitializeComponent();
        _vm = viewModel;
        _hotkeys = hotkeyService;
        DataContext = _vm;

        SourceInitialized += OnSourceInitialized;
        Loaded += async (_, _) =>
        {
            await _vm.InitializeAsync();
            OnPageChanged(this, _vm.CurrentPage);
        };
        _vm.PageChanged += OnPageChanged;
    }

    void OnSourceInitialized(object? sender, EventArgs e)
    {
        _hwnd = new WindowInteropHelper(this).Handle;
        var source = HwndSource.FromHwnd(_hwnd);
        source?.AddHook(WndProc);

        // Initialize hotkeys with window handle
        _hotkeys.Initialize(_hwnd);
        _hotkeys.RegisterDefaults();
        _hotkeys.HotkeyPressed += OnHotkeyPressed;
    }

    void OnHotkeyPressed(object? sender, string action)
    {
        Dispatcher.Invoke(() =>
        {
            switch (action)
            {
                case "play_pause": _vm.PlayerVM.TogglePlayCommand.Execute(null); break;
                case "seek_backward": _vm.PlayerVM.SeekRelativeCommand.Execute(-10); break;
                case "seek_forward": _vm.PlayerVM.SeekRelativeCommand.Execute(10); break;
                case "fullscreen": ToggleFullScreen(); break;
                case "theater_mode": _vm.PlayerVM.ToggleTheaterModeCommand.Execute(null); break;
            }
        });
    }

    nint WndProc(nint hwnd, int msg, nint wParam, nint lParam, ref bool handled)
    {
        if (msg == 0x0312) // WM_HOTKEY
            _hotkeys.ProcessMessage((uint)msg, wParam);
        return nint.Zero;
    }

    void OnPageChanged(object? sender, string page)
    {
        var key = page switch
        {
            "home" => "HomeTemplate",
            "library" => "LibraryTemplate",
            "playlists" => "PlaylistTemplate",
            "equalizer" => "EqualizerTemplate",
            "settings" => "SettingsTemplate",
            _ => "HomeTemplate"
        };
        ContentArea.ContentTemplate = TryFindResource(key) as DataTemplate;
    }

    void ToggleFullScreen()
    {
        if (WindowState == WindowState.Maximized)
        {
            WindowState = WindowState.Normal;
            WindowStyle = WindowStyle.None;
        }
        else
        {
            WindowState = WindowState.Maximized;
        }
    }

    void MinimizeClick(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    void MaximizeClick(object sender, RoutedEventArgs e)
    {
        WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    }
    void CloseClick(object sender, RoutedEventArgs e) => Close();

    protected override void OnKeyDown(KeyEventArgs e)
    {
        base.OnKeyDown(e);
        switch (e.Key)
        {
            case Key.Space: _vm.PlayerVM.TogglePlayCommand.Execute(null); e.Handled = true; break;
            case Key.Left: _vm.PlayerVM.SeekRelativeCommand.Execute(-10); e.Handled = true; break;
            case Key.Right: _vm.PlayerVM.SeekRelativeCommand.Execute(10); e.Handled = true; break;
            case Key.Up: _vm.PlayerVM.SetVolumeCommand.Execute(_vm.PlayerVM.Volume + 0.1); e.Handled = true; break;
            case Key.Down: _vm.PlayerVM.SetVolumeCommand.Execute(_vm.PlayerVM.Volume - 0.1); e.Handled = true; break;
            case Key.F: ToggleFullScreen(); e.Handled = true; break;
            case Key.M: _vm.PlayerVM.ToggleMuteCommand.Execute(null); e.Handled = true; break;
            case Key.Escape: if (WindowState == WindowState.Maximized) { WindowState = WindowState.Normal; } e.Handled = true; break;
        }
    }

    protected override void OnDrop(DragEventArgs e)
    {
        base.OnDrop(e);
        if (e.Data.GetDataPresent(DataFormats.FileDrop) && e.Data.GetData(DataFormats.FileDrop) is string[] files)
        {
            foreach (var file in files)
            {
                var ext = System.IO.Path.GetExtension(file).ToLower();
                if (ext is ".m3u" or ".m3u8")
                {
                    _ = _vm.PlaylistVM.ImportM3UCommand.ExecuteAsync(file);
                }
                else if (LibraryService.IsMediaFile(file))
                {
                    _ = _vm.OpenFilesCommand.ExecuteAsync(null);
                }
            }
        }
    }
}
