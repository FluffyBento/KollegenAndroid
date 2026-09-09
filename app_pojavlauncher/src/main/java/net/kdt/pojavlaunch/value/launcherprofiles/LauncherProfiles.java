package net.kdt.pojavlaunch.value.launcherprofiles;

import android.util.Log;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LauncherProfiles {
    public static MinecraftLauncherProfiles mainProfileJson;
    private static final File launcherProfilesFile = new File(Tools.GAME_PROFILES_FILE);

    
    public static void load(){
        if (launcherProfilesFile.exists()) {
            try {
                mainProfileJson = Tools.GLOBAL_GSON.fromJson(Tools.read(launcherProfilesFile.getAbsolutePath()), MinecraftLauncherProfiles.class);
            } catch (IOException e) {
                Log.e(LauncherProfiles.class.toString(), "Failed to load file: ", e);
                throw new RuntimeException(e);
            }
        }

        
        if (mainProfileJson == null) mainProfileJson = new MinecraftLauncherProfiles();
        if (mainProfileJson.profiles == null) mainProfileJson.profiles = new HashMap<>();
        if (mainProfileJson.profiles.size() == 0)
            mainProfileJson.profiles.put(UUID.randomUUID().toString(), MinecraftProfile.getDefaultProfile());

        
        if(normalizeProfileIds(mainProfileJson)){
            write();
            load();
        }
    }

    
    public static void write() {
        try {
            Tools.write(launcherProfilesFile.getAbsolutePath(), mainProfileJson.toJson());
        } catch (IOException e) {
            Log.e(LauncherProfiles.class.toString(), "Failed to write profile file", e);
            throw new RuntimeException(e);
        }
    }

    public static @NonNull MinecraftProfile getCurrentProfile() {
        if(mainProfileJson == null) LauncherProfiles.load();
        String defaultProfileName = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "");
        MinecraftProfile profile = mainProfileJson.profiles.get(defaultProfileName);
        if(profile == null) throw new RuntimeException("The current profile stopped existing :(");
        return profile;
    }

    
    public static void insertMinecraftProfile(MinecraftProfile minecraftProfile) {
        mainProfileJson.profiles.put(getFreeProfileKey(), minecraftProfile);
    }

    
    public static String getFreeProfileKey() {
        Map<String, MinecraftProfile> profileMap = mainProfileJson.profiles;
        String freeKey = UUID.randomUUID().toString();
        while(profileMap.get(freeKey) != null) freeKey = UUID.randomUUID().toString();
        return freeKey;
    }

    
    private static boolean normalizeProfileIds(MinecraftLauncherProfiles launcherProfiles){
        boolean hasNormalized = false;
        ArrayList<String> keys = new ArrayList<>();

        
        for(String profileKey : launcherProfiles.profiles.keySet()){
            try{
                if(!UUID.fromString(profileKey).toString().equals(profileKey)) keys.add(profileKey);
            }catch (IllegalArgumentException exception){
                keys.add(profileKey);
                Log.w(LauncherProfiles.class.toString(), "Illegal profile uuid: " + profileKey);
            }
        }

        
        for(String profileKey : keys){
            MinecraftProfile currentProfile = launcherProfiles.profiles.get(profileKey);
            insertMinecraftProfile(currentProfile);
            launcherProfiles.profiles.remove(profileKey);
            hasNormalized = true;
        }

        return hasNormalized;
    }
}
