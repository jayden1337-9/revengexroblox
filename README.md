# IconForge

A lightweight Android launcher focused on deep visual customization.

## Current target
- Java + native Android views
- Android 8.0+ (API 26+), initially tested on Android 14 / API 34
- GitHub Actions builds debug APKs
- Vertical alphabetical app drawer
- Launcher-local per-app labels and icon overrides
- No root required

## Important Android limitation
IconForge customizes how apps appear **inside IconForge**. Android does not provide ordinary third-party apps with a universal API to replace icons in another launcher or inside other apps. Set IconForge as the default Home app to use its drawer and home screen.

## Build
Push to `main` or run the **Build IconForge APK** workflow manually. Download the `IconForge-debug-apk` artifact from the workflow run.

## Project status
Early foundation. App listing, launching, search, and launcher-local customization are being developed incrementally.
