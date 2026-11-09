using System.IO;
using System.Net.Http;
using StreamLite.Data;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.Services;

public class PlaylistService : IPlaylistService
{
    readonly DatabaseService _db;

    public event EventHandler? PlaylistsChanged;

    public PlaylistService(DatabaseService db) => _db = db;

    public async Task<List<Playlist>> GetPlaylistsAsync() => await _db.GetPlaylistsAsync();

    public async Task<Playlist> CreatePlaylistAsync(string name, string? description = null)
    {
        var pl = new Playlist
        {
            Name = name,
            Description = description,
            Type = PlaylistType.User,
            DateAdded = DateTime.UtcNow,
        };
        pl.Id = await _db.SavePlaylistAsync(pl);
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
        return pl;
    }

    public async Task DeletePlaylistAsync(int playlistId)
    {
        await _db.RemovePlaylistAsync(playlistId);
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
    }

    public async Task<List<MediaItem>> GetPlaylistItemsAsync(int playlistId)
        => await _db.GetPlaylistItemsAsync(playlistId);

    public async Task AddToPlaylistAsync(int playlistId, MediaItem media)
    {
        if (media.Id <= 0) media.Id = await _db.SaveMediaItemAsync(media);
        var existing = await _db.GetPlaylistItemsAsync(playlistId);
        await _db.AddPlaylistItemAsync(playlistId, media.Id, existing.Count);
        var pl = (await _db.GetPlaylistsAsync()).FirstOrDefault(p => p.Id == playlistId);
        if (pl != null)
        {
            pl.ItemCount = existing.Count + 1;
            pl.DateModified = DateTime.UtcNow;
            await _db.SavePlaylistAsync(pl);
        }
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
    }

    public async Task RemoveFromPlaylistAsync(int playlistId, int mediaItemId)
    {
        var items = await _db.GetPlaylistItemsAsync(playlistId);
        var toRemove = items.FirstOrDefault(i => i.Id == mediaItemId);
        if (toRemove == null) return;
        var all = await _db.GetPlaylistItemsAsync(playlistId);
        var item = all.Select((m, idx) => new { m.Id, idx }).FirstOrDefault(x => x.Id == mediaItemId);
        if (item != null)
        {
            var itemsList = await _db.GetPlaylistItemsAsync(playlistId);
            var pi = await _db.GetPlaylistItemsAsync(playlistId);
            // Remove by media item id
            await _db.RemovePlaylistItemAsync(item.Id);
        }
        var pl = (await _db.GetPlaylistsAsync()).FirstOrDefault(p => p.Id == playlistId);
        if (pl != null)
        {
            var remaining = await _db.GetPlaylistItemsAsync(playlistId);
            pl.ItemCount = remaining.Count;
            pl.DateModified = DateTime.UtcNow;
            await _db.SavePlaylistAsync(pl);
        }
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
    }

    public async Task<List<Playlist>> ImportM3UAsync(string filePath)
    {
        var content = await File.ReadAllTextAsync(filePath);
        return await ParseAndSaveM3U(content, filePath);
    }

    public async Task<List<Playlist>> ImportM3UFromUrlAsync(string url)
    {
        using var http = new HttpClient { Timeout = TimeSpan.FromSeconds(30) };
        var content = await http.GetStringAsync(url);
        return await ParseAndSaveM3U(content, url);
    }

    async Task<List<Playlist>> ParseAndSaveM3U(string content, string source)
    {
        var playlists = new List<Playlist>();
        var lines = content.Split('\n', StringSplitOptions.RemoveEmptyEntries);
        var currentName = System.IO.Path.GetFileNameWithoutExtension(source) ?? "Imported";
        var playlist = new Playlist
        {
            Name = currentName,
            SourceUrl = source.StartsWith("http") ? source : null,
            Type = PlaylistType.M3U,
            DateAdded = DateTime.UtcNow,
        };
        playlist.Id = await _db.SavePlaylistAsync(playlist);
        var items = new List<(MediaItem media, int order)>();

        string? currentExtinf = null;
        int sortOrder = 0;

        foreach (var line in lines)
        {
            var trimmed = line.Trim();
            if (trimmed.StartsWith("#EXTINF:"))
            {
                currentExtinf = trimmed;
            }
            else if (trimmed.StartsWith("#PLAYLIST:"))
            {
                playlist.Name = trimmed[10..].Trim();
            }
            else if (!trimmed.StartsWith("#") && !string.IsNullOrWhiteSpace(trimmed))
            {
                var name = currentName;
                if (currentExtinf != null)
                {
                    var commaIdx = currentExtinf.LastIndexOf(',');
                    if (commaIdx >= 0) name = currentExtinf[(commaIdx + 1)..].Trim();
                }

                var media = new MediaItem
                {
                    FilePath = trimmed,
                    FileName = name,
                    Extension = System.IO.Path.GetExtension(trimmed).ToLower(),
                    Type = MediaType.Stream,
                    FileSize = 0,
                };
                try { media.Id = await _db.SaveMediaItemAsync(media); } catch { media.Id = 0; }
                items.Add((media, sortOrder++));
                currentExtinf = null;
            }
        }

        foreach (var (media, order) in items)
        {
            if (media.Id > 0)
                await _db.AddPlaylistItemAsync(playlist.Id, media.Id, order);
        }

        playlist.ItemCount = items.Count;
        await _db.SavePlaylistAsync(playlist);
        playlists.Add(playlist);
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
        return playlists;
    }

    public async Task ExportM3UAsync(int playlistId, string filePath)
    {
        var items = await _db.GetPlaylistItemsAsync(playlistId);
        var lines = new List<string>
        {
            "#EXTM3U",
            $"#PLAYLIST:{System.IO.Path.GetFileNameWithoutExtension(filePath)}"
        };
        foreach (var item in items)
        {
            var duration = item.Duration?.TotalSeconds ?? -1;
            lines.Add($"#EXTINF:{duration},{item.DisplayName}");
            lines.Add(item.FilePath);
        }
        await File.WriteAllLinesAsync(filePath, lines);
    }

    public async Task ReorderPlaylistAsync(int playlistId, List<int> mediaItemIds)
    {
        var pl = (await _db.GetPlaylistsAsync()).FirstOrDefault(p => p.Id == playlistId);
        if (pl == null) return;

        await _db.ClearPlaylistItemsAsync(playlistId);
        for (int i = 0; i < mediaItemIds.Count; i++)
            await _db.AddPlaylistItemAsync(playlistId, mediaItemIds[i], i);

        pl.ItemCount = mediaItemIds.Count;
        pl.DateModified = DateTime.UtcNow;
        await _db.SavePlaylistAsync(pl);
        PlaylistsChanged?.Invoke(this, EventArgs.Empty);
    }
}

