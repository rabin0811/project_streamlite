using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using StreamLite.Models;

namespace StreamLite.Views.Pages;

public partial class PlaylistPage : UserControl
{
    public PlaylistPage() => InitializeComponent();

    void PlaylistItemMouseDown(object sender, MouseButtonEventArgs e)
    {
        if (sender is Border border && border.DataContext is Playlist playlist)
        {
            var vm = DataContext as ViewModels.MainViewModel;
            vm?.PlaylistVM.SelectPlaylistCommand.Execute(playlist);
        }
    }
}
