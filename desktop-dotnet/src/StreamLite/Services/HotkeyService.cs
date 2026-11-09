using System.Runtime.InteropServices;
using System.Windows.Input;

namespace StreamLite.Services;

public class HotkeyService : IDisposable
{
    nint _windowHandle;
    readonly Dictionary<int, HotkeyRegistration> _hotkeys = new();
    int _nextId;
    bool _initialized;

    public event EventHandler<string>? HotkeyPressed;

    public HotkeyService() { }

    public void Initialize(nint windowHandle) { _windowHandle = windowHandle; _initialized = true; }

    public int RegisterHotkey(Key key, ModifierKeys modifiers, string action)
    {
        var id = _nextId++;
        var vk = KeyInterop.VirtualKeyFromKey(key);
        var mod = (uint)modifiers;

        if (RegisterHotKey(_windowHandle, id, mod, (uint)vk))
        {
            _hotkeys[id] = new HotkeyRegistration(key, modifiers, action);
            return id;
        }
        return -1;
    }

    public bool UnregisterHotkey(int id)
    {
        if (_hotkeys.Remove(id))
            return UnregisterHotKey(_windowHandle, id);
        return false;
    }

    public void ProcessMessage(uint msg, nint wParam)
    {
        if (msg == WM_HOTKEY && _hotkeys.TryGetValue(wParam.ToInt32(), out var reg))
            HotkeyPressed?.Invoke(this, reg.Action);
    }

    public void RegisterDefaults()
    {
        RegisterHotkey(Key.Space, ModifierKeys.None, "play_pause");
        RegisterHotkey(Key.Left, ModifierKeys.None, "seek_backward");
        RegisterHotkey(Key.Right, ModifierKeys.None, "seek_forward");
        RegisterHotkey(Key.Up, ModifierKeys.None, "volume_up");
        RegisterHotkey(Key.Down, ModifierKeys.None, "volume_down");
        RegisterHotkey(Key.F, ModifierKeys.None, "fullscreen");
        RegisterHotkey(Key.M, ModifierKeys.None, "mute");
        RegisterHotkey(Key.Escape, ModifierKeys.None, "exit_fullscreen");
        RegisterHotkey(Key.OemPeriod, ModifierKeys.None, "frame_step");
        RegisterHotkey(Key.F11, ModifierKeys.None, "theater_mode");
        RegisterHotkey(Key.R, ModifierKeys.Control, "toggle_recording");
        RegisterHotkey(Key.S, ModifierKeys.Control, "screenshot");
    }

    public void Dispose()
    {
        foreach (var id in _hotkeys.Keys.ToList())
            UnregisterHotKey(_windowHandle, id);
        _hotkeys.Clear();
    }

    record HotkeyRegistration(Key Key, ModifierKeys Modifiers, string Action);

    const int WM_HOTKEY = 0x0312;

    [DllImport("user32.dll")] static extern bool RegisterHotKey(nint hWnd, int id, uint fsModifiers, uint vk);
    [DllImport("user32.dll")] static extern bool UnregisterHotKey(nint hWnd, int id);
}

[Flags]
public enum ModifierKeys : uint
{
    None = 0,
    Alt = 1,
    Control = 2,
    Shift = 4,
    Windows = 8,
}
