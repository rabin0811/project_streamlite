# project_streamlite

## StreamLite

A cross-platform media player with three implementations: a .NET WPF desktop app, an Electron desktop app, and an Android app.

- Original location: `D:\projects\_Project_Stream_lite_`
- Working copy: `D:\PROJECTS_READY\project_streamlite`
- Stack: .NET/WPF, Electron + React/Vite, Android/Kotlin

## Folder layout

```text
project_streamlite/
  desktop-dotnet/    WPF desktop solution
  desktop-electron/  Electron + React/Vite desktop app
  android/           Android application
  docs/              Project documentation
```

## Build and run

### .NET WPF desktop

```powershell
cd desktop-dotnet
dotnet build StreamLiteDesktop.sln -c Release
```

### Electron desktop

```powershell
cd desktop-electron
npm ci
npm run build
npm run electron:start
```

For development: `npm run electron:dev`.

### Android

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

The debug APK is written under `android/app/build/outputs/apk/debug/`.

## Verified status (2026-11-09)

- [x] WPF solution built with 16 warnings and 0 errors
- [x] Electron frontend built with Vite
- [x] Android debug APK built successfully (41 tasks)
- [x] Android build used the bundled Android Studio JDK 21 and SDK 36
- [ ] Electron application window not launched during the final verification pass
- [ ] No automated tests

## Notes and limitations

- The three implementations are separate codebases and may not share identical features.
- Android builds require Android Studio/SDK and a compatible JDK.
- Electron packaging (`electron:build`) was not run; only the Vite frontend build was verified.
- Build output, APKs, and dependencies are ignored by Git.

## Troubleshooting

- **Android SDK not found**: set `ANDROID_HOME` or create `android/local.properties` with `sdk.dir`.
- **Gradle JDK error**: point Gradle at Android Studio’s bundled JDK 21.
- **Electron window does not open**: run `npm run build` first, then `npm run electron:start`.
- **.NET SDK mismatch**: install the .NET version targeted by the solution or use the isolated SDK noted in the workspace docs.
