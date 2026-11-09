using StreamLite.Models;

namespace StreamLite.Services.Interfaces;

public interface IPlaylistService
{
    event EventHandler? PlaylistsChanged;

    Task<List<Playlist>> GetPlaylistsAsync();
    Task<Playlist> CreatePlaylistAsync(string name, string? description = null);
    Task DeletePlaylistAsync(int playlistId);
    Task<List<MediaItem>> GetPlaylistItemsAsync(int playlistId);
    Task AddToPlaylistAsync(int playlistId, MediaItem media);
    Task RemoveFromPlaylistAsync(int playlistId, int mediaItemId);
    Task<List<Playlist>> ImportM3UAsync(string filePath);
    Task<List<Playlist>> ImportM3UFromUrlAsync(string url);
    Task ExportM3UAsync(int playlistId, string filePath);
    Task ReorderPlaylistAsync(int playlistId, List<int> mediaItemIds);
}
