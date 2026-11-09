using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class PlaylistViewModel : ObservableObject
{
    readonly IPlaylistService _playlistService;
    readonly ILibraryService _library;

    [ObservableProperty] ObservableCollection<Playlist> _playlists = [];
    [ObservableProperty] ObservableCollection<MediaItem> _playlistItems = [];
    [ObservableProperty] Playlist? _selectedPlaylist;
    [ObservableProperty] bool _isLoading;

    public PlaylistViewModel(IPlaylistService playlistService, ILibraryService library)
    {
        _playlistService = playlistService;
        _library = library;
        _playlistService.PlaylistsChanged += async (_, _) => await RefreshAsync();
    }

    public async Task RefreshAsync()
    {
        Playlists = new ObservableCollection<Playlist>(await _playlistService.GetPlaylistsAsync());
    }

    [RelayCommand]
    async Task CreatePlaylist(string name)
    {
        if (string.IsNullOrWhiteSpace(name)) return;
        await _playlistService.CreatePlaylistAsync(name);
    }

    [RelayCommand]
    async Task DeletePlaylist(Playlist playlist)
    {
        if (playlist == null) return;
        await _playlistService.DeletePlaylistAsync(playlist.Id);
        if (SelectedPlaylist?.Id == playlist.Id) SelectedPlaylist = null;
    }

    [RelayCommand]
    async Task SelectPlaylist(Playlist playlist)
    {
        SelectedPlaylist = playlist;
        IsLoading = true;
        PlaylistItems = new ObservableCollection<MediaItem>(
            await _playlistService.GetPlaylistItemsAsync(playlist.Id));
        IsLoading = false;
    }

    [RelayCommand]
    async Task AddToPlaylist(MediaItem media)
    {
        if (SelectedPlaylist == null) return;
        await _playlistService.AddToPlaylistAsync(SelectedPlaylist.Id, media);
        await SelectPlaylist(SelectedPlaylist);
    }

    [RelayCommand]
    async Task RemoveFromPlaylist(MediaItem media)
    {
        if (SelectedPlaylist == null) return;
        await _playlistService.RemoveFromPlaylistAsync(SelectedPlaylist.Id, media.Id);
        await SelectPlaylist(SelectedPlaylist);
    }

    [RelayCommand]
    async Task ImportM3U()
    {
        var dialog = new Microsoft.Win32.OpenFileDialog
        {
            Filter = "M3U Playlist|*.m3u;*.m3u8|All Files|*.*"
        };
        if (dialog.ShowDialog() == true)
        {
            IsLoading = true;
            await _playlistService.ImportM3UAsync(dialog.FileName);
            await RefreshAsync();
            IsLoading = false;
        }
    }

    [RelayCommand]
    async Task ImportM3UFromUrl(string url)
    {
        if (string.IsNullOrWhiteSpace(url)) return;
        IsLoading = true;
        try { await _playlistService.ImportM3UFromUrlAsync(url); }
        finally { IsLoading = false; }
        await RefreshAsync();
    }

    [RelayCommand]
    async Task ExportPlaylist(Playlist playlist)
    {
        var dialog = new Microsoft.Win32.SaveFileDialog
        {
            Filter = "M3U Playlist|*.m3u",
            FileName = $"{playlist.Name}.m3u"
        };
        if (dialog.ShowDialog() == true)
            await _playlistService.ExportM3UAsync(playlist.Id, dialog.FileName);
    }
}
