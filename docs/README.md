# StreamLite — Technical Notes

## Overview

StreamLite is a media-player project with three separate implementations:

1. **WPF desktop** — .NET desktop application.
2. **Electron desktop** — React/Vite renderer packaged with Electron.
3. **Android** — Kotlin Android application.

The implementations share the StreamLite product idea but are separate codebases.

## Folder structure

```text
project_streamlite/
  desktop-dotnet/    .NET WPF solution
  desktop-electron/  Electron main process, React/Vite renderer, build output
  android/           Android Gradle project
  docs/              Documentation
```

## Build commands

### WPF

```powershell
cd desktop-dotnet
dotnet build StreamLiteDesktop.sln -c Release
```

### Electron

```powershell
cd desktop-electron
npm ci
npm run build
npm run electron:start
```

Development mode:

```powershell
npm run electron:dev
```

### Android

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

The debug APK is written under `android/app/build/outputs/apk/debug/`.

## Verification evidence (2026-11-09)

| Check | Result |
| --- | --- |
| WPF `dotnet build` Release | Passed with 16 warnings, 0 errors |
| Electron Vite build | Passed |
| Electron window launch | Not run in the final pass |
| Android `assembleDebug` | Passed, 41 tasks, debug APK built |
| Android JDK | Android Studio bundled OpenJDK 21.0.10 |
| Android SDK | SDK 36 |

## Known limitations

- Electron packaging (`electron:build`) was not run; only the Vite renderer build was verified.
- The Electron window was not launched during the final verification pass.
- The three implementations may not have identical features.
- Android builds require Android Studio, SDK 36, and a compatible JDK.
- No automated tests were run.

## Troubleshooting

- **Gradle JDK error**: point Gradle at Android Studio's bundled JDK 21.
- **SDK not found**: set `ANDROID_HOME` or create `android/local.properties`.
- **Electron blank window**: run `npm run build` before `npm run electron:start`.
- **.NET SDK mismatch**: use the .NET version targeted by the solution.

## Publishing notes

- Exclude `node_modules/`, `dist/`, `bin/`, `obj/`, `.gradle/`, APKs, and local SDK paths.
- Do not commit signing keys or keystores.
