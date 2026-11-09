namespace StreamLite.Models;

public class Playlist
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string? SourceUrl { get; set; }
    public PlaylistType Type { get; set; } = PlaylistType.User;
    public int ItemCount { get; set; }
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;
    public DateTime? DateModified { get; set; }
    public string? ArtworkPath { get; set; }
    public bool IsDynamic { get; set; }
}

public enum PlaylistType { User, Smart, M3U, Network }

public class PlaylistItem
{
    public int Id { get; set; }
    public int PlaylistId { get; set; }
    public int MediaItemId { get; set; }
    public int SortOrder { get; set; }
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;

    public MediaItem? Media { get; set; }
    public Playlist? Playlist { get; set; }
}
