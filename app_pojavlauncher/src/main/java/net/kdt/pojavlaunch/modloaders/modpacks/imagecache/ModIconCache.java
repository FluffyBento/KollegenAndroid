package net.kdt.pojavlaunch.modloaders.modpacks.imagecache;

import android.util.Base64;
import android.util.Log;

import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ModIconCache {
    ThreadPoolExecutor cacheLoaderPool = new ThreadPoolExecutor(10,
            10,
            1000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
    File cachePath;
    private final List<WeakReference<ImageReceiver>> mCancelledReceivers = new ArrayList<>();
    public ModIconCache() {
        cachePath = getImageCachePath();
        if(!cachePath.exists() && !cachePath.isFile() && Tools.DIR_CACHE.canWrite()) {
            if(!cachePath.mkdirs())
                throw new RuntimeException("Failed to create icon cache directory");
        }

    }
    static File getImageCachePath() {
        return new File(Tools.DIR_CACHE, "mod_icons");
    }

    
    public void getImage(ImageReceiver imageReceiver, String imageTag, String imageUrl) {
        cacheLoaderPool.execute(new ReadFromDiskTask(this, imageReceiver, imageTag, imageUrl));
    }

    
    public void cancelImage(ImageReceiver imageReceiver) {
        synchronized (mCancelledReceivers) {
            mCancelledReceivers.add(new WeakReference<>(imageReceiver));
        }
    }

    boolean checkCancelled(ImageReceiver imageReceiver) {
        boolean isCanceled = false;
        synchronized (mCancelledReceivers) {
            Iterator<WeakReference<ImageReceiver>> iterator = mCancelledReceivers.iterator();
            while (iterator.hasNext()) {
                WeakReference<ImageReceiver> reference = iterator.next();
                if (reference.get() == null) {
                    iterator.remove();
                    continue;
                }
                if(reference.get() == imageReceiver) {
                    isCanceled = true;
                }
            }
        }
        if(isCanceled) Log.i("IconCache", "checkCancelled("+imageReceiver.hashCode()+") == true");
        return isCanceled;
    }

    

    public static String getBase64Image(String imageTag) {
        File imagePath = new File(Tools.DIR_CACHE, "mod_icons/"+imageTag+".ca");
        Log.i("IconCache", "Creating base64 version of icon "+imageTag);
        if(!imagePath.canRead() || !imagePath.isFile()) {
            Log.i("IconCache", "Icon does not exist");
            return null;
        }
        try {
            try(FileInputStream fileInputStream = new FileInputStream(imagePath)) {
                byte[] imageBytes = IOUtils.toByteArray(fileInputStream);
                
                
                return "data:image/png;base64,"+ Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
