package net.kdt.pojavlaunch.kollegen;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

public final class KollegenCall {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private static AlertDialog sDialog;
    private static String sCallId;
    private static boolean sLive;
    private static TextView sStatus;

    private KollegenCall() {}

    public static void startDirect(Context context, String peerId, String peerName) {
        JSONObject body = new JSONObject();
        try { body.put("peerId", peerId); } catch (Exception ignored) {}
        open(context, peerName, body, "/api/profil/call/direct/open", true);
    }

    public static void startGroup(Context context, String groupId, String groupName) {
        JSONObject body = new JSONObject();
        try { body.put("groupId", groupId); } catch (Exception ignored) {}
        open(context, groupName, body, "/api/profil/call/open", false);
    }

    private static void open(final Context context, final String name, JSONObject body, final String path, final boolean direct) {
        if (sDialog != null && sDialog.isShowing()) return;
        int[] pal = KollegenTheme.palette();
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = KollegenKit.dp(context, 20);
        box.setPadding(pad, KollegenKit.dp(context, 8), pad, 0);

        sStatus = new TextView(context);
        sStatus.setText(context.getString(R.string.kollegen_call_ringing));
        sStatus.setTextColor(pal[KollegenTheme.TEXT]);
        sStatus.setTextSize(16);
        sStatus.setTypeface(Typeface.DEFAULT_BOLD);
        sStatus.setGravity(Gravity.CENTER);
        box.addView(sStatus);

        final TextView peer = new TextView(context);
        peer.setText(name);
        peer.setTextColor(pal[KollegenTheme.ACCENT]);
        peer.setTextSize(14);
        peer.setGravity(Gravity.CENTER);
        box.addView(peer);

        Button hangup = new Button(context);
        hangup.setText(R.string.kollegen_call_hangup);
        hangup.setAllCaps(false);
        hangup.setBackgroundColor(pal[KollegenTheme.DANGER]);
        hangup.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, KollegenKit.dp(context, 44));
        hp.topMargin = KollegenKit.dp(context, 16);
        hangup.setLayoutParams(hp);
        hangup.setOnClickListener(v -> {
            JSONObject lb = new JSONObject();
            try { lb.put("callId", sCallId); } catch (Exception ignored) {}
            KollegenApi.post("/api/profil/call/leave", lb, new KollegenApi.Callback() {
                @Override public void onResult(Object json) {}
                @Override public void onError(String message) {}
            });
            sLive = false;
            sHandler.removeCallbacksAndMessages(null);
            if (sDialog != null) sDialog.dismiss();
        });
        box.addView(hangup);

        sDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.kollegen_call)
                .setView(box)
                .setCancelable(false)
                .create();
        sDialog.getWindow().setBackgroundDrawable(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        sDialog.show();

        KollegenApi.post(path, body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (json instanceof JSONObject) {
                    sCallId = ((JSONObject) json).optString("callId", "");
                }
                if (sCallId.isEmpty()) {
                    sStatus.setText(R.string.kollegen_call_failed);
                    return;
                }
                sLive = true;
                poll(context, direct);
            }

            @Override
            public void onError(String message) {
                sStatus.setText(message);
            }
        });
    }

    private static void poll(final Context context, final boolean direct) {
        if (!sLive || sCallId.isEmpty()) return;
        String path = "/api/profil/call/direct/poll?callId=" + KollegenApi.encode(sCallId) + "&sinceSig=0";
        KollegenApi.get(path, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!sLive) return;
                if (json instanceof JSONObject) {
                    JSONArray members = ((JSONObject) json).optJSONArray("members");
                    String txt = context.getString(R.string.kollegen_call_ringing);
                    if (members != null && members.length() >= 2) txt = context.getString(R.string.kollegen_call_connected);
                    sStatus.setText(txt);
                }
                if (sLive) sHandler.postDelayed(() -> poll(context, direct), 2000);
            }

            @Override
            public void onError(String message) {
                if (sLive) sHandler.postDelayed(() -> poll(context, direct), 2500);
            }
        });
    }
}