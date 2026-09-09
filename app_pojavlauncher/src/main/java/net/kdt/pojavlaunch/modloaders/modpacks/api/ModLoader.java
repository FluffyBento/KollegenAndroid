package net.kdt.pojavlaunch.modloaders.modpacks.api;

import android.content.Context;
import android.content.Intent;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.modloaders.FabriclikeDownloadTask;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask;
import net.kdt.pojavlaunch.modloaders.ForgeUtils;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.NeoForgeDownloadTask;

import java.io.File;

public class ModLoader {
    public static final int MOD_LOADER_FORGE = 0;
    public static final int MOD_LOADER_FABRIC = 1;
    public static final int MOD_LOADER_QUILT = 2;
    public static final int MOD_LOADER_NEOFORGE = 3;
    public final int modLoaderType;
    public final String modLoaderVersion;
    public final String minecraftVersion;

    public ModLoader(int modLoaderType, String modLoaderVersion, String minecraftVersion) {
        this.modLoaderType = modLoaderType;
        this.modLoaderVersion = modLoaderVersion;
        this.minecraftVersion = minecraftVersion;
    }

    
    public String getVersionId() {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                return minecraftVersion+"-forge-"+modLoaderVersion;
            case MOD_LOADER_FABRIC:
                return "fabric-loader-"+modLoaderVersion+"-"+minecraftVersion;
            case MOD_LOADER_QUILT:
                return "quilt-loader-"+modLoaderVersion+"-"+minecraftVersion;
            case MOD_LOADER_NEOFORGE:
                return "neoforge-"+modLoaderVersion;
            default:
                return null;
        }
    }

    
    public Runnable getDownloadTask(ModloaderDownloadListener listener) {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                return new ForgeDownloadTask(listener, minecraftVersion, modLoaderVersion);
            case MOD_LOADER_FABRIC:
                return createFabriclikeTask(listener, FabriclikeUtils.FABRIC_UTILS);
            case MOD_LOADER_QUILT:
                return createFabriclikeTask(listener, FabriclikeUtils.QUILT_UTILS);
            case MOD_LOADER_NEOFORGE:
                return new NeoForgeDownloadTask(listener, modLoaderVersion);
            default:
                return null;
        }
    }

    
    public Intent getInstallationIntent(Context context, File modInstallerJar) {
        Intent baseIntent = new Intent(context, JavaGUILauncherActivity.class);
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                ForgeUtils.addAutoInstallArgs(baseIntent, modInstallerJar, getVersionId());
                return baseIntent;
            case MOD_LOADER_NEOFORGE:
                return baseIntent
                        .putExtra("javaArgs", "-jar "+modInstallerJar.getAbsolutePath()+" --install-client")
                        .putExtra("openLogOutput", true)
                        ;
            case MOD_LOADER_QUILT:
            case MOD_LOADER_FABRIC:
            default:
                return null;
        }
    }

    
    public boolean requiresGuiInstallation() {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
            case MOD_LOADER_NEOFORGE:
                return true;
            case MOD_LOADER_FABRIC:
            case MOD_LOADER_QUILT:
            default:
                return false;
        }
    }

    private FabriclikeDownloadTask createFabriclikeTask(ModloaderDownloadListener modloaderDownloadListener, FabriclikeUtils utils) {
        return new FabriclikeDownloadTask(modloaderDownloadListener, utils, minecraftVersion, modLoaderVersion, false);
    }
}
