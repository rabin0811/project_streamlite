using StreamLite.Models;

namespace StreamLite.Services.Interfaces;

public interface ILibraryService
{
    event EventHandler? LibraryUpdated;

    Task<List<MediaItem>> GetMediaAsync(MediaType? type = null);
    Task<List<MediaItem>> GetRecentAsync(int count = 50);
    Task<List<MediaItem>> GetFavoritesAsync();
    Task<List<MediaItem>> SearchAsync(string query, MediaType? type = null);
    Task<List<MediaFolder>> GetFoldersAsync();
    Task ScanFolderAsync(string path, bool includeSubfolders = true);
    Task RemoveFolderAsync(int folderId);
    Task<MediaItem?> GetMetadataAsync(string filePath);
    Task UpdatePlaybackAsync(MediaItem media);
    Task ToggleFavoriteAsync(MediaItem media);
    Task<bool> IsFavoriteAsync(int mediaId);
    Task<int> GetTotalCountAsync();
    Task<long> GetTotalSizeAsync();
    Task<List<MediaItem>> GetResumeCandidatesAsync();
}
