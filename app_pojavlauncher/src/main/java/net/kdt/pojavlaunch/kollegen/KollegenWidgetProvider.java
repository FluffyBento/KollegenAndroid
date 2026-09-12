package net.kdt.pojavlaunch.kollegen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.TestStorageActivity;

import org.json.JSONObject;

public class KollegenWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidget(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("net.kdt.pojavlaunch.kollegen.WIDGET_REFRESH".equals(intent.getAction())) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            updateWidget(context, mgr, mgr.getAppWidgetIds(new ComponentName(context, KollegenWidgetProvider.class)));
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] ids) {
        if (ids == null || ids.length == 0) return;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kollegen_widget);
        views.setTextViewText(R.id.koll_widget_name, context.getString(R.string.kollegen_widget_loading));
        views.setTextViewText(R.id.koll_widget_meta, "");

        Intent open = new Intent(context, TestStorageActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.koll_widget_root, pi);

        Intent refresh = new Intent(context, KollegenWidgetProvider.class).setAction("net.kdt.pojavlaunch.kollegen.WIDGET_REFRESH");
        PendingIntent rpi = PendingIntent.getBroadcast(context, 1, refresh, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.koll_widget_refresh, rpi);

        final Thread t = new Thread(() -> {
            JSONObject me = fetchMe();
            if (me == null) {
                views.setTextViewText(R.id.koll_widget_name, context.getString(R.string.kollegen_widget_not_logged_in));
                views.setTextViewText(R.id.koll_widget_meta, context.getString(R.string.app_name));
            } else {
                String name = me.optString("mcName", "");
                if (name.isEmpty()) name = me.optString("name", "");
                views.setTextViewText(R.id.koll_widget_name, name.isEmpty() ? context.getString(R.string.app_name) : name);
                int points = me.optInt("points", 0);
                int level = me.optInt("level", 0);
                String meta = context.getString(R.string.kollegen_level, level) + " \u00b7 " + context.getString(R.string.kollegen_points, points);
                views.setTextViewText(R.id.koll_widget_meta, meta);
            }
            appWidgetManager.updateAppWidget(ids, views);
        });
        t.start();
    }

    private static JSONObject fetchMe() {
        final JSONObject[] out = {null};
        final Object lock = new Object();
        KollegenApi.get("/api/profil/me", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (json instanceof JSONObject) out[0] = (JSONObject) json;
                synchronized (lock) { lock.notifyAll(); }
            }

            @Override
            public void onError(String message) {
                synchronized (lock) { lock.notifyAll(); }
            }
        });
        try {
            synchronized (lock) { lock.wait(15000); }
        } catch (InterruptedException ignored) {}
        return out[0];
    }
}