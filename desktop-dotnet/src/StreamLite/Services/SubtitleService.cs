using System.IO;
namespace StreamLite.Services;

public class SubtitleService
{
    static readonly HashSet<string> SubtitleExts = [".srt", ".ass", ".ssa", ".vtt", ".sub", ".idx", ".pgs"];

    public List<string> FindSubtitles(string videoPath)
    {
        var dir = System.IO.Path.GetDirectoryName(videoPath);
        var nameWithoutExt = System.IO.Path.GetFileNameWithoutExtension(videoPath);
        if (dir == null) return [];

        return Directory.EnumerateFiles(dir)
            .Where(f => SubtitleExts.Contains(System.IO.Path.GetExtension(f).ToLower()))
            .Where(f =>
            {
                var fn = System.IO.Path.GetFileNameWithoutExtension(f).ToLower();
                return fn == nameWithoutExt.ToLower() ||
                       fn.StartsWith(nameWithoutExt.ToLower() + ".");
            })
            .OrderBy(f => System.IO.Path.GetExtension(f) == ".srt" ? 0 :
                          System.IO.Path.GetExtension(f) == ".vtt" ? 1 : 2)
            .ToList();
    }

    public static bool IsSubtitleFile(string path) =>
        SubtitleExts.Contains(System.IO.Path.GetExtension(path).ToLower());

    public async Task<string?> DownloadFromOpenSubtitles(string hash, string language = "eng")
    {
        // Placeholder for OpenSubtitles API integration
        await Task.CompletedTask;
        return null;
    }
}

