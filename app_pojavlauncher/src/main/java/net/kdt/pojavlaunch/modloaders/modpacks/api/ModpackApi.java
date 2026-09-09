package net.kdt.pojavlaunch.modloaders.modpacks.api;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchResult;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;

import java.io.IOException;
import java.io.File;
import java.security.NoSuchAlgorithmException;



public interface ModpackApi {

    
    SearchResult searchMod(SearchFilters searchFilters, SearchResult previousPageResult);

    
    default SearchResult searchMod(SearchFilters searchFilters) {
        return searchMod(searchFilters, null);
    }

    
    ModDetail getModDetails(ModItem item);

    
    default void handleInstallation(Context context, ModDetail modDetail, int selectedVersion) {
        
        
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.global_waiting);
        PojavApplication.sExecutorService.execute(() -> {
            try {
                ModLoader loaderInfo = installMod(modDetail, selectedVersion);
                if (loaderInfo == null) return;
                loaderInfo.getDownloadTask(new NotificationDownloadListener(context, loaderInfo)).run();
            }catch (IOException e) {
                Tools.showErrorRemote(context, R.string.modpack_install_download_failed, e);
            }
        });
    }

    
    ModLoader installMod(ModDetail modDetail, int selectedVersion) throws IOException;

    
    ModLoader importModpack(File modpackFile) throws IOException, NoSuchAlgorithmException;
}
