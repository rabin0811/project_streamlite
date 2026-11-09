using System.Windows;
using System.IO;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;

namespace StreamLite.Helpers;

public static class UiExtensions
{
    public static T? FindVisualChild<T>(this DependencyObject parent) where T : DependencyObject
    {
        for (int i = 0; i < VisualTreeHelper.GetChildrenCount(parent); i++)
        {
            var child = VisualTreeHelper.GetChild(parent, i);
            if (child is T t) return t;
            var result = child.FindVisualChild<T>();
            if (result != null) return result;
        }
        return null;
    }

    public static void SetControlTemplate<T>(this FrameworkElement element, DependencyProperty property, T value)
        where T : DependencyObject
    {
        element.SetValue(property, value);
    }

    public static string Truncate(this string value, int maxLength)
        => value?.Length <= maxLength ? value : value?[..maxLength] + "...";
}

public class DragBehavior
{
    public static readonly DependencyProperty EnableDragProperty =
        DependencyProperty.RegisterAttached("EnableDrag", typeof(bool), typeof(DragBehavior),
            new PropertyMetadata(false, OnEnableDragChanged));

    public static bool GetEnableDrag(DependencyObject obj) => (bool)obj.GetValue(EnableDragProperty);
    public static void SetEnableDrag(DependencyObject obj, bool value) => obj.SetValue(EnableDragProperty, value);

    static void OnEnableDragChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not Window window) return;
        if ((bool)e.NewValue) window.MouseLeftButtonDown += (_, _) => window.DragMove();
    }
}

public class DropFileBehavior
{
    public static readonly DependencyProperty EnableDropProperty =
        DependencyProperty.RegisterAttached("EnableDrop", typeof(bool), typeof(DropFileBehavior),
            new PropertyMetadata(false, OnEnableDropChanged));

    public static bool GetEnableDrop(DependencyObject obj) => (bool)obj.GetValue(EnableDropProperty);
    public static void SetEnableDrop(DependencyObject obj, bool value) => obj.SetValue(EnableDropProperty, value);

    public static readonly DependencyProperty DropCommandProperty =
        DependencyProperty.RegisterAttached("DropCommand", typeof(ICommand), typeof(DropFileBehavior));

    public static ICommand GetDropCommand(DependencyObject obj) => (ICommand)obj.GetValue(DropCommandProperty);
    public static void SetDropCommand(DependencyObject obj, ICommand value) => obj.SetValue(DropCommandProperty, value);

    static void OnEnableDropChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not UIElement element) return;
        if ((bool)e.NewValue)
        {
            element.AllowDrop = true;
            element.DragOver += (_, args) =>
            {
                if (args.Data.GetDataPresent(DataFormats.FileDrop))
                    args.Effects = DragDropEffects.Copy;
            };
            element.Drop += (_, args) =>
            {
                if (args.Data.GetData(DataFormats.FileDrop) is string[] files)
                {
                    var cmd = GetDropCommand(d);
                    if (cmd?.CanExecute(files) == true) cmd.Execute(files);
                }
            };
        }
    }
}

