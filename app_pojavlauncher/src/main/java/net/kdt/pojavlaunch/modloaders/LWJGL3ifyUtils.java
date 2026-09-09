package net.kdt.pojavlaunch.modloaders;

import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class LWJGL3ifyUtils {
    public static LWJGL3ifyVersionList getLWJGL3ifyVersionList(ModpackApi modpackApi) throws IOException {
        ModDetail lwjgl3ifyModDetail = getLWJGL3ifyModDetail(modpackApi);
        List<LWJGL3ifyMod> supportedVersions = new ArrayList<>(), brokenVersions = new ArrayList<>();
        for (int i = 0; i < lwjgl3ifyModDetail.versionNames.length; i++) {
            String normalizedVersion = normalizeVersionName(lwjgl3ifyModDetail.versionNames[i], lwjgl3ifyModDetail.apiSource);


            LWJGL3ifyMod version = new LWJGL3ifyMod(
                    normalizedVersion,
                    lwjgl3ifyModDetail.versionUrls[i],
                    lwjgl3ifyModDetail.imageUrl,
                    lwjgl3ifyModDetail.id,
                    lwjgl3ifyModDetail.versionHashes[i],
                    lwjgl3ifyModDetail.dependencies[i]
            );
            


            supportedVersions.add(version);

        }
        return new LWJGL3ifyVersionList(supportedVersions, brokenVersions);
    }

    
    public static LWJGL3ifyMod getLWJGL3ifyVersion(String jarName, ModpackApi source) throws IOException {
        
        String providedNormalizedVersion = normalizeVersionName(jarName, Constants.SOURCE_CURSEFORGE);
        ModDetail lwjgl3ifyModDetail = getLWJGL3ifyModDetail(source);
        for (int i = 0; i < lwjgl3ifyModDetail.versionNames.length; i++) {
            String normalizedVersion = normalizeVersionName(lwjgl3ifyModDetail.versionNames[i], lwjgl3ifyModDetail.apiSource);
            if (providedNormalizedVersion.equals(normalizedVersion))
                return new LWJGL3ifyMod(
                    providedNormalizedVersion,
                    lwjgl3ifyModDetail.versionUrls[i],
                    lwjgl3ifyModDetail.imageUrl,
                    lwjgl3ifyModDetail.id,
                    lwjgl3ifyModDetail.versionHashes[i],
                    lwjgl3ifyModDetail.dependencies[i]
            );
        }
        String sourceName = (lwjgl3ifyModDetail.apiSource == Constants.SOURCE_MODRINTH) ? "Modrinth" : "Curseforge";
        throw new IllegalArgumentException("Cannot find LWJGL3ify version "+providedNormalizedVersion+" from "+sourceName);
    }

    
    public static List<LWJGL3ifyMod> collectDependencies(LWJGL3ifyMod lwjgl3ifyMod, ModpackApi modpackApi) {
        List<LWJGL3ifyMod> allDeps = new ArrayList<>();
        for (ModDetail.Dependencies dep : lwjgl3ifyMod.dependencies) {
            ModDetail detail = getModDetail(modpackApi, dep.project_id);
            if (detail != null) {
                LWJGL3ifyMod newMod = new LWJGL3ifyMod(
                        detail.versionNames[0],
                        detail.versionUrls[0],
                        detail.imageUrl,
                        detail.id,
                        detail.versionHashes[0],
                        detail.dependencies[0]
                );

                allDeps.add(newMod);
                
                allDeps.addAll(collectDependencies(newMod, modpackApi));
            }
        }
        return allDeps;
    }

    @NonNull
    private static String normalizeVersionName(String versionName, int apiSource) {
        if (apiSource == Constants.SOURCE_MODRINTH) { 
            versionName = versionName.replaceAll(" - .*", "");
        }else if (apiSource == Constants.SOURCE_CURSEFORGE) { 
            versionName = versionName.split("-")[1].replace(".jar", "");
        }else throw new IllegalArgumentException("LWJGL3ify is only available on Modrinth or Curseforge!");
        return versionName;
    }

    @Nullable
    private static ModDetail getModDetail(ModpackApi modpackApi, String id){
        ModDetail modDetail = null;
        try {
            
            if (id != null && !id.isEmpty()) {
                modDetail = fetch(modpackApi, Constants.SOURCE_MODRINTH, id);
            }
            if (modDetail == null && id != null && !id.isEmpty()) {
                Integer.parseInt(id); 
                modDetail = fetch(modpackApi, Constants.SOURCE_CURSEFORGE, id);
            }
        } catch (NumberFormatException ignored) {}
        return modDetail;
    }

    private static ModDetail fetch(ModpackApi modpackApi, int source, String id) {
        ModItem item = new ModItem(source, false, id, null, null, null);
        return modpackApi.getModDetails(item);
    }

    @NonNull
    private static ModDetail getLWJGL3ifyModDetail(ModpackApi modpackApi) throws IOException {
        ModDetail lwjgl3ifyModDetail = getModDetail(modpackApi, "lwjgl3ify");
        if (lwjgl3ifyModDetail == null) 
            lwjgl3ifyModDetail = getModDetail(modpackApi, "998880");
        if (lwjgl3ifyModDetail == null) throw new IOException("Unable to fetch LWJGL3ify version list from Curseforge and Modrinth. " +
                "Please check your internet connection and whether Modrinth and Curseforge are accessible.");
        return lwjgl3ifyModDetail;
    }

    public static JMinecraftVersionList.Version installJson(File modJar) throws IOException {
        String profileID = getProfileID(modJar);
        String jsonPath = getJsonPath(profileID);
        try {
            JMinecraftVersionList.Version version = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(
                            ZipUtils.getEntryStream(
                                    new ZipFile(modJar),
                                    "me/eigenraven/lwjgl3ify/relauncher/version.json"
                            )
                    ),
                    JMinecraftVersionList.Version.class);
            version.id = profileID;
            if (!org.apache.commons.io.FileUtils.getFile(jsonPath).exists())
                Tools.write(jsonPath, Tools.GLOBAL_GSON.toJson(version));
            return version;
        } catch (IOException e) {
            throw new IOException("Failed to install "+profileID+" json file.");
        }
    }

    @NonNull
    public static String getJsonPath(String profileID) {
        return Tools.DIR_HOME_VERSION + "/" + profileID + "/" + profileID + ".json";
    }

    @NonNull
    public static String getProfileID(File modJar) throws IOException {
        return "1.7.10-LWJGL3ify-"+getVersionFromJar(modJar);
    }

    public static String getVersionFromJar(File modJar) throws IOException {
        try (ZipFile zipFile = new ZipFile(modJar)) {
            JsonObject root = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(zipFile, "mcmod.info")),
                    JsonObject.class);
            return root.getAsJsonArray("modList")
                    .get(0).getAsJsonObject()
                    .get("version").getAsString();
        }
    }

    public static void createInstance(MinecraftProfile profile, File modJar, String versionName) throws IOException {
        File modsDir = new File(Tools.DIR_GAME_HOME, profile.gameDir+"/mods");
        if (modsDir.isFile()) {
            if (!modsDir.delete()) {
                throw new IOException("Failed to delete file where directory should be: " + modsDir.getAbsolutePath());
            }
        }
        try {
            FileUtils.ensureDirectory(modsDir);
        } catch (IOException e) {
            throw new IOException("Failed to create folder " + modsDir.getAbsolutePath());
        }
        
        try (FileInputStream fis = new FileInputStream(modJar);
             FileOutputStream fos = new FileOutputStream(new File(modsDir, "lwjgl3ify-"+ versionName+".jar"))) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MinecraftProfile createProfile(String profileID, String versionName, String iconUrl) {
        MinecraftProfile LWJGL3ifyProfile = new MinecraftProfile();
        LWJGL3ifyProfile.lastVersionId = profileID;
        LWJGL3ifyProfile.name = "LWJGL3ify - "+ versionName;
        LWJGL3ifyProfile.gameDir = String.format("./custom_instances/LWJGL3ify_%s", versionName);
        LWJGL3ifyProfile.icon = tryDownloadIcon(iconUrl);
        return LWJGL3ifyProfile;
    }

    public static String tryDownloadIcon(String iconUrl) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream, Base64.DEFAULT)){
            
            
            byteArrayOutputStream.write("data:image/png;base64,".getBytes(StandardCharsets.US_ASCII));
            DownloadUtils.download(iconUrl, base64OutputStream);
            return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.US_ASCII);
        }catch (IOException e) {
            Log.w(LWJGL3ifyDownloadTask.TAG, "Failed to download base64 icon", e);
        }finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                Log.wtf(LWJGL3ifyDownloadTask.TAG, "Failed to close a byte array stream??", e);
            }
        }
        return null;
    }

    
    public static class LWJGL3ifyMod {
        public final String versionName;
        public final String downloadUrl;
        public final String iconUrl;
        public final String id;
        public final String hash;
        public final ModDetail.Dependencies[] dependencies;

        public LWJGL3ifyMod(String versionName, String downloadUrl, String iconUrl, String id, String hash, ModDetail.Dependencies[] dependencies) {
            this.versionName = versionName;
            this.downloadUrl = downloadUrl;
            this.iconUrl = iconUrl;
            this.id = id;
            this.hash = hash;
            this.dependencies = dependencies;
        }
    }
    public static class LWJGL3ifyVersionList {
        public final List<LWJGL3ifyMod> supportedVersions;
        public final List<LWJGL3ifyMod> brokenVersions; 

        public LWJGL3ifyVersionList(List<LWJGL3ifyMod> mSupportedVersions, List<LWJGL3ifyMod> mBrokenVersions) {
            this.supportedVersions = mSupportedVersions;
            this.brokenVersions = mBrokenVersions;
        }
    }
}
