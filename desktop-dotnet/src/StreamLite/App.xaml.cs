using System.Windows;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using StreamLite.Data;
using StreamLite.Services;
using StreamLite.Services.Interfaces;
using StreamLite.ViewModels;

namespace StreamLite;

public partial class App : Application
{
    static IHost? _host;

    public App()
    {
        _host = CreateHostBuilder().Build();
    }

    static IHostBuilder CreateHostBuilder() => Host.CreateDefaultBuilder()
        .ConfigureServices((_, services) =>
        {
            // Database
            services.AddSingleton<DatabaseService>();

            // Services
            services.AddSingleton<IMediaPlayerService, MediaPlayerService>();
            services.AddSingleton<ILibraryService, LibraryService>();
            services.AddSingleton<IPlaylistService, PlaylistService>();
            services.AddSingleton<ISettingsService, SettingsService>();
            services.AddSingleton<IThumbnailService, ThumbnailService>();
            services.AddSingleton<SubtitleService>();

            // Hotkeys - created per window
            services.AddTransient<HotkeyService>();

            // ViewModels
            services.AddTransient<MainViewModel>();
            services.AddTransient<PlayerViewModel>();
            services.AddTransient<LibraryViewModel>();
            services.AddTransient<PlaylistViewModel>();
            services.AddTransient<SettingsViewModel>();
            services.AddTransient<EqualizerViewModel>();

            // Window
            services.AddTransient<MainWindow>();
        });

    protected override async void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        await _host!.StartAsync();

        var mainWindow = _host.Services.GetRequiredService<MainWindow>();
        mainWindow.Show();
    }

    protected override async void OnExit(ExitEventArgs e)
    {
        await _host!.StopAsync();
        base.OnExit(e);
    }
}
