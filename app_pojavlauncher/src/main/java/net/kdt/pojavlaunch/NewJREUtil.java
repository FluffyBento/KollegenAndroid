package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;
import static net.kdt.pojavlaunch.Architecture.getDeviceArchitecture;
import static net.kdt.pojavlaunch.Tools.NATIVE_LIB_DIR;
import static net.kdt.pojavlaunch.Tools.isOnline;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.progresskeeper.DownloaderProgressWrapper;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.MathUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NewJREUtil {
    private static boolean checkInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime) {
        String launcher_runtime_version;
        String installed_runtime_version = MultiRTUtils.readInternalRuntimeVersion(internalRuntime.name);
        try {
            launcher_runtime_version = Tools.read(assetManager.open(internalRuntime.path+"/version"));
        }catch (IOException exc) {
            
            
            
            return installed_runtime_version != null;
        }
        
        if(!launcher_runtime_version.equals(installed_runtime_version))
            return unpackInternalRuntime(assetManager, internalRuntime, launcher_runtime_version);
        else return true;
    }

    private static boolean unpackInternalRuntime(AssetManager assetManager, InternalRuntime internalRuntime, String version) {
        try {
            MultiRTUtils.installRuntimeNamedBinpack(
                    assetManager.open(internalRuntime.path+"/universal.tar.xz"),
                    assetManager.open(internalRuntime.path+"/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    internalRuntime.name, version);
            MultiRTUtils.postPrepare(internalRuntime.name);
            return true;
        }catch (IOException e) {
            Log.e("NewJREAuto", "Internal JRE unpack failed", e);
            return false;
        }
    }

    private static InternalRuntime getInternalRuntime(Runtime runtime) {
        for(InternalRuntime internalRuntime : InternalRuntime.values()) {
            if(internalRuntime.name.equals(runtime.name)) return internalRuntime;
        }
        return null;
    }

    private static MathUtils.RankedValue<Runtime> getNearestInstalledRuntime(int targetVersion) {
        List<Runtime> runtimes = MultiRTUtils.getInstalledRuntimes();
        return MathUtils.findNearestPositive(targetVersion, runtimes, (runtime)->runtime.javaVersion);
    }

    private static MathUtils.RankedValue<InternalRuntime> getNearestInternalRuntime(int targetVersion) {
        List<InternalRuntime> runtimeList = Arrays.asList(InternalRuntime.values());
        return MathUtils.findNearestPositive(targetVersion, runtimeList, (runtime)->runtime.majorVersion);
    }


    
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        
        if (versionInfo.javaVersion == null || versionInfo.javaVersion.component.equalsIgnoreCase("jre-legacy"))
            return true;

        int gameRequiredVersion = versionInfo.javaVersion.majorVersion;

        LauncherProfiles.load();
        AssetManager assetManager = activity.getAssets();
        MinecraftProfile minecraftProfile = LauncherProfiles.getCurrentProfile();
        String profileRuntime = Tools.getSelectedRuntime(minecraftProfile);
        Runtime runtime = MultiRTUtils.read(profileRuntime);
        
        if (runtime.javaVersion >= gameRequiredVersion) {
            
            InternalRuntime internalRuntime = getInternalRuntime(runtime);
            
            if (internalRuntime != null) {
                
                return checkInternalRuntime(assetManager, internalRuntime);
            }
            return true;
        }

        
        
        MathUtils.RankedValue<?> nearestInstalledRuntime = getNearestInstalledRuntime(gameRequiredVersion);
        MathUtils.RankedValue<?> nearestInternalRuntime = getNearestInternalRuntime(gameRequiredVersion);

        MathUtils.RankedValue<?> selectedRankedRuntime = MathUtils.objectMin(
                nearestInternalRuntime, nearestInstalledRuntime, (value)->value.rank
        );

        
        
        if (selectedRankedRuntime.value instanceof InternalRuntime)
            if (!checkInternalRuntime(assetManager, (InternalRuntime) selectedRankedRuntime.value)) {
                if (nearestInstalledRuntime == null) 
                    tryDownloadRuntime(activity, gameRequiredVersion);
                
                
                selectedRankedRuntime = getNearestInstalledRuntime(gameRequiredVersion);
            }


        
        if(selectedRankedRuntime == null) {
            showRuntimeFail(activity, versionInfo);
            return false;
        }

        Object selected = selectedRankedRuntime.value;
        String appropriateRuntime;
        InternalRuntime internalRuntime;

        
        if(selected instanceof Runtime) {
            
            
            Runtime selectedRuntime = (Runtime) selected;
            appropriateRuntime = selectedRuntime.name;
            internalRuntime = getInternalRuntime(selectedRuntime);
        } else if (selected instanceof InternalRuntime) {
            
            internalRuntime = (InternalRuntime) selected;
            appropriateRuntime = internalRuntime.name;
        } else {
            throw new RuntimeException("Unexpected type of selected: "+selected.getClass().getName());
        }

        
        if(internalRuntime != null && !checkInternalRuntime(assetManager, internalRuntime)) {
            
            return false;
        }

        minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
        LauncherProfiles.write();
        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompatiblert, verInfo.javaVersion.majorVersion));
    }

    public static boolean isJavaVersionAvailableForDownload(int version) {
        for (ExternalRuntime javaVersion : ExternalRuntime.values()) {
            if (javaVersion.majorVersion == version) {
                return true;
            }
        }
        return false;
    }

    private static String getJreSource(int javaVersion, String arch){
        return String.format("https://github.com/AngelAuraMC/angelauramc-openjdk-build/releases/download/download_jre%1$s/jre%1$s-android-%2$s.tar.xz", javaVersion, arch);
    }
    
    private static void tryDownloadRuntime(Context activity, int javaVersion){
        if (!isOnline(activity)) throw new RuntimeException(activity.getString(R.string.multirt_no_internet));
        String arch = archAsString(getDeviceArchitecture());
        
        if (!isJavaVersionAvailableForDownload(javaVersion)) throw new RuntimeException("This is not an available JRE version");
        if ((getDeviceArchitecture() == Architecture.ARCH_X86 && javaVersion >= 21)) throw new RuntimeException("x86 is not supported on Java"+javaVersion);
        try {
            File outputFile = new File(Tools.DIR_CACHE, String.format("jre%s-android-%s.tar.xz", javaVersion, arch));
            DownloaderProgressWrapper monitor = new DownloaderProgressWrapper(R.string.newdl_downloading_jre_runtime,
                    ProgressLayout.UNPACK_RUNTIME);
            monitor.extraString = Integer.toString(javaVersion);
            DownloadUtils.downloadFileMonitored(
                    getJreSource(javaVersion, arch),
                    outputFile,
                    null,
                    monitor
            );
            String jreName = "External-" + javaVersion;
            MultiRTUtils.installRuntimeNamed(NATIVE_LIB_DIR, new FileInputStream(outputFile), jreName);
            MultiRTUtils.postPrepare(jreName);
            outputFile.delete();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download Java "+javaVersion+" for "+arch, e);
        }
    }

    private enum InternalRuntime {
        JRE_17(17, "Internal-17", "components/jre-new"),
        JRE_21(21, "Internal-21", "components/jre-21"),
        JRE_25(25, "Internal-25", "components/jre-25");
        public final int majorVersion;
        public final String name;
        public final String path;
        InternalRuntime(int majorVersion, String name, String path) {
            this.majorVersion = majorVersion;
            this.name = name;
            this.path = path;
        }
    }

    public enum ExternalRuntime {
        JRE_8(8, "External-8"),
        JRE_17(17, "External-17"),
        JRE_21(21, "External-21"),
        JRE_25(25, "External-25");
        public final int majorVersion;
        public final String name;
        public final String downloadLink;
        public boolean isDownloading = false;

        ExternalRuntime(int majorVersion, String name) {
            this.majorVersion = majorVersion;
            this.name = name;
            this.downloadLink = getJreSource(majorVersion, archAsString(getDeviceArchitecture()));
        }
        public void downloadRuntime(Context activity){
            tryDownloadRuntime(activity, majorVersion);
        }
    }

}