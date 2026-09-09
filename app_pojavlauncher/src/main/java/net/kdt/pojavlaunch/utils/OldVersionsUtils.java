package net.kdt.pojavlaunch.utils;

import android.util.Log;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.text.ParseException;
import java.util.Date;


public class OldVersionsUtils {
    
    public static void selectOpenGlVersion(JMinecraftVersionList.Version version){
        
        String creationTime = version.time;
        if(!Tools.isValidString(creationTime)){
            ExtraCore.setValue(ExtraConstants.OPEN_GL_VERSION, "2");
            return;
        }

        try {
           Date creationDate = DateUtils.parseReleaseDate(creationTime);
            if(creationDate == null) {
                Log.e("GL_SELECT", "Failed to parse version date");
                ExtraCore.setValue(ExtraConstants.OPEN_GL_VERSION, "2");
                return;
            }
            String openGlVersion =  DateUtils.dateBefore(creationDate, 2011, 6, 8) ? "1" : "2";
            Log.i("GL_SELECT", openGlVersion);
            ExtraCore.setValue(ExtraConstants.OPEN_GL_VERSION, openGlVersion);
        }catch (ParseException exception){
            Log.e("GL_SELECT", exception.toString());
            ExtraCore.setValue(ExtraConstants.OPEN_GL_VERSION, "2");
        }
    }
}
