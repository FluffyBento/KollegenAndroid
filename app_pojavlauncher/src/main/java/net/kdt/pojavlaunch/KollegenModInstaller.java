package net.kdt.pojavlaunch;

import android.util.Log;

import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class KollegenModInstaller {
    public static final String COMPANION_MOD_FILENAME = "kollegen-client-mod.jar";
    public static final String COMPANION_TARGET_MC_VERSION = "1.21.11";

    private static final String MOD_DOWNLOAD_URL =
            "https://github.com/FluffyBento/KollegenClient/releases/latest/download/kollegen-client-mod.jar";

    private KollegenModInstaller() {}

    private static File cacheDir() {
        return new File(Tools.DIR_CACHE, "kollegen");
    }

    private static boolean isCompatible(String version, String loader) {
        if (!COMPANION_TARGET_MC_VERSION.equals(version)) return false;
        if (loader == null) return false;
        String lc = loader.toLowerCase(Locale.ROOT);
        return lc.contains("fabric") || lc.contains("quilt");
    }

    public static String mcVersionFrom(String lastVersionId) {
        if (lastVersionId == null) return null;
        int dash = lastVersionId.lastIndexOf('-');
        return dash >= 0 ? lastVersionId.substring(dash + 1) : lastVersionId;
    }

    public static String loaderFrom(String lastVersionId) {
        if (lastVersionId == null) return null;
        String lc = lastVersionId.toLowerCase(Locale.ROOT);
        if (lc.startsWith("fabric")) return "fabric";
        if (lc.startsWith("quilt")) return "quilt";
        return null;
    }

    private static final String[] RENDERER_JARS = {
            "VulkanMod.jar", "sodium.jar", "iris.jar", "beryl.jar"
    };

    public static void ensureFor(MinecraftProfile profile, String version, String loader) {
        if (profile == null) return;
        File gameDir = Tools.getGameDirPath(profile);
        File modsDir = new File(gameDir, "mods");
        removeCorruptJars(modsDir);
        if (!isCompatible(version, loader)) {
            removeIncompatible(modsDir);
            return;
        }
        File cached = new File(cacheDir(), COMPANION_MOD_FILENAME);
        try {
            long remoteSize = DownloadUtils.getContentLength(MOD_DOWNLOAD_URL);
            if (!isValidMod(cached, true) || (remoteSize > 0 && cached.length() != remoteSize)) {
                DownloadUtils.downloadFile(MOD_DOWNLOAD_URL, cached);
            }
            if (!isValidMod(cached, true) && !cached.delete()) {
                Log.w("KollegenModInstaller", "Ungueltige Mod-Datei konnte nicht geloescht werden");
            }
        } catch (IOException e) {
            Log.w("KollegenModInstaller", "Mod-Prüfung fehlgeschlagen", e);
            if (!isValidMod(cached, true)) {
                cached.delete();
            }
        }
        copyToMods(cached, modsDir);
    }

    private static boolean isValidMod(File file, boolean requireDescriptor) {
        if (file == null || !file.isFile() || file.length() == 0) return false;
        try (ZipFile zip = new ZipFile(file)) {
            if (requireDescriptor) {
                ZipEntry entry = zip.getEntry("fabric.mod.json");
                if (entry == null) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void removeCorruptJars(File modsDir) {
        if (modsDir == null || !modsDir.isDirectory()) return;
        File[] files = modsDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !isRendererJar(file.getName())) continue;
            if (!isValidMod(file, false)) {
                file.delete();
                Log.w("KollegenModInstaller", "Korrupte Renderer-Mod entfernt: " + file.getName());
                File disabled = new File(modsDir, file.getName() + ".disabled");
                if (disabled.isFile() && !isValidMod(disabled, false)) {
                    disabled.delete();
                }
            }
        }
    }

    private static boolean isRendererJar(String name) {
        for (String renderer : RENDERER_JARS) {
            if (renderer.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static void copyToMods(File cached, File modsDir) {
        if (!isValidMod(cached, true)) return;
        if (!modsDir.isDirectory() && !modsDir.mkdirs()) return;
        try {
            Files.copy(cached.toPath(), new File(modsDir, COMPANION_MOD_FILENAME).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Log.w("KollegenModInstaller", "Mod-Kopie fehlgeschlagen: " + modsDir, e);
        }
    }

    private static void removeIncompatible(File modsDir) {
        if (!modsDir.isDirectory()) return;
        File[] mods = modsDir.listFiles(f -> f.isFile() && f.getName().toLowerCase(Locale.ROOT).startsWith("kollegen-client") && f.getName().endsWith(".jar"));
        if (mods == null) return;
        for (File mod : mods) mod.delete();
    }
}