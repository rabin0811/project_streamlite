using StreamLite.Data;
using System.IO;
using StreamLite.Models;
using StreamLite.Services.Interfaces;
using TagLib;
using File = System.IO.File;

namespace StreamLite.Services;

public class LibraryService : ILibraryService
{
    readonly DatabaseService _db;
    static readonly HashSet<string> VideoExts = [".mp4", ".mkv", ".avi", ".mov", ".webm", ".flv", ".wmv", ".ts", ".mpeg", ".mpg", ".m4v", ".3gp", ".ogv", ".dv", ".vob"];
    static readonly HashSet<string> AudioExts = [".mp3", ".aac", ".flac", ".ogg", ".wav", ".m4a", ".wma", ".opus", ".ape", ".aiff", ".dsf", ".dff"];
    static readonly HashSet<string> ImageExts = [".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp", ".tiff", ".tif", ".heic", ".heif", ".svg", ".ico"];

    public event EventHandler? LibraryUpdated;

    public LibraryService(DatabaseService db) => _db = db;

    public async Task<List<MediaItem>> GetMediaAsync(MediaType? type = null)
        => await _db.GetMediaItemsAsync(type);

    public async Task<List<MediaItem>> GetRecentAsync(int count = 50)
        => (await _db.GetRecentAsync(count)).Take(count).ToList();

    public async Task<List<MediaItem>> GetFavoritesAsync()
        => await _db.GetFavoritesAsync();

    public async Task<List<MediaItem>> SearchAsync(string query, MediaType? type = null)
    {
        var all = await _db.GetMediaItemsAsync(type);
        if (string.IsNullOrWhiteSpace(query)) return all;
        var q = query.ToLower();
        return all.Where(m =>
            m.FileName.ToLower().Contains(q) ||
            m.Title?.ToLower().Contains(q) == true ||
            m.Artist?.ToLower().Contains(q) == true ||
            m.Album?.ToLower().Contains(q) == true ||
            m.Genre?.ToLower().Contains(q) == true).ToList();
    }

    public async Task<List<MediaFolder>> GetFoldersAsync()
        => await _db.GetFoldersAsync();

    public async Task ScanFolderAsync(string path, bool includeSubfolders = true)
    {
        var folder = new MediaFolder
        {
            Path = path,
            Name = System.IO.Path.GetFileName(path) ?? path,
            IncludeSubfolders = includeSubfolders,
            LastScanned = DateTime.UtcNow,
        };
        var folderId = await _db.SaveFolderAsync(folder);

        var searchOption = includeSubfolders ? SearchOption.AllDirectories : SearchOption.TopDirectoryOnly;
        var files = Directory.EnumerateFiles(path, "*.*", searchOption)
            .Where(f => VideoExts.Contains(System.IO.Path.GetExtension(f).ToLower()) ||
                        AudioExts.Contains(System.IO.Path.GetExtension(f).ToLower()) ||
                        ImageExts.Contains(System.IO.Path.GetExtension(f).ToLower()));

        foreach (var file in files) { await ProcessFileAsync(file, folderId); }

        LibraryUpdated?.Invoke(this, EventArgs.Empty);
    }

    async Task ProcessFileAsync(string filePath, int folderId)
    {
        try
        {
            var ext = System.IO.Path.GetExtension(filePath).ToLower();
            var fi = new FileInfo(filePath);
            var type = GetMediaType(ext);

            var item = new MediaItem
            {
                FilePath = filePath,
                FileName = fi.Name,
                Extension = ext,
                Type = type,
                FileSize = fi.Length,
                DateModified = fi.LastWriteTimeUtc,
                FolderId = folderId,
                FolderPath = System.IO.Path.GetDirectoryName(filePath),
            };

            if (type is MediaType.Video or MediaType.Audio)
            {
                try
                {
                    using var tfile = TagLib.File.Create(filePath);
                    item.Title = tfile.Tag.Title;
                    item.Artist = tfile.Tag.FirstPerformer;
                    item.Album = tfile.Tag.Album;
                    item.Genre = tfile.Tag.FirstGenre;
                    item.TrackNumber = (int?)tfile.Tag.Track;
                    item.Year = (int?)tfile.Tag.Year;
                    item.Duration = tfile.Properties?.Duration;
                    item.Bitrate = (int?)tfile.Properties?.AudioBitrate;
                    item.Width = tfile.Properties?.VideoWidth;
                    item.Height = tfile.Properties?.VideoHeight;
                    item.Codec = tfile.Properties?.Description;
                    item.AudioCodec = tfile.Properties != null ? tfile.Properties.AudioBitrate.ToString() : null;
                    item.FrameRate = null;
                }
                catch { /* metadata read best-effort */ }
            }

            await _db.SaveMediaItemAsync(item);
        }
        catch { /* per-file best-effort */ }
    }

    public async Task<MediaItem?> GetMetadataAsync(string filePath)
    {
        if (!File.Exists(filePath)) return null;
        var existing = await _db.GetMediaItemByPathAsync(filePath);
        if (existing != null) return existing;

        var ext = System.IO.Path.GetExtension(filePath).ToLower();
        var fi = new FileInfo(filePath);
        var item = new MediaItem
        {
            FilePath = filePath,
            FileName = fi.Name,
            Extension = ext,
            Type = GetMediaType(ext),
            FileSize = fi.Length,
            DateModified = fi.LastWriteTimeUtc,
        };

        if (item.Type is MediaType.Video or MediaType.Audio)
        {
            try
            {
                using var tfile = TagLib.File.Create(filePath);
                item.Title = tfile.Tag.Title;
                item.Duration = tfile.Properties?.Duration;
                item.Width = tfile.Properties?.VideoWidth;
                item.Height = tfile.Properties?.VideoHeight;
                item.Bitrate = (int?)tfile.Properties?.AudioBitrate;
            }
            catch { /* ignore */ }
        }

        return item;
    }

    public async Task UpdatePlaybackAsync(MediaItem media)
    {
        if (media.Id <= 0) return;
        media.LastPlayed = DateTime.UtcNow;
        media.PlayCount++;
        await _db.UpdatePlaybackAsync(media.Id, media.ResumePosition, media.PlayCount, media.LastPlayed);
        LibraryUpdated?.Invoke(this, EventArgs.Empty);
    }

    public async Task ToggleFavoriteAsync(MediaItem media)
    {
        if (media.Id <= 0) return;
        media.IsFavorite = !media.IsFavorite;
        await _db.ToggleFavoriteAsync(media.Id, media.IsFavorite);
        LibraryUpdated?.Invoke(this, EventArgs.Empty);
    }

    public async Task<bool> IsFavoriteAsync(int mediaId)
    {
        var items = await _db.GetFavoritesAsync();
        return items.Any(i => i.Id == mediaId);
    }

    public async Task<int> GetTotalCountAsync() => await _db.GetTotalMediaCountAsync();
    public async Task<long> GetTotalSizeAsync() => await _db.GetTotalMediaSizeAsync();

    public async Task<List<MediaItem>> GetResumeCandidatesAsync()
    {
        var all = await _db.GetRecentAsync(100);
        return all.Where(m => m.ResumePosition.HasValue && m.ResumePosition.Value > 5).ToList();
    }

    public async Task RemoveFolderAsync(int folderId) => await _db.RemoveFolderAsync(folderId);

    static MediaType GetMediaType(string ext)
    {
        if (VideoExts.Contains(ext)) return MediaType.Video;
        if (AudioExts.Contains(ext)) return MediaType.Audio;
        if (ImageExts.Contains(ext)) return MediaType.Image;
        return MediaType.Unknown;
    }

    public static bool IsMediaFile(string path)
    {
        var ext = System.IO.Path.GetExtension(path).ToLower();
        return VideoExts.Contains(ext) || AudioExts.Contains(ext) || ImageExts.Contains(ext);
    }
}

