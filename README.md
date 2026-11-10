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

For development: `npm run electron:dev` starts Vite and launches Electron with `--dev`.
`npm run electron:start` loads the built `dist/index.html`, so run `npm run build` first.

### Android

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

The debug APK is written under `android/app/build/outputs/apk/debug/`.

## Playback support

- Local files: MP4/WebM and other Chromium-supported formats through the HTML5 `<video>` element.
- Network streams: direct HTTP(S) media URLs are supported.
- IPTV playlists: M3U parsing is available. HLS (`.m3u8`) playback depends on Chromium codec support and is not guaranteed. RTMP/MMS are not supported by the built-in HTML5 player.
- The legacy native VLC engine hook remains a stub; the verified path is HTML5 playback in the Electron renderer.

## Verified status (2026-11-10)

- [x] WPF solution built with 16 warnings and 0 errors
- [x] Electron frontend built with Vite
- [x] Android debug APK built successfully (41 tasks)
- [x] Android build used the bundled Android Studio JDK 21 and SDK 36
- [x] Electron window launched and loaded `dist/index.html`
- [x] `HashRouter` used for `file://` routing in Electron
- [x] HTML5 playback verified end-to-end through the Network screen with a network MP4 (duration 5.055 s, `readyState` 4, playback advanced past 2 s); direct `file://` MP4 playback was also confirmed
- [ ] No automated tests
- [ ] Electron packaging (`electron:build`) not run

## Notes and limitations

- The three implementations are separate codebases and may not share identical features.
- Android builds require Android Studio/SDK and a compatible JDK.
- Electron packaging (`electron:build`) was not run; only the Vite frontend build and unpackaged Electron launch were verified.
- Build output, APKs, and dependencies are ignored by Git.

## Troubleshooting

- **Android SDK not found**: set `ANDROID_HOME` or create `android/local.properties` with `sdk.dir`.
- **Gradle JDK error**: point Gradle at Android Studio’s bundled JDK 21.
- **Electron window is blank**: run `npm run build` before `npm run electron:start`. The launcher only uses the Vite dev server when `--dev` is passed.
- **Electron dev mode**: use `npm run electron:dev`; it starts Vite and passes `--dev` to Electron.
- **Video does not play**: use a Chromium-supported codec/container (for example MP4/H.264 or WebM). HLS/RTMP/MMS are not supported by the built-in HTML5 player.
- **.NET SDK mismatch**: install the .NET version targeted by the solution or use the isolated SDK noted in the workspace docs.
