package net.kdt.pojavlaunch.utils;


import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_FORCE_ENGLISH;

import android.content.*;
import android.content.res.*;
import android.os.Build;
import android.os.LocaleList;

import androidx.preference.*;
import java.util.*;

public class LocaleUtils extends ContextWrapper {

    public LocaleUtils(Context base) {
        super(base);
    }

    public static Context setLocale(Context context) {
        if (DEFAULT_PREF == null) {
            DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(context);
            PREF_FORCE_ENGLISH = DEFAULT_PREF.getBoolean("force_english", false);
        }

        if (!PREF_FORCE_ENGLISH) {
            return context;
        }

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        configuration.setLocale(Locale.ENGLISH);
        Locale.setDefault(Locale.ENGLISH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(Locale.ENGLISH);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return context.createConfigurationContext(configuration);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return new LocaleUtils(context);
    }
}
