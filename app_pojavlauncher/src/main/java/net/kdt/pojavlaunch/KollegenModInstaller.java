package net.kdt.pojavlaunch;

import android.util.Log;

import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

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

    public static void ensureFor(MinecraftProfile profile, String version, String loader) {
        if (profile == null) return;
        File gameDir = Tools.getGameDirPath(profile);
        if (!isCompatible(version, loader)) {
            removeIncompatible(new File(gameDir, "mods"));
            return;
        }
        File cached = new File(cacheDir(), COMPANION_MOD_FILENAME);
        if (!cached.isFile() || cached.length() == 0) {
            try {
                DownloadUtils.downloadFile(MOD_DOWNLOAD_URL, cached);
            } catch (IOException e) {
                Log.w("KollegenModInstaller", "Mod-Download fehlgeschlagen", e);
                return;
            }
        }
        copyToMods(cached, new File(gameDir, "mods"));
    }

    private static void copyToMods(File cached, File modsDir) {
        if (!cached.isFile() || cached.length() == 0) return;
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