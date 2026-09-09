package net.kdt.pojavlaunch;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;
import static net.kdt.pojavlaunch.Architecture.archAsStringAndroid;
import static net.kdt.pojavlaunch.Architecture.getDeviceArchitecture;
import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;
import static net.kdt.pojavlaunch.PojavProfile.getAllProfiles;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_IGNORE_NOTCH;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_NOTCH_SIZE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.lifecycle.ContextExecutorTask;
import net.kdt.pojavlaunch.lifecycle.LifecycleAwareAlertDialog;
import net.kdt.pojavlaunch.memory.MemoryHoleFinder;
import net.kdt.pojavlaunch.memory.SelfMapsParser;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.plugins.FFmpegPlugin;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.tasks.AsyncAssetManager;
import net.kdt.pojavlaunch.utils.DateUtils;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.GLInfoUtils;
import net.kdt.pojavlaunch.utils.JREUtils;
import net.kdt.pojavlaunch.utils.JSONUtils;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.utils.OldVersionsUtils;
import net.kdt.pojavlaunch.value.DependentLibrary;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import net.kdt.pojavlaunch.value.MinecraftLibraryArtifact;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.libsdl.app.SDLControllerManager;
import org.lwjgl.glfw.CallbackBridge;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("IOStreamConstructor")
public final class Tools {
    public  static final float BYTE_TO_MB = 1024 * 1024;
    public static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    public static String APP_NAME = "Kollegen";

    public static final Gson GLOBAL_GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String URL_HOME = "https://kollegen.dev";
    public static String NATIVE_LIB_DIR;
    public static String DIR_DATA; 
    public static File DIR_CACHE;
    public static String MULTIRT_HOME;
    public static String LOCAL_RENDERER = null;
    public static int DEVICE_ARCHITECTURE;
    public static final String LAUNCHERPROFILES_RTPREFIX = "kollegen://";

    
    public static String DIR_ACCOUNT_NEW;
    public static String DIR_GAME_HOME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/games/Kollegen";
    public static String DIR_GAME_NEW;
    public static String GAME_PROFILES_FILE;

    
    public static String DIRNAME_HOME_JRE = "lib";

    
    public static String DIR_HOME_VERSION;
    public static String DIR_HOME_LIBRARY;

    public static String DIR_HOME_CRASH;

    public static String ASSETS_PATH;
    public static String OBSOLETE_RESOURCES_PATH;
    public static String CTRLMAP_PATH;
    public static String CTRLDEF_FILE;
    private static RenderersList sCompatibleRenderers;
    public static boolean useSFPEW = true;
    public static boolean useANGLE = false;
    public static int iLwjglVersion = 0;
    public static String sLwjglVersion = null;
    public static String lwjglNativesDir = null;


    private static File getPojavStorageRoot(Context ctx) {
        if(SDK_INT >= 29) {
            return ctx.getExternalFilesDir(null);
        }else{
            return new File(Environment.getExternalStorageDirectory(),"games/Kollegen");
        }
    }

    
    public static boolean checkStorageRoot(Context context) {
        File externalFilesDir = DIR_GAME_HOME  == null ? Tools.getPojavStorageRoot(context) : new File(DIR_GAME_HOME);
        
        return externalFilesDir != null && Environment.getExternalStorageState(externalFilesDir).equals(Environment.MEDIA_MOUNTED);
    }

    
    public static boolean checkStorageInteractive(Activity context) {
        if(!Tools.checkStorageRoot(context)) {
            context.startActivity(new Intent(context, MissingStorageActivity.class));
            context.finish();
            return false;
        }
        return true;
    }

    
    public static void initEarlyConstants(Context ctx) {
        DIR_CACHE = ctx.getCacheDir();
        DIR_DATA = ctx.getFilesDir().getParent();
        MULTIRT_HOME = DIR_DATA + "/runtimes";
        DIR_ACCOUNT_NEW = DIR_DATA + "/accounts";
        NATIVE_LIB_DIR = ctx.getApplicationInfo().nativeLibraryDir;
    }

    
    public static void initStorageConstants(Context ctx){
        initEarlyConstants(ctx);
        DIR_GAME_HOME = getPojavStorageRoot(ctx).getAbsolutePath();
        DIR_GAME_NEW = DIR_GAME_HOME + "/.minecraft";
        DIR_HOME_VERSION = DIR_GAME_NEW + "/versions";
        DIR_HOME_LIBRARY = DIR_GAME_NEW + "/libraries";
        DIR_HOME_CRASH = DIR_GAME_NEW + "/crash-reports";
        ASSETS_PATH = DIR_GAME_NEW + "/assets";
        OBSOLETE_RESOURCES_PATH = DIR_GAME_NEW + "/resources";
        CTRLMAP_PATH = DIR_GAME_HOME + "/controlmap";
        CTRLDEF_FILE = DIR_GAME_HOME + "/controlmap/default.json";
        GAME_PROFILES_FILE = Tools.DIR_GAME_NEW + "/launcher_profiles.json";
        switchDemo(isDemoProfile(ctx));
    }

    @SuppressLint("PrivateApi")
    private static String systemPropertiesGet(String systemProperty) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class<?> cSystemProperties = Class.forName("android.os.SystemProperties");
        Method get = cSystemProperties.getMethod("get", String.class);
        return (String) get.invoke(null, systemProperty);
    }

    private static boolean isAdreno740(){
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("/sys/class/kgsl/kgsl-3d0/gpu_model")
            );
            String gpuRenderer = br.readLine();
            return gpuRenderer != null &&
                    gpuRenderer.toLowerCase().contains("adreno") &&
                    gpuRenderer.contains("740");
        } catch (IOException e) {
            
            return false;
        }
    }

    
    public static boolean shouldUseUBWC() {
        try {
            boolean isSamsung = Build.MANUFACTURER.equalsIgnoreCase("samsung");
            boolean isOneUI = !systemPropertiesGet("ro.build.version.oneui").isBlank();
            return isOneUI && isSamsung && isAdreno740();
        } catch (Exception e) {
            return false;
        }
    }


    
    @NonNull
    private static File getGameDir() {
        return getGameDirPath(LauncherProfiles.getCurrentProfile());
    }

    
    public static boolean hasMods(String... filenames) {
        return !getMods(filenames).isEmpty();
    }

    
    public static List<File> getMods(String... filenames) {
        File gameDir = getGameDir();
        File modsDir = new File(gameDir, "mods");
        File[] modFiles = modsDir.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".jar"));
        if (modFiles == null) return new ArrayList<>();
        List<File> foundModFiles = new ArrayList<>();
        for (File file : modFiles) {
            for (String filename : filenames)
                if (file.getName().toLowerCase().contains(filename.toLowerCase()) &&
                        file.getName().toLowerCase().endsWith(".jar")) {
                    foundModFiles.add(file);
                    break;
                }
        }
        return foundModFiles;
    }

    
    public static void deleteSodiumMods() {
        File modsDir = new File(getGameDir(), "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if(mods == null) ;
        for(File file : mods) {
            String name = file.getName().toLowerCase();
            if(name.contains("sodium") ||
                    name.contains("beddium")    || 
                    name.contains("rubidium")   ||
                    name.contains("xenon")      || 
                    name.contains("celeritas")  ||
                    name.contains("relictium")  ||
                    name.contains("vintagium")  ||
                    name.contains("podium")     ||
                    name.contains("indium")     ||
                    name.contains("lazurite")   ||
                    name.contains("iris")       ||
                    name.contains("monocle")    ||
                    name.contains("voxy")       ||
                    name.contains("nvidium")    ||
                    name.contains("chloride")   ||
                    name.contains("bedrodium")  ||
                    name.contains("substrate")  || 
                    name.contains("blendium")   ||
                    name.contains("ryoamium")
                
                
            ) if(!file.delete())
                throw new RuntimeException("Failed to delete Sodium and related mods!");
        }
    }

    
    public static boolean hasTouchController(File gameDir) {
        File modsDir = new File(gameDir, "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if (mods == null) {
            return false;
        }
        for (File file : mods) {
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.contains("touchcontroller")) {
                return true;
            }
        }
        return false;
    }

    
    private static boolean affectedByRenderDistanceIssue() {
        GLInfoUtils.GLInfo info = GLInfoUtils.getGlInfo();
        return info.isAdreno() && info.glesMajorVersion >= 3;
    }

    private static String[] sodiumMods = {"sodium", "embeddium", "rubidium", "xenon"};

    private static boolean affectedByLTWRenderDistanceIssue() {
        if(!"opengles3_ltw".equals(Tools.LOCAL_RENDERER)) return false;
        if(!affectedByRenderDistanceIssue()) return false;
        if(hasMods(sodiumMods)) return false;

        int renderDistance;
        try {
            MCOptionUtils.load();
            String renderDistanceString = MCOptionUtils.get("renderDistance");
            renderDistance = Integer.parseInt(renderDistanceString);
        }catch (Exception e) {
            Log.e("Tools", "Failed to check render distance", e);
            renderDistance = 12; 
        }
        
        
        return renderDistance > 7;
    }

    public static void launchMinecraft(final AppCompatActivity activity, MinecraftAccount minecraftAccount,
                                       MinecraftProfile minecraftProfile, String versionId, int versionJavaRequirement) throws Throwable {
        int freeDeviceMemory = getFreeDeviceMemory(activity);
        int localeString;
        int freeAddressSpace = Architecture.is32BitsDevice() ? getMaxContinuousAddressSpaceSize() : -1;
        Log.i("MemStat", "Free RAM: " + freeDeviceMemory + " Addressable: " + freeAddressSpace);
        if(freeDeviceMemory > freeAddressSpace && freeAddressSpace != -1) {
            freeDeviceMemory = freeAddressSpace;
            localeString = R.string.address_memory_warning_msg;
        } else {
            localeString = R.string.memory_warning_msg;
        }

        if(LauncherPreferences.PREF_RAM_ALLOCATION > freeDeviceMemory) {
            int finalDeviceMemory = freeDeviceMemory;
            LifecycleAwareAlertDialog.DialogCreator dialogCreator = (dialog, builder) ->
                builder.setMessage(activity.getString(localeString, finalDeviceMemory, LauncherPreferences.PREF_RAM_ALLOCATION))
                        .setPositiveButton(android.R.string.ok, (d, w)->{});

            if(LifecycleAwareAlertDialog.haltOnDialog(activity.getLifecycle(), activity, dialogCreator)) {
                return; 
                
                
            }
        }
        LauncherProfiles.load();
        File gamedir = Tools.getGameDirPath(minecraftProfile);
        startControllableMitigation(activity, gamedir);
        startOldLegacy4JMitigation(activity, gamedir);
        if(affectedByLTWRenderDistanceIssue()) {
            LifecycleAwareAlertDialog.DialogCreator dialogCreator = ((alertDialog, dialogBuilder) ->
                    dialogBuilder.setMessage(activity.getString(R.string.ltw_render_distance_warning_msg))
                            .setPositiveButton(android.R.string.ok, (d, w)->{}));
            if(LifecycleAwareAlertDialog.haltOnDialog(activity.getLifecycle(), activity, dialogCreator)) {
                return;
            }
            
            try {
                MCOptionUtils.set("renderDistance", "7");
                MCOptionUtils.save();
            }catch (Exception e) {
                Log.e("Tools", "Failed to fix render distance setting", e);
            }
        }


        Runtime runtime = MultiRTUtils.forceReread(Tools.pickRuntime(minecraftProfile, versionJavaRequirement));
        JMinecraftVersionList.Version versionInfo = Tools.getVersionInfo(versionId);


        
        disableSplash(gamedir);
        String[] launchArgs = getMinecraftClientArgs(minecraftAccount, versionInfo, gamedir);

        
        OldVersionsUtils.selectOpenGlVersion(versionInfo);


        String launchClasspath = generateLaunchClasspath(versionInfo, versionId);

        List<String> javaArgList = new ArrayList<>();

        getCacioJavaArgs(javaArgList, runtime.javaVersion == 8, activity);

        if (versionInfo.logging != null) {
            String configFile = Tools.DIR_DATA + "/security/" + versionInfo.logging.client.file.id.replace("client", "log4j-rce-patch");
            if (!new File(configFile).exists()) {
                configFile = Tools.DIR_GAME_NEW + "/" + versionInfo.logging.client.file.id;
            }
            javaArgList.add("-Dlog4j.configurationFile=" + configFile);
        }

        File versionSpecificNativesDir = new File(Tools.DIR_CACHE, "natives/"+versionId);
        StringBuilder javaLibraryPath = new StringBuilder();

        
        javaLibraryPath.append(lwjglNativesDir).append(":");

        
        javaLibraryPath.append(Tools.NATIVE_LIB_DIR).append(":");
        if(versionSpecificNativesDir.exists()) {
            String dirPath = versionSpecificNativesDir.getAbsolutePath();
            javaLibraryPath.append(dirPath).append(":");
            javaArgList.add("-Djna.boot.library.path="+dirPath);
        }
        javaArgList.add("-Djava.library.path="+javaLibraryPath);

        javaArgList.addAll(Arrays.asList(getMinecraftJVMArgs(versionId, gamedir)));
        javaArgList.add("-cp"); javaArgList.add(launchClasspath);

        
        
        javaArgList.add("-Dorg.lwjgl.librarypath="+lwjglNativesDir);

        
        
        
        
        javaArgList.add("-Dfml.ignoreInvalidMinecraftCertificates=true");

        
        
        javaArgList.add("-Dimgui.library.name=imgui-java");
        
        javaArgList.add("-DZstdNativePath="+Tools.NATIVE_LIB_DIR+"/libzstd-jni-1.5.7-6-dhcompat.so");
        
        boolean hasSodiumMod = false;
        for (String modName : sodiumMods) {
            if (hasMods(sodiumMods)) {
                hasSodiumMod = true;
                File mixinPropertiesConfigFile = new File(getGameDir(), "config/" + modName + "-mixins.properties");
                
                String[] propertiesToAdd = {
                        "mixin.features.buffer_builder.intrinsics=false",
                        "mixin.features.chunk_rendering=false"
                };
                List<String> mixinPropertiesConfigStrings = null;
                try {
                    mixinPropertiesConfigStrings = org.apache.commons.io.FileUtils.readLines(mixinPropertiesConfigFile, "UTF-8");
                } catch (IOException ignored) {}
                if (mixinPropertiesConfigStrings == null) {
                    mixinPropertiesConfigStrings = new ArrayList<>();
                }
                for (String newLine : propertiesToAdd) {
                    if (!mixinPropertiesConfigStrings.contains(newLine)) {
                        mixinPropertiesConfigStrings.add(newLine);
                    }
                }
                try {
                    org.apache.commons.io.FileUtils.writeLines(mixinPropertiesConfigFile, mixinPropertiesConfigStrings);
                } catch (IOException ignored) {} 

            }
        }
        
        if (hasSodiumMod) javaArgList.add("-Dsodium.checks.issue2561=false");
        javaArgList.add(versionInfo.mainClass);
        javaArgList.addAll(Arrays.asList(launchArgs));
        
        String args = LauncherPreferences.PREF_CUSTOM_JAVA_ARGS;
        if(Tools.isValidString(minecraftProfile.javaArgs)) args = minecraftProfile.javaArgs;
        FFmpegPlugin.discover(activity);
        JREUtils.launchJavaVM(activity, runtime, gamedir, javaArgList, args);
        
        
    }
    private static Logger.eventLogListener controllableMitigationLogListener;
    
    private static void startControllableMitigation(Activity activity ,File gamedir) {
        String TAG = "ControllableMitigation";
        File deleted = new File(gamedir + "/controllable_natives/SDL");
        boolean hasControllable = false;
        File modsDir = new File(gamedir, "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if (mods != null) {
            for (File file : mods) {
                String name = file.getName();
                if (name.contains("controllable")) {
                    hasControllable = true;
                    break;
                }
            }
        }
        if (hasControllable) {
            Tools.runOnUiThread(() -> {
                Tools.dialog(activity, activity.getString(R.string.global_warning), activity.getString(R.string.controllableFound));
            });
            Thread mitigationThread = new Thread(() -> {
                
                Log.i(TAG, "Controllable detected! Starting mitigation thread");
                try {org.apache.commons.io.FileUtils.deleteDirectory(deleted);} catch (IOException ignored) {}
                while (!Thread.currentThread().isInterrupted()) {
                    
                    
                    
                    if (deleted.isDirectory()) {
                        if (deleted.listFiles().length > 0) {
                            if (deleted.listFiles()[0].listFiles().length > 0) {
                                if (deleted.listFiles()[0].listFiles()[0].exists()) {
                                    deleted.listFiles()[0].listFiles()[0].delete();
                                    break;
                                }
                            }
                        }
                    }
                }
                
                
                Log.i(TAG, "Success! Ending Controllable crash mitigation..");
            });
            mitigationThread.start();
            controllableMitigationLogListener = loggedLine -> {
                
                if (loggedLine.contains("Sound engine started") && mitigationThread.isAlive()) {
                    Log.i(TAG, "Nothing happened. Ending Controllable crash mitigation..");
                    Logger.removeLogListener(controllableMitigationLogListener);
                    mitigationThread.interrupt();
                }
            };
            Logger.addLogListener(controllableMitigationLogListener);
        }
    }

    private static Logger.eventLogListener oldL4JMitigationLogListener;
    
    
    private static void startOldLegacy4JMitigation(Activity activity, File gamedir) {
        boolean hasLegacy4J = false;
        File modsDir = new File(gamedir, "mods");
        File[] mods = modsDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if(mods != null) {
            for (File file : mods) {
                String name = file.getName();
                if (name.contains("Legacy4J")) {
                    hasLegacy4J = true;
                    break;
                }
            }
        }
        if (hasLegacy4J) {
            String TAG = "OldLegacy4JMitigation";
            Log.i(TAG, "Legacy4J detected!");
            oldL4JMitigationLogListener = loggedLine -> {
                if (loggedLine.contains("literal{SDL3 (isXander's libsdl4j)} isn't supported in this system. GLFW will be used instead.")) {
                    Logger.appendToLog("Kollegen-Android: Broken version of Legacy4J (below 1.8.51.8.5.2537.1) detected! Force enabling SDL");
                    Tools.SDL.initializeControllerSubsystems();
                    Tools.runOnUiThread(() -> {
                        Tools.dialog(activity, activity.getString(R.string.global_warning), activity.getString(R.string.oldL4JFound));
                    });
                    Logger.removeLogListener(oldL4JMitigationLogListener);
                } else if (loggedLine.contains("Added SDL Controller Mappings")) {
                    Logger.appendToLog("Kollegen-Android: Fixed version of Legacy4J (1.8.5.2537.1 or higher) detected! Have fun!");
                    Logger.removeLogListener(oldL4JMitigationLogListener);
                }
            };
            Logger.addLogListener(oldL4JMitigationLogListener);
        }
    }

    public static File getGameDirPath(@NonNull MinecraftProfile minecraftProfile){
        if(minecraftProfile.gameDir != null){
            if(minecraftProfile.gameDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
                return new File(minecraftProfile.gameDir.replace(Tools.LAUNCHERPROFILES_RTPREFIX,Tools.DIR_GAME_HOME+"/"));
            else
                return new File(Tools.DIR_GAME_HOME,minecraftProfile.gameDir);
        }
        return new File(Tools.DIR_GAME_NEW);
    }

    public static void buildNotificationChannel(Context context){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
                context.getString(R.string.notif_channel_id),
                context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.createNotificationChannel(channel);
    }
    public static void disableSplash(File dir) {
        File configDir = new File(dir, "config");
        if(FileUtils.ensureDirectorySilently(configDir)) {
            File forgeSplashFile = new File(dir, "config/splash.properties");
            String forgeSplashContent = "enabled=true";
            try {
                if (forgeSplashFile.exists()) {
                    forgeSplashContent = Tools.read(forgeSplashFile.getAbsolutePath());
                }
                if (forgeSplashContent.contains("enabled=true")) {
                    Tools.write(forgeSplashFile.getAbsolutePath(),
                            forgeSplashContent.replace("enabled=true", "enabled=false"));
                }
            } catch (IOException e) {
                Log.w(Tools.APP_NAME, "Could not disable Forge 1.12.2 and below splash screen!", e);
            }
        } else {
            Log.w(Tools.APP_NAME, "Failed to create the configuration directory");
        }
    }

    public static void getCacioJavaArgs(List<String> javaArgList, boolean isJava8, Activity activity) {
        
        javaArgList.add("-Djava.awt.headless=false");
        javaArgList.add("-Dcacio.managed.screensize=" + AWTCanvasView.AWT_CANVAS_WIDTH + "x" + AWTCanvasView.AWT_CANVAS_HEIGHT);
        javaArgList.add("-Dcacio.font.fontmanager=sun.awt.X11FontManager");
        javaArgList.add("-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler");
        javaArgList.add("-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel");
        if (isJava8) {
            javaArgList.add("-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit");
            javaArgList.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment");
        } else {
            File caciocavavallo17Dir = new File(Tools.DIR_GAME_HOME, "caciocavallo17");
            File[] caciocavallo17Jars = caciocavavallo17Dir.listFiles((f, s) ->s.contains("cacio-tta"));
            if(caciocavallo17Jars == null || caciocavallo17Jars.length < 1) {
            
                AsyncAssetManager.unpackComponents(activity);
                caciocavallo17Jars = caciocavavallo17Dir.listFiles((f, s) ->s.contains("cacio-tta"));
                if(caciocavallo17Jars == null || caciocavallo17Jars.length < 1)
                    throw new RuntimeException("Failed to extract required assets!");
            }
            javaArgList.add("-javaagent:"+caciocavallo17Jars[0].getAbsolutePath());
            javaArgList.add("-Dawt.toolkit=com.github.caciocavallosilano.cacio.ctc.CTCToolkit");
            javaArgList.add("-Djava.awt.graphicsenv=com.github.caciocavallosilano.cacio.ctc.CTCGraphicsEnvironment");
            

            javaArgList.add("--add-exports=java.desktop/java.awt=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.awt.event=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED");
            javaArgList.add("--add-exports=java.base/sun.security.action=ALL-UNNAMED");
            javaArgList.add("--add-opens=java.base/java.util=ALL-UNNAMED");
            javaArgList.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED");
            javaArgList.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED");
            javaArgList.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED");
            javaArgList.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");

            
            javaArgList.add("--add-opens=java.base/java.net=ALL-UNNAMED");
        }

        StringBuilder cacioClasspath = new StringBuilder();
        cacioClasspath.append("-Xbootclasspath/").append(isJava8 ? "p" : "a");
        File cacioDir = new File(DIR_GAME_HOME + "/caciocavallo" + (isJava8 ? "" : "17"));
        File[] cacioFiles = cacioDir.listFiles();
        if (cacioFiles != null) {
            for (File file : cacioFiles) {
                if (file.getName().endsWith(".jar")) {
                    cacioClasspath.append(":").append(file.getAbsolutePath());
                }
            }
        }
        javaArgList.add(cacioClasspath.toString());
    }

    public static String[] getMinecraftJVMArgs(String versionName, File gameDir) {
        JMinecraftVersionList.Version versionInfo = Tools.getVersionInfo(versionName, true);
        if (versionInfo.arguments == null || versionInfo.arguments.jvm == null)
            return new String[0];

        Map<String, String> varArgMap = new ArrayMap<>();
        varArgMap.put("classpath_separator", ":");
        varArgMap.put("library_directory", DIR_HOME_LIBRARY);
        varArgMap.put("version_name", versionInfo.id);
        varArgMap.put("natives_directory", Tools.DIR_CACHE.getAbsolutePath());

        List<String> minecraftArgs = new ArrayList<>();
        for (Object arg : versionInfo.arguments.jvm) {
            if (arg instanceof String) {
                
                if (((String) arg).contains("java.library.path")) {
                    continue;
                }
                if (arg.equals("-cp")) {
                    continue;
                }
                if (arg.equals("${classpath}")){
                    continue;
                }
                
                
                
                
                
                
                if (((String) arg).startsWith("-DignoreList=")){
                    minecraftArgs.add(arg+",${version_name}.jar");
                    continue;
                }

                
                if (((String) arg).contains("minecraft.launcher.brand") ||
                    ((String) arg).contains("minecraft.launcher.version")) {
                    continue;
                }

                minecraftArgs.add((String) arg);
            } 
        }
        return JSONUtils.insertJSONValueList(minecraftArgs.toArray(new String[0]), varArgMap);
    }

    public static String[] getMinecraftClientArgs(MinecraftAccount profile, JMinecraftVersionList.Version versionInfo, File gameDir) {
        String username = profile.username.replace("Demo.", "");
        String versionName = versionInfo.id;
        if (versionInfo.inheritsFrom != null) {
            versionName = versionInfo.inheritsFrom;
        }

        String userType = "mojang";
        try {
            Date creationDate = DateUtils.getOriginalReleaseDate(versionInfo);
            
            
            
            if(creationDate != null && !DateUtils.dateBefore(creationDate, 2022, 9, 26)) {
                userType = "msa";
            }
        }catch (ParseException e) {
            Log.e("CheckForProfileKey", "Failed to determine profile creation date, using \"mojang\"", e);
        }


        Map<String, String> varArgMap = new ArrayMap<>();
        varArgMap.put("auth_session", profile.accessToken); 
        varArgMap.put("auth_access_token", profile.accessToken);
        varArgMap.put("auth_player_name", username);
        varArgMap.put("auth_uuid", profile.profileId.replace("-", ""));
        varArgMap.put("auth_xuid", profile.xuid);
        varArgMap.put("assets_root", Tools.ASSETS_PATH);
        varArgMap.put("assets_index_name", versionInfo.assets);
        varArgMap.put("game_assets", Tools.ASSETS_PATH);
        varArgMap.put("game_directory", gameDir.getAbsolutePath());
        varArgMap.put("user_properties", "{}");
        varArgMap.put("user_type", userType);
        varArgMap.put("version_name", versionName);
        varArgMap.put("version_type", versionInfo.type);

        List<String> minecraftArgs = new ArrayList<>();
        if (versionInfo.arguments != null) {
            
            for (Object arg : versionInfo.arguments.game) {
                if (arg instanceof String) {
                    minecraftArgs.add((String) arg);
                } 
            }
        }

        String mcArguments = versionInfo.minecraftArguments == null ?
                fromStringArray(minecraftArgs.toArray(new String[0])):
                versionInfo.minecraftArguments;

        if(profile.isDemo()) mcArguments += " --demo";

        return JSONUtils.insertJSONValueList(splitAndFilterEmpty(mcArguments), varArgMap);
    }

    public static String fromStringArray(String[] strArr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            if (i > 0) builder.append(" ");
            builder.append(strArr[i]);
        }

        return builder.toString();
    }

    private static String[] splitAndFilterEmpty(String argStr) {
        List<String> strList = new ArrayList<>();
        for (String arg : argStr.split(" ")) {
            if (!arg.isEmpty()) {
                strList.add(arg);
            }
        }
        
        return strList.toArray(new String[0]);
    }

    public static String artifactToPath(DependentLibrary library) {
        if (library.downloads != null &&
            library.downloads.artifact != null &&
            library.downloads.artifact.path != null)
            return library.downloads.artifact.path;
        String[] libInfos = library.name.split(":");
        return libInfos[0].replaceAll("\\.", "/") + "/" + libInfos[1] + "/" + libInfos[2] + "/" + libInfos[1] + "-" + libInfos[2] + (libInfos.length == 4 ? "-" + libInfos[3] : "") + ".jar";
    }

    private static String getLibClasspath(JMinecraftVersionList.Version info){
        StringBuilder libClasspath = new StringBuilder();
        String[] classpath = generateLibClasspath(info);
        for (String jarFile : classpath) {
            libClasspath.append(jarFile).append(":");
        }
        
        libClasspath.setLength(libClasspath.length() - 1);
        return libClasspath.toString();
    }

    public static String getClientClasspath(String version) {
        return DIR_HOME_VERSION + "/" + version + "/" + version + ".jar";
    }
    public static String generateLaunchClasspath(JMinecraftVersionList.Version info, String actualname) {
        StringBuilder launchClasspath = new StringBuilder(); 
        String libClasspath = getLibClasspath(info); 
        String internalLwjglVersion = iLwjglVersion >= 341 ? "3.4.1" : "3.3.3";
        File lwjgl3Folder = new File(Tools.DIR_GAME_HOME, "lwjgl3/"+internalLwjglVersion);
        String lwjglCore = lwjgl3Folder.getAbsolutePath() + "/lwjgl.jar";
        String lwjglMerged = lwjgl3Folder.getAbsolutePath() + "/lwjgl-"+internalLwjglVersion+"-merged-modules.jar";
        String lwjglxFile = lwjgl3Folder + "/lwjgl-lwjglx.jar";

        if (!new File(lwjglCore).exists() || !new File(lwjglMerged).exists() || !new File(lwjglxFile).exists()) {
            try { 
                if (lwjgl3Folder.exists())
                    org.apache.commons.io.FileUtils.deleteDirectory(lwjgl3Folder);
            } catch (IOException ignored) {}
            throw new RuntimeException("LWJGL jars incomplete, restart the app to reextract them.");
        }
        launchClasspath.append(lwjglCore).append(":");
        
        launchClasspath.append(lwjglMerged).append(":");

        File[] lwjglModules = lwjgl3Folder.listFiles(pathname ->
                pathname.getName().endsWith(".jar") &&
                
                !pathname.getName().equals("lwjgl.jar") &&
                !pathname.getName().equals("lwjgl-"+internalLwjglVersion+"-merged-modules.jar") &&
                !pathname.getName().endsWith("lwjglx.jar"));

        if (lwjglModules != null) {
            for (File lwjglModule : lwjglModules)
                launchClasspath.append(lwjglModule.getAbsolutePath()).append(":");
        } else Log.e("generateLaunchClasspath", "lwjgl modules are missing from components!");

        launchClasspath.append(libClasspath).append(":");
        launchClasspath.append(getClientClasspath(actualname));
        
        if (iLwjglVersion <= 299) launchClasspath.append(":").append(lwjglxFile);
        return launchClasspath.toString();
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        if(SDK_INT >= Build.VERSION_CODES.N && (activity.isInMultiWindowMode() || activity.isInPictureInPictureMode())){
            
            displayMetrics = activity.getResources().getDisplayMetrics();
        }else{
            if (SDK_INT >= Build.VERSION_CODES.R) {
                activity.getDisplay().getRealMetrics(displayMetrics);
            } else { 
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            }
            if(!PREF_IGNORE_NOTCH){
                
                if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    displayMetrics.heightPixels -= PREF_NOTCH_SIZE;
                else
                    displayMetrics.widthPixels -= PREF_NOTCH_SIZE;
            }
        }
        currentDisplayMetrics = displayMetrics;
        return displayMetrics;
    }

    public static void setFullscreen(Activity activity, boolean fullscreen) {
        final View decorView = activity.getWindow().getDecorView();
        View.OnSystemUiVisibilityChangeListener visibilityChangeListener = visibility -> {
            boolean multiWindowMode = SDK_INT >= 24 && activity.isInMultiWindowMode();
            
            
            if(fullscreen && !multiWindowMode){
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            }else{
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

        };
        decorView.setOnSystemUiVisibilityChangeListener(visibilityChangeListener);
        visibilityChangeListener.onSystemUiVisibilityChange(decorView.getSystemUiVisibility()); 
    }

    public static DisplayMetrics currentDisplayMetrics;

    public static void updateWindowSize(Activity activity) {
        currentDisplayMetrics = getDisplayMetrics(activity);

        View dimensionView = activity.findViewById(R.id.dimension_tracker);

        if(dimensionView != null) {
            int width = dimensionView.getWidth();
            int height = dimensionView.getHeight();
            if(width != 0 && height != 0) {
                Log.i("Tools", "Using dimension_tracker for display dimensions; W="+width+" H="+height);
                CallbackBridge.physicalWidth = width;
                CallbackBridge.physicalHeight = height;
                return;
            }else{
                Log.e("Tools","Dimension tracker detected but dimensions out of date. Please check usage.", new Exception());
            }
        }

        CallbackBridge.physicalWidth = currentDisplayMetrics.widthPixels;
        CallbackBridge.physicalHeight = currentDisplayMetrics.heightPixels;
    }

    public static float dpToPx(float dp) {
        
        return dp * currentDisplayMetrics.density;
    }

    public static float pxToDp(float px){
        
        return px / currentDisplayMetrics.density;
    }

    public static void copyAssetFile(Context ctx, String fileName, String output, boolean overwrite) throws IOException {
        copyAssetFile(ctx, fileName, output, new File(fileName).getName(), overwrite);
    }

    public static void copyAssetFile(Context ctx, String fileName, String output, String outputName, boolean overwrite) throws IOException {
        File parentFolder = new File(output);
        FileUtils.ensureDirectory(parentFolder);
        File destinationFile = new File(output, outputName);
        if(!destinationFile.exists() || overwrite){
            try(InputStream inputStream = ctx.getAssets().open(fileName)) {
                try (OutputStream outputStream = new FileOutputStream(destinationFile)){
                    IOUtils.copy(inputStream, outputStream);
                }
            }
        }
    }

    public static String printToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    public static void showError(Context ctx, Throwable e) {
        showError(ctx, e, false);
    }

    public static void showError(final Context ctx, final Throwable e, final boolean exitIfOk) {
        showError(ctx, R.string.global_error, null ,e, exitIfOk, false);
    }
    public static void showError(final Context ctx, final int rolledMessage, final Throwable e) {
        showError(ctx, R.string.global_error, ctx.getString(rolledMessage), e, false, false);
    }
    public static void showError(final Context ctx, final String rolledMessage, final Throwable e) {
        showError(ctx, R.string.global_error, rolledMessage, e, false, false);
    }
    public static void showError(final Context ctx, final String rolledMessage, final Throwable e, boolean exitIfOk) {
        showError(ctx, R.string.global_error, rolledMessage, e, exitIfOk, false);
    }
    public static void showError(final Context ctx, final int titleId, final Throwable e, final boolean exitIfOk) {
        showError(ctx, titleId, null, e, exitIfOk, false);
    }

    private static void showError(final Context ctx, final int titleId, final String rolledMessage, final Throwable e, final boolean exitIfOk, final boolean showMore) {
        if(e instanceof ContextExecutorTask) {
            ContextExecutor.execute((ContextExecutorTask) e);
            return;
        }
        e.printStackTrace();

        Runnable runnable = () -> {
            final String errMsg = showMore ? printToString(e) : rolledMessage != null ? rolledMessage : e.getMessage();
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                    .setTitle(titleId)
                    .setMessage(errMsg)
                    .setPositiveButton(android.R.string.ok, (p1, p2) -> {
                        if(exitIfOk) {
                            if (ctx instanceof MainActivity) {
                                fullyExit();
                            } else if (ctx instanceof Activity) {
                                ((Activity) ctx).finish();
                            }
                        }
                    })
                    .setNegativeButton(showMore ? R.string.error_show_less : R.string.error_show_more, (p1, p2) -> showError(ctx, titleId, rolledMessage, e, exitIfOk, !showMore))
                    .setNeutralButton(android.R.string.copy, (p1, p2) -> {
                        ClipboardManager mgr = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                        mgr.setPrimaryClip(ClipData.newPlainText("error", printToString(e)));
                        if(exitIfOk) {
                            if (ctx instanceof MainActivity) {
                                fullyExit();
                            } else {
                                ((Activity) ctx).finish();
                            }
                        }
                    })
                    .setCancelable(!exitIfOk);
            try {
                builder.show();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        };

        if (ctx instanceof Activity) {
            ((Activity) ctx).runOnUiThread(runnable);
        } else {
            runnable.run();
        }
    }

    
    public static void showErrorRemote(Throwable e) {
        showErrorRemote(null, e);
    }
    public static void showErrorRemote(Context context, int rolledMessage, Throwable e) {
        showErrorRemote(context.getString(rolledMessage), e);
    }
    public static void showErrorRemote(String rolledMessage, Throwable e) {
        
        
        

        
        ContextExecutor.execute(new ShowErrorActivity.RemoteErrorTask(e, rolledMessage));
    }



    public static void dialogOnUiThread(final Activity activity, final CharSequence title, final CharSequence message) {
        activity.runOnUiThread(()->dialog(activity, title, message));
    }
    public static void dialogOnUiThread(final Activity activity, final int title, final int message) {
        dialogOnUiThread(activity, activity.getString(title), activity.getString(message));
    }

    public static void dialog(final Context context, final CharSequence title, final CharSequence message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static void openURL(Activity act, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        act.startActivity(browserIntent);
    }

    private static boolean checkRules(JMinecraftVersionList.Arguments.ArgValue.ArgRules[] rules) {
        if(rules == null) return true; 
        for (JMinecraftVersionList.Arguments.ArgValue.ArgRules rule : rules) {
            if (rule.action.equals("allow") && rule.os != null && rule.os.name.equals("osx")) {
                return false; 
            }
        }
        return true; 
    }

    public static void preProcessLibraries(DependentLibrary[] libraries) {
        for (int i = 0; i < libraries.length; i++) {
            DependentLibrary libItem = libraries[i];
            String[] version = libItem.name.split(":")[2].split("\\.");
            if (libItem.name.startsWith("net.java.dev.jna:jna:")) {
                
                
                if (Integer.parseInt(version[0]) >= 5 && Integer.parseInt(version[1]) >= 13) continue;
                Log.d(APP_NAME, "Library " + libItem.name + " has been changed to version 5.13.0");
                createLibraryInfo(libItem);
                libItem.name = "net.java.dev.jna:jna:5.13.0";
                libItem.downloads.artifact.path = "net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar";
                libItem.downloads.artifact.sha1 = "1200e7ebeedbe0d10062093f32925a912020e747";
                libItem.downloads.artifact.url = "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar";
            } else if (libItem.name.startsWith("com.github.oshi:oshi-core:")) {
                
                

                if (Integer.parseInt(version[0]) != 6 || Integer.parseInt(version[1]) != 2) continue;
                Log.d(APP_NAME, "Library " + libItem.name + " has been changed to version 6.3.0");
                createLibraryInfo(libItem);
                libItem.name = "com.github.oshi:oshi-core:6.3.0";
                libItem.downloads.artifact.path = "com/github/oshi/oshi-core/6.3.0/oshi-core-6.3.0.jar";
                libItem.downloads.artifact.sha1 = "9e98cf55be371cafdb9c70c35d04ec2a8c2b42ac";
                libItem.downloads.artifact.url = "https://repo1.maven.org/maven2/com/github/oshi/oshi-core/6.3.0/oshi-core-6.3.0.jar";
            } else if (libItem.name.startsWith("org.ow2.asm:asm-all:")) {
                
                
                
                if(Integer.parseInt(version[0]) >= 5) continue;
                Log.d(APP_NAME, "Library " + libItem.name + " has been changed to version 5.0.4");
                createLibraryInfo(libItem);
                libItem.name = "org.ow2.asm:asm-all:5.0.4";
                libItem.url = null;
                libItem.downloads.artifact.path = "org/ow2/asm/asm-all/5.0.4/asm-all-5.0.4.jar";
                libItem.downloads.artifact.sha1 = "e6244859997b3d4237a552669279780876228909";
                libItem.downloads.artifact.url = "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/5.0.4/asm-all-5.0.4.jar";
            }
        }
    }

    private static void createLibraryInfo(DependentLibrary library) {
        if(library.downloads == null || library.downloads.artifact == null)
            library.downloads = new DependentLibrary.LibraryDownloads(new MinecraftLibraryArtifact());
    }

    public static String[] generateLibClasspath(JMinecraftVersionList.Version info) {
        List<String> libDir = new ArrayList<>();
        for (DependentLibrary libItem: info.libraries) {
            
            int libItemVersionStringOffset = 0;
            if(libItem.name.startsWith("org.lwjgl.lwjgl:lwjgl:")) {
                libItemVersionStringOffset = "org.lwjgl.lwjgl:lwjgl:".length();
            } else if (libItem.name.startsWith("org.lwjgl:lwjgl:")) {
                libItemVersionStringOffset = "org.lwjgl:lwjgl:".length();
            }
            
            if (libItemVersionStringOffset != 0 && (iLwjglVersion < 200 || iLwjglVersion > 999)) {
                while (libItemVersionStringOffset < libItem.name.length()) {
                    char c = libItem.name.charAt(libItemVersionStringOffset);
                    if (c >= '0' && c <= '9') {
                        iLwjglVersion = iLwjglVersion * 10 + (c - '0');
                    } else if (c == '.') {
                        
                    } else {
                        break; 
                    }
                    libItemVersionStringOffset++;
                }
            }

            String libPath = Tools.DIR_HOME_LIBRARY + "/" + artifactToPath(libItem);
            if (!FileUtils.exists(libPath)) {
                Log.d(APP_NAME, "Ignored non-exists file: " + libPath);
                continue;
            }
            if(!checkRules(libItem.rules)) continue;
            libDir.add(libPath);
            
            if (libItem.name.startsWith("org.ow2.asm:asm") && !libItem.name.startsWith("org.ow2.asm:asm-all:")){
                libDir.remove(Tools.DIR_HOME_LIBRARY + "/" + artifactToPath(new DependentLibrary(){{
                    name = "org.ow2.asm:asm-all:5.0.4";
                }} ));
            }
        }
        
        if (iLwjglVersion < 200 || iLwjglVersion > 999) throw new RuntimeException("Unable to determine LWJGL version, JSON may be corrupt.");
        sLwjglVersion = iLwjglVersion >= 341 ? "3.4.1" : "3.3.3";
        lwjglNativesDir = String.format("%s/lwjgl-%s-natives/%s", Tools.DIR_DATA, sLwjglVersion, archAsStringAndroid(getDeviceArchitecture()));
        return libDir.toArray(new String[0]);
    }

    public static JMinecraftVersionList.Version getVersionInfo(String versionName) {
        return getVersionInfo(versionName, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static JMinecraftVersionList.Version getVersionInfo(String versionName, boolean skipInheriting) {
        try {
            JMinecraftVersionList.Version customVer = Tools.GLOBAL_GSON.fromJson(read(DIR_HOME_VERSION + "/" + versionName + "/" + versionName + ".json"), JMinecraftVersionList.Version.class);
            if (skipInheriting || customVer.inheritsFrom == null || customVer.inheritsFrom.equals(customVer.id)) {
                preProcessLibraries(customVer.libraries);
            } else {
                JMinecraftVersionList.Version inheritsVer;
                
                try{
                    inheritsVer = Tools.GLOBAL_GSON.fromJson(read(DIR_HOME_VERSION + "/" + customVer.inheritsFrom + "/" + customVer.inheritsFrom + ".json"), JMinecraftVersionList.Version.class);
                }catch(IOException e) {
                    throw new RuntimeException("Can't find the source version for "+ versionName +" (req version="+customVer.inheritsFrom+")");
                }
                
                insertSafety(inheritsVer, customVer,
                        "assetIndex", "assets", "id",
                        "mainClass", "minecraftArguments",
                        "releaseTime", "time", "type", "inheritsFrom"
                );

                
                List<DependentLibrary> inheritLibraryList = new ArrayList<>(Arrays.asList(inheritsVer.libraries));
                outer_loop:
                for(DependentLibrary library : customVer.libraries){
                    
                    String libName = library.name.substring(0, library.name.lastIndexOf(":"));

                    for(DependentLibrary inheritLibrary : inheritLibraryList) {
                        String inheritLibName = inheritLibrary.name.substring(0, inheritLibrary.name.lastIndexOf(":"));

                        if(libName.equals(inheritLibName)){
                            Log.d(APP_NAME, "Library " + libName + ": Replaced version " +
                                    libName.substring(libName.lastIndexOf(":") + 1) + " with " +
                                    inheritLibName.substring(inheritLibName.lastIndexOf(":") + 1));

                            
                            inheritLibraryList.remove(inheritLibrary);
                            continue outer_loop;
                        }
                    }
                }

                
                inheritLibraryList.addAll(Arrays.asList(customVer.libraries));
                inheritsVer.libraries = inheritLibraryList.toArray(new DependentLibrary[0]);
                preProcessLibraries(inheritsVer.libraries);


                
                if (inheritsVer.arguments != null && customVer.arguments != null) {
                    List totalArgList = new ArrayList(Arrays.asList(inheritsVer.arguments.game));

                    int nskip = 0;
                    for (int i = 0; i < customVer.arguments.game.length; i++) {
                        if (nskip > 0) {
                            nskip--;
                            continue;
                        }

                        Object perCustomArg = customVer.arguments.game[i];
                        if (perCustomArg instanceof String) {
                            String perCustomArgStr = (String) perCustomArg;
                            
                            if (perCustomArgStr.startsWith("--") && totalArgList.contains(perCustomArgStr)) {
                                perCustomArg = customVer.arguments.game[i + 1];
                                if (perCustomArg instanceof String) {
                                    perCustomArgStr = (String) perCustomArg;
                                    
                                    if (!perCustomArgStr.startsWith("--")) {
                                        nskip++;
                                    }
                                }
                            } else {
                                totalArgList.add(perCustomArgStr);
                            }
                        } else if (!totalArgList.contains(perCustomArg)) {
                            totalArgList.add(perCustomArg);
                        }
                    }

                    inheritsVer.arguments.game = totalArgList.toArray(new Object[0]);
                }

                customVer = inheritsVer;
            }

            
            if (customVer.javaVersion != null && customVer.javaVersion.majorVersion == 0) {
                customVer.javaVersion.majorVersion = customVer.javaVersion.version;
            }
            return customVer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    private static void insertSafety(JMinecraftVersionList.Version targetVer, JMinecraftVersionList.Version fromVer, String... keyArr) {
        for (String key : keyArr) {
            Object value = null;
            try {
                Field fieldA = fromVer.getClass().getDeclaredField(key);
                value = fieldA.get(fromVer);
                if (((value instanceof String) && !((String) value).isEmpty()) || value != null) {
                    Field fieldB = targetVer.getClass().getDeclaredField(key);
                    fieldB.set(targetVer, value);
                }
            } catch (Throwable th) {
                Log.w(Tools.APP_NAME, "Unable to insert " + key + "=" + value, th);
            }
        }
    }

    public static String read(InputStream is) throws IOException {
        String readResult = IOUtils.toString(is, StandardCharsets.UTF_8);
        is.close();
        return readResult;
    }

    public static String read(String path) throws IOException {
        return read(new FileInputStream(path));
    }

    public static String read(File path) throws IOException {
        return read(new FileInputStream(path));
    }

    public static void write(String path, String content) throws IOException {
        File file = new File(path);
        FileUtils.ensureParentDirectory(file);
        try(FileOutputStream outStream = new FileOutputStream(file)) {
            IOUtils.write(content, outStream);
        }
    }

    public static void downloadFile(String urlInput, String nameOutput) throws IOException {
        File file = new File(nameOutput);
        DownloadUtils.downloadFile(urlInput, file);
    }

    public static boolean isAndroid8OrHigher() {
        return SDK_INT >= 26;
    }

    public static void fullyExit() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void printLauncherInfo(String gameVersion, String javaArguments, int deviceRam) {
        Logger.appendToLog("Info: Launcher version: " + BuildConfig.VERSION_NAME);
        Logger.appendToLog("Info: Architecture: " + Architecture.archAsString(DEVICE_ARCHITECTURE));
        Logger.appendToLog("Info: Device model: " + Build.MANUFACTURER + " " +Build.MODEL);
        Logger.appendToLog(String.format("Info: Total RAM: %s MB", deviceRam != 0 ? deviceRam : "unavailable"));
        Logger.appendToLog("Info: Allocated RAM: " + LauncherPreferences.PREF_RAM_ALLOCATION + "MB");
        Logger.appendToLog("Info: API version: " + SDK_INT);
        Logger.appendToLog("Info: Selected Minecraft version: " + gameVersion);
        Logger.appendToLog("Info: Custom Java arguments: \"" + javaArguments + "\"");
        GLInfoUtils.GLInfo info = GLInfoUtils.getGlInfo();
        Logger.appendToLog("Info: Graphics device: "+info.vendor+ " "+info.renderer+" (OpenGL ES "+info.glesMajorVersion+")");
    }

    public interface DownloaderFeedback {
        void updateProgress(int curr, int max);
    }


    public static boolean compareSHA1(File f, String sourceSHA) {
        try {
            String sha1_dst;
            try (InputStream is = new FileInputStream(f)) {
                sha1_dst = new String(Hex.encodeHex(org.apache.commons.codec.digest.DigestUtils.sha1(is)));
            }
            if(sourceSHA != null) {
                return sha1_dst.equalsIgnoreCase(sourceSHA);
            } else{
                return true; 
            }
        }catch (IOException e) {
            Log.i("SHA1","Fake-matching a hash due to a read error",e);
            return true;
        }
    }

    public static void ignoreNotch(boolean shouldIgnore, Activity ctx){
        if (SDK_INT >= P) {
            if (shouldIgnore) {
                ctx.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                ctx.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            }
            ctx.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            Tools.updateWindowSize(ctx);
        }
    }

    public static int getTotalDeviceMemory(Context ctx){
        ActivityManager actManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return (int) (memInfo.totalMem / 1048576L);
    }

    public static int getFreeDeviceMemory(Context ctx){
        ActivityManager actManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return (int) (memInfo.availMem / 1048576L);
    }

    private static int internalGetMaxContinuousAddressSpaceSize() throws Exception{
        MemoryHoleFinder memoryHoleFinder = new MemoryHoleFinder();
        new SelfMapsParser(memoryHoleFinder).run();
        long largestHole = memoryHoleFinder.getLargestHole();
        if(largestHole == -1) return -1;
        else return (int)(largestHole / 1048576L);
    }

    public static int getMaxContinuousAddressSpaceSize() {
        try {
            return internalGetMaxContinuousAddressSpaceSize();
        }catch (Exception e){
            Log.w("Tools", "Failed to find the largest uninterrupted address space");
            return -1;
        }
    }

    public static int getDisplayFriendlyRes(int displaySideRes, float scaling){
        displaySideRes *= scaling;
        if(displaySideRes % 2 != 0) displaySideRes --;
        return displaySideRes;
    }

    public static String getFileName(Context ctx, Uri uri) {
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        if(c == null) return uri.getLastPathSegment(); 
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if(columnIndex == -1) return uri.getLastPathSegment();
        String fileName = c.getString(columnIndex);
        c.close();
        return fileName;
    }

    
    public static void swapFragment(FragmentActivity fragmentActivity , Class<? extends Fragment> fragmentClass,
                                    @Nullable String fragmentTag, @Nullable Bundle bundle) {
        
        
        fragmentActivity.getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(fragmentClass.getName())
                .replace(R.id.container_fragment, fragmentClass, bundle, fragmentTag).commit();
    }

    public static void backToMainMenu(FragmentActivity fragmentActivity) {
        fragmentActivity.getSupportFragmentManager()
                .popBackStack("ROOT", 0);
    }

    
    public static void removeCurrentFragment(FragmentActivity fragmentActivity){
        fragmentActivity.getSupportFragmentManager().popBackStack();
    }

    public static void installMod(Activity activity, boolean customJavaArgs) {
        if (MultiRTUtils.getExactJreName(8) == null) {
            Toast.makeText(activity, R.string.multirt_nojava8rt, Toast.LENGTH_LONG).show();
            return;
        }

        if(!customJavaArgs){ 
            if(!(activity instanceof LauncherActivity))
                throw new IllegalStateException("Cannot start Mod Installer without LauncherActivity");
            LauncherActivity launcherActivity = (LauncherActivity)activity;
            launcherActivity.modInstallerLauncher.launch(null);
            return;
        }

        
        final EditText editText = new EditText(activity);
        editText.setSingleLine();
        editText.setHint("-jar/-cp /path/to/file.jar ...");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.alerttitle_installmod)
                .setNegativeButton(android.R.string.cancel, null)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (di, i) -> {
                    Intent intent = new Intent(activity, JavaGUILauncherActivity.class);
                    intent.putExtra("javaArgs", editText.getText().toString());
                    activity.startActivity(intent);
                });
        builder.show();
    }

    
    public static ProgressDialog getWaitingDialog(Context ctx, int message){
        final ProgressDialog barrier = new ProgressDialog(ctx);
        barrier.setMessage(ctx.getString(message));
        barrier.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        barrier.setCancelable(false);
        barrier.show();

        return barrier;
    }

    
    public static void launchModInstaller(Activity activity, @NonNull Uri uri){
        Intent intent = new Intent(activity, JavaGUILauncherActivity.class);
        intent.putExtra("modUri", uri);
        activity.startActivity(intent);
    }


    public static void installRuntimeFromUri(Context context, Uri uri){
        sExecutorService.execute(() -> {
            try {
                String name = getFileName(context, uri);
                MultiRTUtils.installRuntimeNamed(
                        NATIVE_LIB_DIR,
                        context.getContentResolver().openInputStream(uri),
                        name);

                MultiRTUtils.postPrepare(name);
            } catch (IOException e) {
                Tools.showError(context, e);
            }
        });
    }

    public static String extractUntilCharacter(String input, String whatFor, char terminator) {
        int whatForStart = input.indexOf(whatFor);
        if(whatForStart == -1) return null;
        whatForStart += whatFor.length();
        int terminatorIndex = input.indexOf(terminator, whatForStart);
        if(terminatorIndex == -1) return null;
        return input.substring(whatForStart, terminatorIndex);
    }

    public static boolean isValidString(String string) {
        return string != null && !string.isEmpty();
    }

    public static String getRuntimeName(String prefixedName) {
        if(prefixedName == null) return prefixedName;
        if(!prefixedName.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX)) return null;
        return prefixedName.substring(Tools.LAUNCHERPROFILES_RTPREFIX.length());
    }

    public static String getSelectedRuntime(MinecraftProfile minecraftProfile) {
        String runtime = LauncherPreferences.PREF_DEFAULT_RUNTIME;
        String profileRuntime = getRuntimeName(minecraftProfile.javaDir);
        if(profileRuntime != null) {
            if(MultiRTUtils.forceReread(profileRuntime).versionString != null) {
                runtime = profileRuntime;
            }
        }
        return runtime;
    }

    public static void runOnUiThread(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }

    public static @NonNull String pickRuntime(MinecraftProfile minecraftProfile, int targetJavaVersion) {
        String runtime = getSelectedRuntime(minecraftProfile);
        String profileRuntime = getRuntimeName(minecraftProfile.javaDir);
        Runtime pickedRuntime = MultiRTUtils.read(runtime);
        if(runtime == null || pickedRuntime.javaVersion == 0 || pickedRuntime.javaVersion < targetJavaVersion) {
            String preferredRuntime = MultiRTUtils.getNearestJreName(targetJavaVersion);
            if(preferredRuntime == null) throw new RuntimeException("Failed to autopick runtime!");
            if(profileRuntime != null) minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX+preferredRuntime;
            runtime = preferredRuntime;
        }
        return runtime;
    }

    
    public static void shareLog(Context context){
        openPath(context, new File(Tools.DIR_GAME_HOME, "latestlog.txt"), true);
    }

    
    public static String getMimeType(File file) {
        if(file.isDirectory()) return DocumentsContract.Document.MIME_TYPE_DIR;
        String mimeType = null;
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            
            
            
            try(BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                mimeType = URLConnection.guessContentTypeFromStream(bufferedInputStream);
            }
        }catch (IOException e) {
            Log.w("FileMimeType", "Failed to determine MIME type by stream", e);
        }
        if(mimeType != null) return mimeType;
        mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if(mimeType != null) return mimeType;
        return "*/*";
    }

    
    public static void openPath(Context context, File file, boolean share) {
        Uri contentUri = DocumentsContract.buildDocumentUri(context.getString(R.string.storageProviderAuthorities), file.getAbsolutePath());
        String mimeType = getMimeType(file);
        Intent intent = new Intent();
        if(share) {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(getMimeType(file));
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        }else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mimeType);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooserIntent = Intent.createChooser(intent, file.getName());
        context.startActivity(chooserIntent);
    }

    
    public static int mesureTextviewHeight(TextView t) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(t.getWidth(), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        t.measure(widthMeasureSpec, heightMeasureSpec);
        return t.getMeasuredHeight();
    }

    
    public static boolean deviceHasHangingLinker() {
        
        
        if(SDK_INT >= Build.VERSION_CODES.O) return false;
        
        
        return Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("huawei");
    }

    public static class RenderersList {
        public final List<String> rendererIds;
        public final String[] rendererDisplayNames;

        public RenderersList(List<String> rendererIds, String[] rendererDisplayNames) {
            this.rendererIds = rendererIds;
            this.rendererDisplayNames = rendererDisplayNames;
        }
    }

    public static boolean checkVulkanSupport(PackageManager packageManager) {
        if(SDK_INT >= Build.VERSION_CODES.N) {
            return packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) &&
                    packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION);
        }
        return false;
    }

    public static <T> T getWeakReference(WeakReference<T> weakReference) {
        if(weakReference == null) return null;
        return weakReference.get();
    }

    
    public static RenderersList getCompatibleRenderers(Context context) {
        if(sCompatibleRenderers != null) return sCompatibleRenderers;
        Resources resources = context.getResources();
        String[] defaultRenderers = resources.getStringArray(R.array.renderer_values);
        String[] defaultRendererNames = resources.getStringArray(R.array.renderer);
        boolean deviceHasVulkan = checkVulkanSupport(context.getPackageManager());
        
        boolean deviceHasOSMesaZinkBinary = new File(Tools.NATIVE_LIB_DIR, "libOSMesa.so").exists();
        boolean deviceHasOpenGLES3 = JREUtils.getDetectedVersion() >= 3;
        
        boolean appHasLtw = new File(Tools.NATIVE_LIB_DIR, "libltw.so").exists();
        boolean appHasKw = new File(Tools.NATIVE_LIB_DIR, "libng_gl4es.so").exists();
        List<String> rendererIds = new ArrayList<>(defaultRenderers.length);
        List<String> rendererNames = new ArrayList<>(defaultRendererNames.length);
        for(int i = 0; i < defaultRenderers.length; i++) {
            String rendererId = defaultRenderers[i];
            if(rendererId.contains("vulkan") && !deviceHasVulkan) continue;
            if(rendererId.contains("vulkan_zink") && !deviceHasOSMesaZinkBinary) continue;
            if(rendererId.contains("ltw") && (!deviceHasOpenGLES3 || !appHasLtw)) continue;
            if(rendererId.contains("opengles2") && (!deviceHasOpenGLES3 || !appHasKw)) continue;
            rendererIds.add(rendererId);
            rendererNames.add(defaultRendererNames[i]);
        }
        sCompatibleRenderers = new RenderersList(rendererIds,
                rendererNames.toArray(new String[0]));

        return sCompatibleRenderers;
    }

    
    public static boolean checkRendererCompatible(Context context, String rendererName) {
         return getCompatibleRenderers(context).rendererIds.contains(rendererName);
    }

    
    public static void releaseRenderersCache() {
        sCompatibleRenderers = null;
        System.gc();
    }

    public static boolean deviceSupportsGyro(@NonNull Context context) {
        return ((SensorManager)context.getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null;

    }

    public static void dialogForceClose(Context ctx) {
        new android.app.AlertDialog.Builder(ctx)
                .setMessage(R.string.mcn_exit_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (p1, p2) -> {
                    try {
                        Tools.fullyExit();
                    } catch (Throwable th) {
                        Log.w(Tools.APP_NAME, "Could not enable System.exit() method!", th);
                    }
                }).show();
    }

    public static void switchDemo(boolean isDemo){
        if(isDemo) {
            DIR_GAME_NEW = DIR_DATA + "/demo/.minecraft";
        } else {
            DIR_GAME_NEW = DIR_GAME_HOME + "/.minecraft";
        }
        DIR_HOME_VERSION = DIR_GAME_NEW + "/versions";
        DIR_HOME_LIBRARY = DIR_GAME_NEW + "/libraries";
        ASSETS_PATH = DIR_GAME_NEW + "/assets";
        OBSOLETE_RESOURCES_PATH = DIR_GAME_NEW + "/resources";
    }

    private static NetworkInfo getActiveNetworkInfo(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo; 
    }

    public static boolean isOnline(Context ctx) {
        NetworkInfo info = getActiveNetworkInfo(ctx);
        if(info == null) return false;
        return (info.isConnected());
    }

    public static boolean isDemoProfile(Context ctx){
        MinecraftAccount currentProfile = PojavProfile.getCurrentProfileContent(ctx, null);
        return currentProfile != null && currentProfile.isDemo();
    }

    public static boolean isLocalProfile(Context ctx){
        MinecraftAccount currentProfile = PojavProfile.getCurrentProfileContent(ctx, null);
        return currentProfile == null || currentProfile.isLocal();
    }
    public static boolean hasOnlineProfile(){
        for (MinecraftAccount accountToCheck : getAllProfiles()) {
            if (!accountToCheck.isLocal() && !accountToCheck.isDemo()) {
                return true;
            }
        }
        return false;
    }

    public static void hasNoOnlineProfileDialog(Activity activity, @Nullable Runnable run, @Nullable String customTitle, @Nullable String customMessage){
        if (hasOnlineProfile() && !Tools.isDemoProfile(activity)){
            if (run != null) { 
                run.run();
            }
        } else { 
            customTitle = customTitle == null ? activity.getString(R.string.no_minecraft_account_found) : customTitle;
            customMessage = customMessage == null ? activity.getString(R.string.feature_requires_java_account) : customMessage;
            dialogOnUiThread(activity, customTitle, customMessage);
        }
    }

    
    public static void hasNoOnlineProfileDialog(Activity activity){
        hasNoOnlineProfileDialog(activity, null, null, null);
    }
    public static void hasNoOnlineProfileDialog(Activity activity, Runnable run){
        hasNoOnlineProfileDialog(activity, run, null, null);
    }
    public static void hasNoOnlineProfileDialog(Activity activity, String customTitle, String customMessage){
        hasNoOnlineProfileDialog(activity, null, customTitle, customMessage);
    }

    public static String getSelectedVanillaMcVer(){
        String selectedProfile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "");
        MinecraftProfile selected = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
        if (selected == null) { 
            throw new RuntimeException("No profile selected, how did you reach this? Go ask in the discord or github");
        }
        String currentMCVersion = selected.lastVersionId;
        String vanillaVersion = currentMCVersion;
        File providedJsonFile = new File(Tools.DIR_HOME_VERSION + "/" + currentMCVersion + "/" + currentMCVersion + ".json");
        JMinecraftVersionList.Version providedJsonVersion = null;
        try {
            providedJsonVersion = Tools.GLOBAL_GSON.fromJson(Tools.read(providedJsonFile.getAbsolutePath()), JMinecraftVersionList.Version.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            vanillaVersion = providedJsonVersion.inheritsFrom != null ? providedJsonVersion.inheritsFrom : vanillaVersion;
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
        return vanillaVersion;
    }

    public static Integer mcVersiontoInt(String mcVersion){
        String[] sVersionArray = mcVersion.split("\\.");
        String[] iVersionArray = new String[3];
        
        for (int i = 0; i < iVersionArray.length; i++) {
            try {
                
                sVersionArray[i] =  String.format("%3s", sVersionArray[i]).replace(' ', '0');
                
                sVersionArray[i] = sVersionArray[i].substring(sVersionArray[i].length() - 3);
            } catch (ArrayIndexOutOfBoundsException ignored){
                
                iVersionArray[i] = "000";
                continue;
            }
            try {
                
                Integer.parseInt(sVersionArray[i]);
                iVersionArray[i] = sVersionArray[i];
            } catch (NumberFormatException e) {
                throw new RuntimeException("Tools(mcVersiontoInt): Invalid version string");
            }
        }
        return Integer.parseInt(iVersionArray[0] + iVersionArray[1] + iVersionArray[2]);
    }

    public static boolean isPointerDeviceConnected() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int id : deviceIds) {
            InputDevice device = InputDevice.getDevice(id);
            if (device == null) continue;
            int sources = device.getSources();
            if ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE
                    || (sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD
                    || (sources & InputDevice.SOURCE_TRACKBALL) == InputDevice.SOURCE_TRACKBALL) {
                return true;
            }
        }
        return false;
    }

    public static Object runMethodbyReflection(String className, String methodName) throws ReflectiveOperationException{
        Class<?> clazz = Class.forName(className);
        Method method = clazz.getDeclaredMethod(methodName);
        method.setAccessible(true);
        Object motionListener = method.invoke(null);
        assert motionListener != null;
        return motionListener;
    }

    public static class SDL {
        
        public static native void initializeControllerSubsystems();
    }
    public static native String jObjectToString(Object object);
    public static native long getJavaVMPointer();
}
