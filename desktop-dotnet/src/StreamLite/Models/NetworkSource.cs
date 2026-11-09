namespace StreamLite.Models;

/// <summary>
/// Represents a saved network source (SMB share, FTP server, WebDAV, etc.).
/// </summary>
public class NetworkSource
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public NetworkProtocol Protocol { get; set; }
    public string Host { get; set; } = string.Empty;
    public int? Port { get; set; }
    public string? Path { get; set; }
    public string? Username { get; set; }
    public string? EncryptedPassword { get; set; }
    public DateTime? LastConnected { get; set; }
    public bool IsFavorite { get; set; }
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;

    /// <summary>
    /// Constructs the full URI for this network source.
    /// </summary>
    public string GetUri()
    {
        var scheme = Protocol switch
        {
            NetworkProtocol.SMB => "smb",
            NetworkProtocol.FTP => "ftp",
            NetworkProtocol.WebDAV => "http",
            NetworkProtocol.HTTP => "http",
            NetworkProtocol.HTTPS => "https",
            NetworkProtocol.RTSP => "rtsp",
            _ => "http"
        };

        var port = Port.HasValue ? $":{Port}" : string.Empty;
        var path = string.IsNullOrEmpty(Path) ? "/" : Path;
        return $"{scheme}://{Host}{port}{path}";
    }
}

/// <summary>
/// A record in stream playback history.
/// </summary>
public class StreamHistoryItem
{
    public long Id { get; set; }
    public string Url { get; set; } = string.Empty;
    public string? Title { get; set; }
    public DateTime PlayedAt { get; set; } = DateTime.UtcNow;
    public TimeSpan? Duration { get; set; }
}

public enum NetworkProtocol
{
    HTTP,
    HTTPS,
    FTP,
    SMB,
    WebDAV,
    RTSP,
    UDP,
    RTP,
    HLS,
    DASH
}
