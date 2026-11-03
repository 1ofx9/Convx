<div align="center">
  <img src="assets/banner%20dark.png" width="1000" alt="Convx banner showing the music player interface">
  <h1>Convx</h1>
  <p><strong>A modern, private Android streaming client for self-hosted music servers.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=flat&logo=android&logoColor=white" alt="Android 8.0+">
    <img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=flat" alt="GPL-3.0 License">
    <img src="https://img.shields.io/badge/Audio-Lossless%20%2F%20Bit--Perfect-4CAF50?style=flat" alt="Lossless Audio">
    <img src="https://img.shields.io/badge/Telemetry-Zero-critical?style=flat" alt="Zero Telemetry">
  </p>
</div>

---

Convx is an elegant, personal Android music player focused purely on self-hosted music streaming. Built with Kotlin, Jetpack Compose, and Media3, it pairs high-fidelity audio playback with a refined Cupertino and Liquid Glass interface.

## ✨ Key Features

- **📡 Pure Self-Hosted Streaming**: Built for [Navidrome](https://www.navidrome.org/) and Subsonic-compatible servers. Seamlessly streams lossless audio directly from your private library with instant playlist synchronization.
- **🎙️ Synced Lyrics**: Fluid, synchronized lyrics with word-level progressive reveal, soft active-line glow, and background depth blur.
- **🪟 Fluid Glass Interface**: Cupertino-inspired controls, adaptive album artwork palettes, smooth spring animations, and subtle frosted surfaces.
- **🔒 Private & Bloat-Free**: Zero telemetry, no third-party trackers, no ad networks, and no external AI models. Your library and listening habits stay strictly between you and your server.
- **⚡ Compact & Optimized**: Pruned dependencies and streamlined architecture for fast cold starts, minimal battery usage, and an optimized ~35 MB release package.

---

## 🏗️ Project Structure

Convx is organized into focused Gradle modules:

| Module | Purpose |
| --- | --- |
| `:app` | Application shell, screens, navigation, Media3 playback service, and Room database. |
| `:network-storage` | Subsonic / Navidrome network integration, library synchronization, and streaming sources. |
| `:lyrics` | Synchronized lyrics parser, timeline indexing, and provider logic. |
| `:artwork` | Album art fetching, caching, and color palette extraction. |
| `:hig` | Cupertino-style UI components, liquid glass surfaces, and design primitives. |
| `:canvas` | Canvas rendering utilities and backdrop animations. |
| `:innertube` | Shared core media models and data structures. |

---

## 🛠️ Building Locally

### Prerequisites

- **JDK 21**
- **Android SDK** (API 26+)
- **Android NDK** (for native audio components)

### Build Commands

Clone the repository and build using the Gradle wrapper:

```bash
# Build the signed Universal FOSS Release APK
./gradlew assembleUniversalFossRelease

# Or build the Debug variant for testing
./gradlew assembleUniversalFossDebug
```

On Windows (PowerShell / Command Prompt):

```powershell
.\gradlew.bat assembleUniversalFossRelease
```

The compiled release APK will be located at:
```
app/build/outputs/apk/universalFoss/release/app-universal-foss-release.apk
```

---

## 🚀 Cloud Builds (GitHub Actions)

A single workflow is provided in [`.github/workflows/build-prod-apk.yml`](.github/workflows/build-prod-apk.yml) to build and sign release APKs in the cloud:

- **Manual Trigger**: Run anytime from the **Actions** tab on GitHub via `workflow_dispatch`.
- **Push to Main**: Automatically packages and uploads the release APK as an artifact.
- **Tagging a Release**: Pushing any tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`) automatically creates a GitHub Release with the APK attached for direct mobile download.

---

## 📄 License

Convx is licensed under the [GNU General Public License v3.0](LICENSE).
