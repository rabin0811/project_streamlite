using StreamLite.Models;

namespace StreamLite.Services.Interfaces;

public interface ISettingsService
{
    AppSettings Settings { get; }
    event EventHandler<AppSettings>? SettingsChanged;

    Task LoadAsync();
    Task SaveAsync();
    Task ResetAsync();
    T GetValue<T>(string key, T defaultValue);
    Task SetValueAsync<T>(string key, T value);
}
