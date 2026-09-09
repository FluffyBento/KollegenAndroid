package net.kdt.pojavlaunch.utils;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class DateUtils {
    
    public static Date parseReleaseDate(String releaseTime) throws ParseException {
        if(releaseTime == null) return null;
        int tIndexOf = releaseTime.indexOf('T');
        if(tIndexOf != -1) releaseTime = releaseTime.substring(0, tIndexOf);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(releaseTime);
    }

    
    public static boolean dateBefore(@NonNull Date date, int year, int month, int dayOfMonth) {
        return date.before(new Date(new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis()));
    }

    
    public static Date getOriginalReleaseDate(JMinecraftVersionList.Version gameVersion) throws ParseException {
        if(Tools.isValidString(gameVersion.inheritsFrom)) {
            gameVersion = Tools.getVersionInfo(gameVersion.inheritsFrom, true);
        }else {
            
            
            
            gameVersion = Tools.getVersionInfo(gameVersion.id, true);
        }
        return parseReleaseDate(gameVersion.releaseTime);
    }
}
