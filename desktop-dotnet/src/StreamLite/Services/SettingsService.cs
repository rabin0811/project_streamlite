using System.Text.Json;
using System.IO;
using StreamLite.Data;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.Services;

public class SettingsService : ISettingsService
{
    readonly DatabaseService _db;
    public AppSettings Settings { get; } = new();
    public event EventHandler<AppSettings>? SettingsChanged;

    public SettingsService(DatabaseService db) => _db = db;

    public async Task LoadAsync()
    {
        var allKeys = await Task.WhenAll(
            GetSettingAsync("DarkTheme"),
            GetSettingAsync("DefaultVolume"),
            GetSettingAsync("DefaultSpeed"),
            GetSettingAsync("HardwareDecoding"),
            GetSettingAsync("AutoResume"),
            GetSettingAsync("RememberPosition"),
            GetSettingAsync("AutoLoadSubtitles"),
            GetSettingAsync("SubtitleFontSize"),
            GetSettingAsync("MinimizeToTray"),
            GetSettingAsync("ShowNotifications"),
            GetSettingAsync("WindowWidth"),
            GetSettingAsync("WindowHeight"),
            GetSettingAsync("EnableEqualizer"),
            GetSettingAsync("EqualizerBands"),
            GetSettingAsync("Preamp")
        );

        if (bool.TryParse(allKeys[0], out var dt)) Settings.DarkTheme = dt;
        if (double.TryParse(allKeys[1], out var dv)) Settings.DefaultVolume = dv;
        if (double.TryParse(allKeys[2], out var ds)) Settings.DefaultSpeed = ds;
        if (bool.TryParse(allKeys[3], out var hw)) Settings.HardwareDecoding = hw;
        if (bool.TryParse(allKeys[4], out var ar)) Settings.AutoResume = ar;
        if (bool.TryParse(allKeys[5], out var rp)) Settings.RememberPosition = rp;
        if (bool.TryParse(allKeys[6], out var als)) Settings.AutoLoadSubtitles = als;
        if (int.TryParse(allKeys[7], out var sfs)) Settings.SubtitleFontSize = sfs;
        if (bool.TryParse(allKeys[8], out var mt)) Settings.MinimizeToTray = mt;
        if (bool.TryParse(allKeys[9], out var sn)) Settings.ShowNotifications = sn;
        if (double.TryParse(allKeys[10], out var ww)) Settings.WindowWidth = ww;
        if (double.TryParse(allKeys[11], out var wh)) Settings.WindowHeight = wh;
        if (bool.TryParse(allKeys[12], out var ee)) Settings.EnableEqualizer = ee;
        if (allKeys[13] != null)
        {
            try { Settings.EqualizerBands = JsonSerializer.Deserialize<float[]>(allKeys[13]) ?? new float[10]; } catch { }
        }
        if (float.TryParse(allKeys[14], out var pa)) Settings.Preamp = pa;

        Settings.PropertyChanged += (_, _) => _ = SaveAsync();
    }

    async Task<string?> GetSettingAsync(string key) => await _db.GetSettingAsync(key);

    public async Task SaveAsync()
    {
        await Task.WhenAll(
            _db.SetSettingAsync("DarkTheme", Settings.DarkTheme.ToString()),
            _db.SetSettingAsync("DefaultVolume", Settings.DefaultVolume.ToString("F2")),
            _db.SetSettingAsync("DefaultSpeed", Settings.DefaultSpeed.ToString("F2")),
            _db.SetSettingAsync("HardwareDecoding", Settings.HardwareDecoding.ToString()),
            _db.SetSettingAsync("AutoResume", Settings.AutoResume.ToString()),
            _db.SetSettingAsync("RememberPosition", Settings.RememberPosition.ToString()),
            _db.SetSettingAsync("AutoLoadSubtitles", Settings.AutoLoadSubtitles.ToString()),
            _db.SetSettingAsync("SubtitleFontSize", Settings.SubtitleFontSize.ToString()),
            _db.SetSettingAsync("MinimizeToTray", Settings.MinimizeToTray.ToString()),
            _db.SetSettingAsync("ShowNotifications", Settings.ShowNotifications.ToString()),
            _db.SetSettingAsync("WindowWidth", Settings.WindowWidth.ToString("F0")),
            _db.SetSettingAsync("WindowHeight", Settings.WindowHeight.ToString("F0")),
            _db.SetSettingAsync("EnableEqualizer", Settings.EnableEqualizer.ToString()),
            _db.SetSettingAsync("EqualizerBands", JsonSerializer.Serialize(Settings.EqualizerBands)),
            _db.SetSettingAsync("Preamp", Settings.Preamp.ToString("F1"))
        );
        SettingsChanged?.Invoke(this, Settings);
    }

    public async Task ResetAsync()
    {
        Settings.DarkTheme = true;
        Settings.DefaultVolume = 0.8;
        Settings.DefaultSpeed = 1.0;
        Settings.HardwareDecoding = true;
        Settings.AutoResume = true;
        Settings.RememberPosition = true;
        Settings.AutoLoadSubtitles = true;
        Settings.SubtitleFontSize = 24;
        Settings.MinimizeToTray = false;
        Settings.ShowNotifications = true;
        Settings.WindowWidth = 1280;
        Settings.WindowHeight = 720;
        Settings.EnableEqualizer = false;
        Settings.EqualizerBands = new float[10];
        Settings.Preamp = 0;
        await SaveAsync();
    }

    public T GetValue<T>(string key, T defaultValue)
    {
        var prop = typeof(AppSettings).GetProperty(key);
        if (prop?.GetValue(Settings) is T val) return val;
        return defaultValue;
    }

    public async Task SetValueAsync<T>(string key, T value)
    {
        var prop = typeof(AppSettings).GetProperty(key);
        prop?.SetValue(Settings, value);
        await SaveAsync();
    }
}

