using System.Windows;

namespace StreamLite.Views.Dialogs;

public partial class OpenUrlDialog : Window
{
    public string StreamUrl => UrlBox.Text.Trim();

    public OpenUrlDialog()
    {
        InitializeComponent();
        UrlBox.Focus();
        UrlBox.KeyDown += (_, e) => { if (e.Key == System.Windows.Input.Key.Enter) DialogResult = true; };
    }

    void OkClick(object sender, RoutedEventArgs e) => DialogResult = true;
    void CancelClick(object sender, RoutedEventArgs e) => DialogResult = false;
}
