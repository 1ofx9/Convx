<div align="center">
  <img src="assets/banner%20dark.png" width="1000" alt="Convx banner showing the music player interface">
  <h1>Convx (Modified Fork)</h1>
  <p><strong>A private, self-hosted Android music streaming client — forked from Convx by Cosmic Taser.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=flat&logo=android&logoColor=white" alt="Android 8.0+">
    <img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=flat" alt="GPL-3.0 License">
    <img src="https://img.shields.io/badge/Type-Personal%20Fork-orange?style=flat" alt="Modified Fork">
    <img src="https://img.shields.io/badge/Audio-Lossless%20%2F%20Bit--Perfect-4CAF50?style=flat" alt="Lossless Audio">
    <img src="https://img.shields.io/badge/Telemetry-Zero-critical?style=flat" alt="Zero Telemetry">
  </p>
</div>

---

> [!NOTE]
> **Fork Notice & Credits**: This repository is a customized, independent fork of the original [Convx](https://github.com/cosmictaserdev-creator/Convx) project created and maintained by **Aryan ([Cosmic Taser](https://github.com/cosmictaserdev-creator))**.
> 
> All credit for the original design, architecture, and Cupertino / Liquid Glass interface concepts goes to the original author. This fork adapts Convx for personal use, focusing purely on self-hosted streaming (Navidrome / Subsonic) with zero telemetry, purged web-scraping/AI services, and a refined, glass-free synchronized lyrics engine.

---

## ✨ Changes in This Fork

- **📡 Pure Self-Hosted Streaming**: Exclusively connects to [Navidrome](https://www.navidrome.org/) and Subsonic-compatible servers. Seamlessly streams lossless audio directly from your private library with instant playlist syncing.
- **🎙️ Refined Apple Music Synced Lyrics**: Replaced heavy external lyrics renderers and bubble capsules with clean, word-level progressive reveal, soft active-line glow, and background depth blur.
- **🔒 Stripped Telemetry & Web Scrapers**: Removed all analytics, telemetry, third-party internet search fallbacks, AI translation services, and romanization libraries.
- **⚡ Streamlined Build & Footprint**: Stripped unused dependencies and modules, shrinking the release APK from ~50 MB to ~35 MB.
- **🪟 Fluid Glass Interface**: Preserves the original Cupertino-inspired controls, adaptive album palettes, and smooth spring physics.

---

## 🏗️ Project Structure

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

A single streamlined workflow is provided in [`.github/workflows/build-prod-apk.yml`](.github/workflows/build-prod-apk.yml) to build and sign release APKs in the cloud:

- **Manual Trigger**: Run anytime from the **Actions** tab on GitHub via `workflow_dispatch`.
- **Push to Main**: Automatically packages and uploads the release APK as an artifact.
- **Tagging a Release**: Pushing any tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`) automatically creates a GitHub Release with the APK attached for direct mobile download.

---

## 👏 Acknowledgments & Credits

This project is a modified fork of [Convx](https://github.com/cosmictaserdev-creator/Convx) by **[Aryan (Cosmic Taser)](https://github.com/cosmictaserdev-creator)**. 

Huge thanks to the original creator for designing the calm Cupertino & Liquid Glass aesthetic. All original licenses and attributions are preserved in accordance with the GNU General Public License v3.0.

---

## 📄 License

Convx is licensed under the [GNU General Public License v3.0](LICENSE).
