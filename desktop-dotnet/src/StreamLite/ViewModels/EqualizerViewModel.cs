using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class EqualizerViewModel : ObservableObject
{
    readonly IMediaPlayerService _player;
    readonly ISettingsService _settings;

    [ObservableProperty] bool _isEnabled;
    [ObservableProperty] float _preamp;
    [ObservableProperty] float _band0;
    [ObservableProperty] float _band1;
    [ObservableProperty] float _band2;
    [ObservableProperty] float _band3;
    [ObservableProperty] float _band4;
    [ObservableProperty] float _band5;
    [ObservableProperty] float _band6;
    [ObservableProperty] float _band7;
    [ObservableProperty] float _band8;
    [ObservableProperty] float _band9;
    [ObservableProperty] string _selectedPreset = "Custom";

    static readonly Dictionary<string, float[]> Presets = new()
    {
        ["Custom"] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        ["Rock"] = [4, 3, 0, -2, -1, 1, 3, 4, 4, 3],
        ["Pop"] = [-1, 2, 3, 4, 3, 0, -1, -1, 0, 0],
        ["Jazz"] = [3, 2, 1, 2, 1, 0, 1, 2, 3, 3],
        ["Classical"] = [4, 3, 2, 1, 0, 0, 1, 2, 3, 4],
        ["Dance"] = [5, 3, 0, -1, -2, -1, 2, 4, 5, 5],
        ["Electronic"] = [4, 3, 1, -1, -2, -1, 1, 3, 4, 4],
        ["Hip Hop"] = [3, 2, -1, -2, -1, 1, 2, 2, 2, 1],
        ["Voice"] = [-2, -1, 0, 2, 3, 3, 2, 1, 0, -1],
        ["Full Bass"] = [6, 5, 3, 1, 0, 0, 0, 0, 0, 0],
        ["Full Treble"] = [0, 0, 0, 0, 0, 1, 3, 5, 6, 6],
    };

    public EqualizerViewModel(IMediaPlayerService player, ISettingsService settings)
    {
        _player = player;
        _settings = settings;
    }

    public float[] GetBands() => [Band0, Band1, Band2, Band3, Band4, Band5, Band6, Band7, Band8, Band9];

    [RelayCommand]
    void ApplyPreset(string name)
    {
        if (!Presets.TryGetValue(name, out var bands)) return;
        SelectedPreset = name;
        for (int i = 0; i < bands.Length && i < 10; i++)
            SetBand(i, bands[i]);
        ApplyToPlayer();
    }

    [RelayCommand]
    void ToggleEqualizer()
    {
        IsEnabled = !IsEnabled;
        _player.SetEqualizerEnabled(IsEnabled);
        if (IsEnabled) ApplyToPlayer();
    }

    void ApplyToPlayer()
    {
        if (!IsEnabled) return;
        _player.SetPreamp(Preamp);
        var bands = GetBands();
        for (int i = 0; i < bands.Length; i++)
            _player.SetEqualizerBand(i, bands[i]);
    }

    void SetBand(int index, float value)
    {
        value = Math.Clamp(value, -20f, 20f);
        switch (index)
        {
            case 0: Band0 = value; break; case 1: Band1 = value; break;
            case 2: Band2 = value; break; case 3: Band3 = value; break;
            case 4: Band4 = value; break; case 5: Band5 = value; break;
            case 6: Band6 = value; break; case 7: Band7 = value; break;
            case 8: Band8 = value; break; case 9: Band9 = value; break;
        }
    }

    static readonly string[] BandLabels = ["60 Hz", "170 Hz", "310 Hz", "600 Hz", "1 kHz", "3 kHz", "6 kHz", "12 kHz", "14 kHz", "16 kHz"];
    public string[] BandFrequencies => BandLabels;
}
