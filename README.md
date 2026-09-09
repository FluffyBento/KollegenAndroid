# Kollegen Launcher Android

Android-Version des [Kollegen Clients](https://kollegen.dev) – ein Launcher für
Minecraft Java Edition auf Android, basiert auf [Amethyst](https://github.com/AngelAuraMC/Amethyst-Android)
(fork of [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncherTeam)).

> **Hinweis:** Bitte kopiere Fehler und hänge Screenshots an, wenn du ein Issue erstellst.

---

## Installation

Die fertige Debug-APK wird per GitHub Actions gebaut und liegt unter den
Artifacts der angelegten Workflow-Läufe
(`kollegen-android-debug`). Es wird keine signierte Release-APK angeboten.

Alternativ lokal bauen:

```bash
./gradlew :app_pojavlauncher:assembleDebug
```

Der Build lädt die benötigten JRE-Assets aus
[AngelAuraMC/angelauramc-openjdk-build](https://github.com/AngelAuraMC/angelauramc-openjdk-build).

---

## Was ist drin?

- **Kollegen-Instanzen**: Fabric, Forge, NeoForge, Quilt, VulkanMod
- Microsoft-Account mit Gerätelogin
- Mods via Modrinth
- Kollegen-Design (dunkles Layout, Orange-Akzent)

## Lizenz

Dieses Projekt ist ein Fork und steht unter **LGPL-3.0** (siehe `LICENSE`).
Der zugrunde liegende Launcher stammt von Amethyst/PojavLauncher.