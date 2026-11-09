using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using StreamLite.Services.Interfaces;

namespace StreamLite.Services;

public class ThumbnailService : IThumbnailService
{
    readonly string _cacheDir;

    public ThumbnailService()
    {
        _cacheDir = System.IO.Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "StreamLite", "thumbnails");
        Directory.CreateDirectory(_cacheDir);
    }

    public async Task<string?> GenerateThumbnailAsync(string filePath, int width = 320, int height = 180)
    {
        var cacheKey = GenerateCacheKey(filePath, width, height);
        var cachePath = System.IO.Path.Combine(_cacheDir, cacheKey + ".jpg");
        if (File.Exists(cachePath)) return cachePath;

        var ext = System.IO.Path.GetExtension(filePath).ToLower();
        if (IsImageFile(ext))
        {
            try
            {
                using var fs = new FileStream(filePath, FileMode.Open, FileAccess.Read);
                using var img = System.Drawing.Image.FromStream(fs);
                using var thumb = img.GetThumbnailImage(width, height, null, IntPtr.Zero);
                thumb.Save(cachePath, System.Drawing.Imaging.ImageFormat.Jpeg);
                return cachePath;
            }
            catch { return null; }
        }

        if (IsVideoFile(ext))
        {
            return await Task.Run(() => ExtractVideoThumbnail(filePath, cachePath, width, height));
        }

        return null;
    }

    static string? ExtractVideoThumbnail(string filePath, string outputPath, int width, int height)
    {
        try
        {
            var ffmpeg = FindFfmpeg();
            if (ffmpeg == null) return null;

            var args = $"-i \"{filePath}\" -ss 00:00:05 -vframes 1 -vf \"scale={width}:{height}:force_original_aspect_ratio=decrease,pad={width}:{height}:(ow-iw)/2:(oh-ih)/2\" -y \"{outputPath}\"";
            var psi = new ProcessStartInfo(ffmpeg, args)
            {
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardError = true,
            };
            using var proc = Process.Start(psi);
            proc?.WaitForExit(5000);
            return File.Exists(outputPath) ? outputPath : null;
        }
        catch { return null; }
    }

    static string? FindFfmpeg()
    {
        var possiblePaths = new[]
        {
            "ffmpeg.exe", "ffmpeg",
            System.IO.Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "ffmpeg.exe"),
            System.IO.Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "ffmpeg", "bin", "ffmpeg.exe"),
        };

        foreach (var p in possiblePaths)
        {
            try
            {
                var psi = new ProcessStartInfo(p, "-version")
                {
                    UseShellExecute = false,
                    CreateNoWindow = true,
                    RedirectStandardOutput = true,
                };
                using var proc = Process.Start(psi);
                if (proc != null) { proc.Kill(); return p; }
            }
            catch { }
        }
        return null;
    }

    public void ClearCache()
    {
        if (Directory.Exists(_cacheDir))
        {
            foreach (var f in Directory.GetFiles(_cacheDir)) try { File.Delete(f); } catch { }
        }
    }

    public long GetCacheSize()
    {
        if (!Directory.Exists(_cacheDir)) return 0;
        return Directory.GetFiles(_cacheDir).Sum(f => new FileInfo(f).Length);
    }

    static string GenerateCacheKey(string filePath, int width, int height)
    {
        var hash = filePath.GetHashCode().ToString("x8");
        var modified = File.GetLastWriteTimeUtc(filePath).Ticks.ToString("x8");
        return $"{hash}_{modified}_{width}x{height}";
    }

    static bool IsImageFile(string ext) => ext is ".png" or ".jpg" or ".jpeg" or ".webp" or ".gif" or ".bmp";
    static bool IsVideoFile(string ext) => ext is ".mp4" or ".mkv" or ".avi" or ".mov" or ".webm" or ".flv" or ".wmv" or ".ts" or ".mpeg" or ".mpg";
}



