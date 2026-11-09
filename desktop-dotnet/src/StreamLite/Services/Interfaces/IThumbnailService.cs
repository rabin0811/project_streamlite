namespace StreamLite.Services.Interfaces;

public interface IThumbnailService
{
    Task<string?> GenerateThumbnailAsync(string filePath, int width = 320, int height = 180);
    void ClearCache();
    long GetCacheSize();
}
