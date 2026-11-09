# Build notes

## Desktop (.NET)

Open `desktop-dotnet/StreamLiteDesktop.sln` in Visual Studio, or run:

```powershell
dotnet build .\desktop-dotnet\StreamLiteDesktop.sln
```

## Desktop (Electron)

```powershell
cd .\desktop-electron
npm install
npm run build
```

## Android

Open the `android/` folder in Android Studio, or run:

```powershell
cd .\android
.\gradlew.bat assembleDebug
```

Detailed verification results will be added in Phase 2.
