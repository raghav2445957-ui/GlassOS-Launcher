# GlassOS Launcher

A fresh Android launcher project for Android 11+ (API 30). This is the Phase 1 foundation: a real HOME activity, installed-app discovery, app launching, a basic home surface, and an app drawer.

## Build
- Open this repository in Android Studio (JDK 17).
- Allow Gradle sync.
- Build > Build Bundle(s) / APK(s) > Build APK(s).
- APK output: `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow also builds the debug APK on every push and pull request. Download it from the workflow run's Artifacts section.

## Install and select
Install the debug APK, then use Android Settings > Apps > Default apps > Home app (wording varies by device) and select **GlassOS Launcher**.

## Android 11 notes
Android 11 is API 30 and is supported. Android 11 does not provide the same public real-time cross-window blur APIs as newer Android versions, so the initial release uses translucent surfaces rather than promising system-level live blur.

## Roadmap
1. Phase 1: launcher shell, app discovery, home and app drawer.
2. Phase 2: liquid-glass surfaces and version-aware blur implementation.
3. Phase 3: drag/drop, folders and widgets.
4. Phase 4: gestures, search and secondary pages.
5. Phase 5: settings, backup/restore and performance polish.

This is an early foundation, not a finished production launcher yet.


## Build
Pushes to `main` automatically build the debug APK with GitHub Actions. The artifact is named `GlassOS-debug-apk`.
