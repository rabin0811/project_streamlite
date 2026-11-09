using Dapper;
using Microsoft.Data.Sqlite;
using StreamLite.Models;

namespace StreamLite.Data;

public class DatabaseService
{
    readonly string _connectionString;

    public DatabaseService()
    {
        var dbPath = System.IO.Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "StreamLite", "streamlite.db");
        System.IO.Directory.CreateDirectory(System.IO.Path.GetDirectoryName(dbPath)!);
        _connectionString = $"Data Source={dbPath};Cache=Shared";
        Initialize();
    }

    void Initialize()
    {
        using var conn = GetConnection();
        conn.Open();

        conn.Execute("""
            CREATE TABLE IF NOT EXISTS MediaFolders (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Path TEXT NOT NULL UNIQUE,
                Name TEXT NOT NULL,
                IncludeSubfolders INTEGER DEFAULT 1,
                DateAdded TEXT NOT NULL,
                LastScanned TEXT,
                IsWatched INTEGER DEFAULT 0
            )
        """);

        conn.Execute("""
            CREATE TABLE IF NOT EXISTS MediaItems (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                FilePath TEXT NOT NULL UNIQUE,
                FileName TEXT NOT NULL,
                Extension TEXT NOT NULL,
                Type INTEGER NOT NULL,
                FileSize INTEGER NOT NULL DEFAULT 0,
                Duration TEXT,
                Width INTEGER,
                Height INTEGER,
                Codec TEXT,
                AudioCodec TEXT,
                Bitrate INTEGER,
                FrameRate REAL,
                Title TEXT,
                Artist TEXT,
                Album TEXT,
                Genre TEXT,
                TrackNumber INTEGER,
                Year INTEGER,
                ThumbnailPath TEXT,
                DateAdded TEXT NOT NULL,
                DateModified TEXT,
                LastPlayed TEXT,
                PlayCount INTEGER DEFAULT 0,
                ResumePosition REAL,
                IsFavorite INTEGER DEFAULT 0,
                FolderId INTEGER,
                FOREIGN KEY (FolderId) REFERENCES MediaFolders(Id) ON DELETE SET NULL
            )
        """);

        conn.Execute("""
            CREATE TABLE IF NOT EXISTS Playlists (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT NOT NULL,
                Description TEXT,
                SourceUrl TEXT,
                Type INTEGER NOT NULL DEFAULT 0,
                ItemCount INTEGER DEFAULT 0,
                DateAdded TEXT NOT NULL,
                DateModified TEXT,
                ArtworkPath TEXT,
                IsDynamic INTEGER DEFAULT 0
            )
        """);

        conn.Execute("""
            CREATE TABLE IF NOT EXISTS PlaylistItems (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                PlaylistId INTEGER NOT NULL,
                MediaItemId INTEGER NOT NULL,
                SortOrder INTEGER NOT NULL DEFAULT 0,
                DateAdded TEXT NOT NULL,
                FOREIGN KEY (PlaylistId) REFERENCES Playlists(Id) ON DELETE CASCADE,
                FOREIGN KEY (MediaItemId) REFERENCES MediaItems(Id) ON DELETE CASCADE
            )
        """);

        conn.Execute("""
            CREATE TABLE IF NOT EXISTS AppSettings (
                Key TEXT PRIMARY KEY,
                Value TEXT NOT NULL
            )
        """);

        conn.Execute("""
            CREATE INDEX IF NOT EXISTS IX_MediaItems_Type ON MediaItems(Type);
            CREATE INDEX IF NOT EXISTS IX_MediaItems_FolderId ON MediaItems(FolderId);
            CREATE INDEX IF NOT EXISTS IX_MediaItems_LastPlayed ON MediaItems(LastPlayed);
            CREATE INDEX IF NOT EXISTS IX_MediaItems_IsFavorite ON MediaItems(IsFavorite);
            CREATE INDEX IF NOT EXISTS IX_PlaylistItems_PlaylistId ON PlaylistItems(PlaylistId);
        """);
    }

    public SqliteConnection GetConnection() => new(_connectionString);

    public async Task<List<MediaItem>> GetMediaItemsAsync(MediaType? type = null, int? folderId = null)
    {
        using var conn = GetConnection();
        var sql = "SELECT * FROM MediaItems WHERE 1=1";
        if (type.HasValue) sql += " AND Type = @Type";
        if (folderId.HasValue) sql += " AND FolderId = @FolderId";
        sql += " ORDER BY DateAdded DESC";
        return (await conn.QueryAsync<MediaItem>(sql, new { Type = (int?)type, FolderId = folderId })).AsList();
    }

    public async Task<MediaItem?> GetMediaItemByPathAsync(string path)
    {
        using var conn = GetConnection();
        return await conn.QueryFirstOrDefaultAsync<MediaItem>(
            "SELECT * FROM MediaItems WHERE FilePath = @Path", new { Path = path });
    }

    public async Task<int> SaveMediaItemAsync(MediaItem item)
    {
        using var conn = GetConnection();
        var existing = await conn.QueryFirstOrDefaultAsync<int?>(
            "SELECT Id FROM MediaItems WHERE FilePath = @FilePath", new { item.FilePath });
        if (existing.HasValue)
        {
            item.Id = existing.Value;
            await conn.ExecuteAsync("""
                UPDATE MediaItems SET FileName=@FileName, FileSize=@FileSize, Duration=@Duration,
                    Width=@Width, Height=@Height, Codec=@Codec, AudioCodec=@AudioCodec,
                    Bitrate=@Bitrate, FrameRate=@FrameRate, Title=@Title, Artist=@Artist,
                    Album=@Album, Genre=@Genre, TrackNumber=@TrackNumber, Year=@Year,
                    ThumbnailPath=@ThumbnailPath, DateModified=@DateModified, LastPlayed=@LastPlayed,
                    PlayCount=@PlayCount, ResumePosition=@ResumePosition, IsFavorite=@IsFavorite,
                    FolderId=@FolderId
                WHERE Id=@Id
            """, item);
            return item.Id;
        }
        else
        {
            item.DateAdded = DateTime.UtcNow;
            return await conn.QuerySingleAsync<int>("""
                INSERT INTO MediaItems (FilePath, FileName, Extension, Type, FileSize, Duration, Width, Height,
                    Codec, AudioCodec, Bitrate, FrameRate, Title, Artist, Album, Genre, TrackNumber, Year,
                    ThumbnailPath, DateAdded, DateModified, LastPlayed, PlayCount, ResumePosition, IsFavorite, FolderId)
                VALUES (@FilePath, @FileName, @Extension, @Type, @FileSize, @Duration, @Width, @Height,
                    @Codec, @AudioCodec, @Bitrate, @FrameRate, @Title, @Artist, @Album, @Genre, @TrackNumber, @Year,
                    @ThumbnailPath, @DateAdded, @DateModified, @LastPlayed, @PlayCount, @ResumePosition, @IsFavorite, @FolderId);
                SELECT last_insert_rowid();
            """, item);
        }
    }

    public async Task UpdatePlaybackAsync(int mediaId, double? resumePos, long playCount, DateTime? lastPlayed)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync(
            "UPDATE MediaItems SET ResumePosition=@ResumePosition, PlayCount=@PlayCount, LastPlayed=@LastPlayed WHERE Id=@Id",
            new { Id = mediaId, ResumePosition = resumePos, PlayCount = playCount, LastPlayed = lastPlayed });
    }

    public async Task ToggleFavoriteAsync(int mediaId, bool isFavorite)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("UPDATE MediaItems SET IsFavorite=@IsFavorite WHERE Id=@Id",
            new { Id = mediaId, IsFavorite = isFavorite });
    }

    public async Task<List<MediaItem>> GetRecentAsync(int limit = 50)
    {
        using var conn = GetConnection();
        return (await conn.QueryAsync<MediaItem>(
            "SELECT * FROM MediaItems WHERE LastPlayed IS NOT NULL ORDER BY LastPlayed DESC LIMIT @Limit",
            new { Limit = limit })).AsList();
    }

    public async Task<List<MediaItem>> GetFavoritesAsync()
    {
        using var conn = GetConnection();
        return (await conn.QueryAsync<MediaItem>(
            "SELECT * FROM MediaItems WHERE IsFavorite = 1 ORDER BY DateAdded DESC")).AsList();
    }

    public async Task<List<MediaFolder>> GetFoldersAsync()
    {
        using var conn = GetConnection();
        return (await conn.QueryAsync<MediaFolder>("SELECT * FROM MediaFolders ORDER BY DateAdded DESC")).AsList();
    }

    public async Task<int> SaveFolderAsync(MediaFolder folder)
    {
        using var conn = GetConnection();
        var existing = await conn.QueryFirstOrDefaultAsync<int?>(
            "SELECT Id FROM MediaFolders WHERE Path = @Path", new { folder.Path });
        if (existing.HasValue)
        {
            folder.Id = existing.Value;
            await conn.ExecuteAsync("""
                UPDATE MediaFolders SET Name=@Name, IncludeSubfolders=@IncludeSubfolders,
                    LastScanned=@LastScanned, IsWatched=@IsWatched WHERE Id=@Id
            """, folder);
            return folder.Id;
        }
        else
        {
            return await conn.QuerySingleAsync<int>("""
                INSERT INTO MediaFolders (Path, Name, IncludeSubfolders, DateAdded, LastScanned, IsWatched)
                VALUES (@Path, @Name, @IncludeSubfolders, @DateAdded, @LastScanned, @IsWatched);
                SELECT last_insert_rowid();
            """, folder);
        }
    }

    public async Task RemoveFolderAsync(int folderId)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("DELETE FROM MediaItems WHERE FolderId = @Id", new { Id = folderId });
        await conn.ExecuteAsync("DELETE FROM MediaFolders WHERE Id = @Id", new { Id = folderId });
    }

    public async Task<List<Playlist>> GetPlaylistsAsync()
    {
        using var conn = GetConnection();
        return (await conn.QueryAsync<Playlist>("SELECT * FROM Playlists ORDER BY DateAdded DESC")).AsList();
    }

    public async Task<int> SavePlaylistAsync(Playlist playlist)
    {
        using var conn = GetConnection();
        if (playlist.Id > 0)
        {
            await conn.ExecuteAsync("""
                UPDATE Playlists SET Name=@Name, Description=@Description, SourceUrl=@SourceUrl,
                    Type=@Type, ItemCount=@ItemCount, DateModified=@DateModified,
                    ArtworkPath=@ArtworkPath, IsDynamic=@IsDynamic WHERE Id=@Id
            """, playlist);
            return playlist.Id;
        }
        else
        {
            return await conn.QuerySingleAsync<int>("""
                INSERT INTO Playlists (Name, Description, SourceUrl, Type, ItemCount, DateAdded, DateModified, ArtworkPath, IsDynamic)
                VALUES (@Name, @Description, @SourceUrl, @Type, @ItemCount, @DateAdded, @DateModified, @ArtworkPath, @IsDynamic);
                SELECT last_insert_rowid();
            """, playlist);
        }
    }

    public async Task RemovePlaylistAsync(int playlistId)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("DELETE FROM PlaylistItems WHERE PlaylistId = @Id", new { Id = playlistId });
        await conn.ExecuteAsync("DELETE FROM Playlists WHERE Id = @Id", new { Id = playlistId });
    }

    public async Task AddPlaylistItemAsync(int playlistId, int mediaItemId, int sortOrder)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("""
            INSERT INTO PlaylistItems (PlaylistId, MediaItemId, SortOrder, DateAdded)
            VALUES (@PlaylistId, @MediaItemId, @SortOrder, @DateAdded)
        """, new { PlaylistId = playlistId, MediaItemId = mediaItemId, SortOrder = sortOrder, DateAdded = DateTime.UtcNow });
    }

    public async Task RemovePlaylistItemAsync(int itemId)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("DELETE FROM PlaylistItems WHERE Id = @Id", new { Id = itemId });
    }

    public async Task ClearPlaylistItemsAsync(int playlistId)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("DELETE FROM PlaylistItems WHERE PlaylistId = @Id", new { Id = playlistId });
    }

    public async Task<List<MediaItem>> GetPlaylistItemsAsync(int playlistId)
    {
        using var conn = GetConnection();
        return (await conn.QueryAsync<MediaItem>("""
            SELECT mi.* FROM PlaylistItems pi
            JOIN MediaItems mi ON mi.Id = pi.MediaItemId
            WHERE pi.PlaylistId = @PlaylistId
            ORDER BY pi.SortOrder
        """, new { PlaylistId = playlistId })).AsList();
    }

    public async Task<string?> GetSettingAsync(string key)
    {
        using var conn = GetConnection();
        return await conn.QueryFirstOrDefaultAsync<string>(
            "SELECT Value FROM AppSettings WHERE Key = @Key", new { Key = key });
    }

    public async Task SetSettingAsync(string key, string value)
    {
        using var conn = GetConnection();
        await conn.ExecuteAsync("""
            INSERT INTO AppSettings (Key, Value) VALUES (@Key, @Value)
            ON CONFLICT(Key) DO UPDATE SET Value = @Value
        """, new { Key = key, Value = value });
    }

    public async Task<int> GetTotalMediaCountAsync()
    {
        using var conn = GetConnection();
        return await conn.QuerySingleAsync<int>("SELECT COUNT(*) FROM MediaItems");
    }

    public async Task<long> GetTotalMediaSizeAsync()
    {
        using var conn = GetConnection();
        return await conn.QuerySingleAsync<long>("SELECT COALESCE(SUM(FileSize), 0) FROM MediaItems");
    }
}
