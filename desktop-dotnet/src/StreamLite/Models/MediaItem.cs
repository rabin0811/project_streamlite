namespace StreamLite.Models;

public enum MediaType { Video, Audio, Image, Playlist, Stream, Unknown }

public class MediaItem
{
    public int Id { get; set; }
    public string FilePath { get; set; } = string.Empty;
    public string FileName { get; set; } = string.Empty;
    public string Extension { get; set; } = string.Empty;
    public MediaType Type { get; set; } = MediaType.Unknown;
    public long FileSize { get; set; }
    public TimeSpan? Duration { get; set; }
    public int? Width { get; set; }
    public int? Height { get; set; }
    public string? Codec { get; set; }
    public string? AudioCodec { get; set; }
    public int? Bitrate { get; set; }
    public double? FrameRate { get; set; }
    public string? Title { get; set; }
    public string? Artist { get; set; }
    public string? Album { get; set; }
    public string? Genre { get; set; }
    public int? TrackNumber { get; set; }
    public int? Year { get; set; }
    public string? ThumbnailPath { get; set; }
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;
    public DateTime? DateModified { get; set; }
    public DateTime? LastPlayed { get; set; }
    public long PlayCount { get; set; }
    public double? ResumePosition { get; set; }
    public bool IsFavorite { get; set; }
    public int? FolderId { get; set; }
    public string? FolderPath { get; set; }

    public string DisplayName => Title ?? FileName;
    public string Dimensions => Width.HasValue && Height.HasValue ? $"{Width}x{Height}" : "";
    public string FileSizeFormatted => FileSize switch
    {
        < 1024 => $"{FileSize} B",
        < 1024 * 1024 => $"{FileSize / 1024.0:F1} KB",
        < 1024L * 1024 * 1024 => $"{FileSize / (1024.0 * 1024):F1} MB",
        _ => $"{FileSize / (1024.0 * 1024 * 1024):F2} GB"
    };
    public string DurationFormatted => Duration.HasValue
        ? (Duration.Value.TotalHours >= 1
            ? Duration.Value.ToString(@"h\:mm\:ss")
            : Duration.Value.ToString(@"mm\:ss"))
        : "";
    public bool IsImage => Type == MediaType.Image;
    public bool IsAudio => Type == MediaType.Audio;
    public bool IsVideo => Type == MediaType.Video;
    public bool IsPlayable => Type is MediaType.Video or MediaType.Audio or MediaType.Stream;
}

public class MediaFolder
{
    public int Id { get; set; }
    public string Path { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public bool IncludeSubfolders { get; set; } = true;
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;
    public DateTime? LastScanned { get; set; }
    public bool IsWatched { get; set; }
}
