namespace StreamLite.Helpers;

/// <summary>
/// Utility methods for formatting display values.
/// </summary>
public static class FormatHelper
{
    /// <summary>
    /// Formats a TimeSpan as a human-readable duration string.
    /// Examples: "3:45", "1:23:45", "0:05"
    /// </summary>
    public static string FormatDuration(TimeSpan duration)
    {
        if (duration.TotalHours >= 1)
            return $"{(int)duration.TotalHours}:{duration.Minutes:D2}:{duration.Seconds:D2}";
        return $"{duration.Minutes}:{duration.Seconds:D2}";
    }

    /// <summary>
    /// Formats a byte count as a human-readable file size.
    /// Examples: "1.5 GB", "342 MB", "12 KB"
    /// </summary>
    public static string FormatFileSize(long bytes)
    {
        string[] suffixes = ["B", "KB", "MB", "GB", "TB"];
        int order = 0;
        double size = bytes;
        while (size >= 1024 && order < suffixes.Length - 1)
        {
            order++;
            size /= 1024;
        }
        return order == 0 ? $"{size:F0} {suffixes[order]}" : $"{size:F1} {suffixes[order]}";
    }

    /// <summary>
    /// Formats a bitrate in bits per second to a display string.
    /// Examples: "5.2 Mbps", "320 kbps"
    /// </summary>
    public static string FormatBitrate(long bitsPerSecond)
    {
        if (bitsPerSecond >= 1_000_000)
            return $"{bitsPerSecond / 1_000_000.0:F1} Mbps";
        if (bitsPerSecond >= 1_000)
            return $"{bitsPerSecond / 1_000.0:F0} kbps";
        return $"{bitsPerSecond} bps";
    }

    /// <summary>
    /// Formats a resolution string from width and height.
    /// Examples: "1920×1080 (Full HD)", "3840×2160 (4K)"
    /// </summary>
    public static string FormatResolution(int width, int height)
    {
        var label = height switch
        {
            >= 4320 => "8K",
            >= 2160 => "4K",
            >= 1440 => "2K",
            >= 1080 => "Full HD",
            >= 720 => "HD",
            >= 480 => "SD",
            _ => ""
        };

        var suffix = string.IsNullOrEmpty(label) ? "" : $" ({label})";
        return $"{width}×{height}{suffix}";
    }

    /// <summary>
    /// Formats a playback speed for display.
    /// </summary>
    public static string FormatSpeed(float speed)
    {
        return speed == 1.0f ? "Normal" : $"{speed:F2}x";
    }

    /// <summary>
    /// Gets a relative time string like "2 hours ago", "Yesterday".
    /// </summary>
    public static string FormatRelativeTime(DateTime dateTime)
    {
        var diff = DateTime.UtcNow - dateTime;

        if (diff.TotalMinutes < 1) return "Just now";
        if (diff.TotalMinutes < 60) return $"{(int)diff.TotalMinutes}m ago";
        if (diff.TotalHours < 24) return $"{(int)diff.TotalHours}h ago";
        if (diff.TotalDays < 2) return "Yesterday";
        if (diff.TotalDays < 7) return $"{(int)diff.TotalDays} days ago";
        if (diff.TotalDays < 30) return $"{(int)(diff.TotalDays / 7)} weeks ago";
        return dateTime.ToString("MMM d, yyyy");
    }
}
