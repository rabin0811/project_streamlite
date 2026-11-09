using System.Collections.ObjectModel;
using System.IO;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using StreamLite.Models;
using StreamLite.Services.Interfaces;

namespace StreamLite.ViewModels;

public partial class LibraryViewModel : ObservableObject
{
    readonly ILibraryService _library;
    readonly IThumbnailService _thumbnails;

    [ObservableProperty] ObservableCollection<MediaItem> _mediaItems = [];
    [ObservableProperty] ObservableCollection<MediaItem> _recentItems = [];
    [ObservableProperty] ObservableCollection<MediaItem> _favoriteItems = [];
    [ObservableProperty] ObservableCollection<MediaFolder> _folders = [];
    [ObservableProperty] ObservableCollection<MediaItem> _resumeCandidates = [];
    [ObservableProperty] string _searchQuery = string.Empty;
    [ObservableProperty] bool _isLoading;
    [ObservableProperty] MediaType _selectedFilter;
    [ObservableProperty] bool _isGridView = true;
    [ObservableProperty] int _totalCount;
    [ObservableProperty] string _totalSizeFormatted = "";
    [ObservableProperty] MediaItem? _selectedMedia;

    public LibraryViewModel(ILibraryService library, IThumbnailService thumbnails)
    {
        _library = library;
        _thumbnails = thumbnails;
        _library.LibraryUpdated += async (_, _) => await LoadAsync();
    }

    public async Task LoadAsync()
    {
        IsLoading = true;
        try
        {
            var types = SelectedFilter == MediaType.Unknown ? null : (MediaType?)SelectedFilter;
            MediaItems = new ObservableCollection<MediaItem>(await _library.GetMediaAsync(types));
            RecentItems = new ObservableCollection<MediaItem>(await _library.GetRecentAsync(20));
            FavoriteItems = new ObservableCollection<MediaItem>(await _library.GetFavoritesAsync());
            Folders = new ObservableCollection<MediaFolder>(await _library.GetFoldersAsync());
            ResumeCandidates = new ObservableCollection<MediaItem>(await _library.GetResumeCandidatesAsync());
            TotalCount = await _library.GetTotalCountAsync();
            var size = await _library.GetTotalSizeAsync();
            TotalSizeFormatted = size switch
            {
                < 1024 * 1024 => $"{size / 1024.0:F1} KB",
                < 1024L * 1024 * 1024 => $"{size / (1024.0 * 1024):F1} MB",
                _ => $"{size / (1024.0 * 1024 * 1024):F2} GB"
            };
        }
        finally { IsLoading = false; }
    }

    [RelayCommand]
    async Task Search()
    {
        if (string.IsNullOrWhiteSpace(SearchQuery))
        { await LoadAsync(); return; }
        var results = await _library.SearchAsync(SearchQuery, SelectedFilter == MediaType.Unknown ? null : SelectedFilter);
        MediaItems = new ObservableCollection<MediaItem>(results);
    }

    [RelayCommand]
    async Task AddFolder()
    {
        var dialog = new Ookii.Dialogs.Wpf.VistaFolderBrowserDialog();
        if (dialog.ShowDialog() == true)
        {
            IsLoading = true;
            await _library.ScanFolderAsync(dialog.SelectedPath);
            await LoadAsync();
            IsLoading = false;
        }
    }

    [RelayCommand]
    async Task RemoveFolder(MediaFolder folder)
    {
        await _library.RemoveFolderAsync(folder.Id);
        await LoadAsync();
    }

    [RelayCommand]
    async Task ToggleFavorite(MediaItem item)
    {
        await _library.ToggleFavoriteAsync(item);
        await LoadAsync();
    }

    [RelayCommand]
    async Task Refresh() => await _library.GetFoldersAsync();

    partial void OnSelectedFilterChanged(MediaType value) => _ = LoadAsync();
    partial void OnSearchQueryChanged(string value) => _ = Search();

    public async Task<string?> GetThumbnailAsync(MediaItem item)
    {
        return await _thumbnails.GenerateThumbnailAsync(item.FilePath);
    }
}

