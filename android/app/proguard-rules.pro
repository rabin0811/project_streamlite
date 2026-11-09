# StreamLite ProGuard Rules

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepresources META-INF/services/androidx.media3.common.MediaSource$Factory
-keepresources META-INF/services/*

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes
-keep class com.streamlite.app.data.model.** { *; }
-keep class com.streamlite.app.data.local.entity.** { *; }
-keep class com.streamlite.app.domain.model.** { *; }
