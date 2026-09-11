package net.kdt.pojavlaunch.kollegen;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KollegenChatFragment extends Fragment {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private String mOtherId;
    private String mOtherName;
    private LinearLayout mMessages;
    private ScrollView mScroller;
    private EditText mInput;
    private boolean mPolling;
    private long mLatestTs;


    public KollegenChatFragment() {
        super(R.layout.fragment_kollegen_chat);
    }

    public KollegenChatFragment(String otherId, String otherName) {
        this();
        mOtherId = otherId;
        mOtherName = otherName;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int[] pal = KollegenTheme.palette();
        view.setBackgroundColor(pal[KollegenTheme.BG]);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8));
        header.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));

        Button back = KollegenKit.tinyButton(requireContext(), "\u2190");
        back.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        header.addView(back);

        LinearLayout nameCol = new LinearLayout(requireContext());
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(KollegenKit.dp(requireContext(), 10), 0, 0, 0);
        TextView nm = new TextView(requireContext());
        nm.setText(mOtherName);
        nm.setTextColor(pal[KollegenTheme.TEXT]);
        nm.setTextSize(16);
        nm.setTypeface(Typeface.DEFAULT_BOLD);
        nameCol.addView(nm);
        final TextView st = new TextView(requireContext());
        st.setTextColor(pal[KollegenTheme.MUTED]);
        st.setTextSize(12);
        nameCol.addView(st);
        header.addView(nameCol);

        Button call = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_call));
        call.setOnClickListener(v -> KollegenCall.startDirect(requireContext(), mOtherId, mOtherName));
        header.addView(call);

        root.addView(header);

        mScroller = new ScrollView(requireContext());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        mScroller.setLayoutParams(sp);
        mMessages = new LinearLayout(requireContext());
        mMessages.setOrientation(LinearLayout.VERTICAL);
        mMessages.setPadding(KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 12));
        mScroller.addView(mMessages);
        root.addView(mScroller);

        LinearLayout inputRow = new LinearLayout(requireContext());
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8));
        inputRow.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));

        mInput = new EditText(requireContext());
        mInput.setHint(R.string.kollegen_chat_hint);
        mInput.setTextColor(pal[KollegenTheme.TEXT]);
        mInput.setHintTextColor(pal[KollegenTheme.MUTED]);
        mInput.setSingleLine(false);
        mInput.setBackground(null);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mInput.setLayoutParams(ip);
        inputRow.addView(mInput);

        Button send = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_chat_send));
        send.setOnClickListener(v -> sendMessage());
        inputRow.addView(send);

        root.addView(inputRow);

        ((ViewGroup) view).removeAllViews();
        ((ViewGroup) view).addView(root);

        loadPresence(st);
        loadMessages(false);
        startPolling();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPolling = false;
        sHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) startPolling();
    }

    private void loadPresence(final TextView status) {
        if (mOtherId == null || mOtherId.isEmpty()) return;
        KollegenApi.get("/api/profil/friends", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded()) return;
                JSONArray arr = KollegenApi.optArray(json, "");
                if (arr == null) return;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject f = arr.optJSONObject(i);
                    if (f != null && mOtherId.equals(f.optString("discordId", f.optString("id", "")))) {
                        boolean online = f.optBoolean("online", false);
                        status.setText(online ? R.string.kollegen_online : R.string.kollegen_offline);
                        status.setTextColor(KollegenTheme.palette()[online ? KollegenTheme.ACCENT : KollegenTheme.MUTED]);
                        break;
                    }
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void startPolling() {
        if (mPolling || mOtherId == null) return;
        mPolling = true;
        sHandler.postDelayed(() -> {
            if (!mPolling) return;
            loadMessages(true);
            sHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPolling) startPolling();
                }
            }, 3000);
        }, 3000);
    }

    private void sendMessage() {
        Editable e = mInput != null ? mInput.getText() : null;
        String text = e == null ? "" : e.toString().trim();
        if (text.isEmpty()) return;
        JSONObject body = new JSONObject();
        try {
            body.put("other", mOtherId);
            body.put("text", text);
        } catch (Exception ignored) {}
        KollegenApi.post("/api/profil/dm/send", body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (isAdded() && mInput != null) mInput.setText("");
                loadMessages(false);
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    new AlertDialog.Builder(requireContext()).setMessage(message).setPositiveButton(R.string.kollegen_close, null).show();
                }
            }
        });
    }

    private void loadMessages(boolean incremental) {
        if (mOtherId == null) return;
        String path = "/api/profil/dm/messages?other=" + KollegenApi.encode(mOtherId);
        KollegenApi.get(path, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded()) return;
                List<JSONObject> msgs = new ArrayList<>();
                JSONArray arr = KollegenApi.optArray(json, "");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) msgs.add(arr.optJSONObject(i));
                }
                renderMessages(msgs, incremental);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void renderMessages(List<JSONObject> msgs, boolean incremental) {
        int[] pal = KollegenTheme.palette();
        boolean scrollToBottom = !incremental;
        for (JSONObject m : msgs) {
            long ts = m.optLong("ts", 0);
            if (incremental && ts <= mLatestTs) continue;
            String from = m.optString("from", "");
            String text = m.optString("text", "");
            boolean mine = !from.equals(mOtherId);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.topMargin = KollegenKit.dp(requireContext(), 6);
            row.setLayoutParams(rowLp);

            TextView bubble = new TextView(requireContext());
            bubble.setText(text);
            bubble.setTextSize(15);
            bubble.setTextColor(pal[KollegenTheme.TEXT]);
            bubble.setPadding(KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 8));
            bubble.setBackground(KollegenTheme.rounded(mine ? pal[KollegenTheme.PANEL2] : pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (mine) { bp.gravity = Gravity.END; }
            else { bp.gravity = Gravity.START; }
            bubble.setLayoutParams(bp);
            row.addView(bubble);

            TextView time = new TextView(requireContext());
            time.setText(formatTime(ts));
            time.setTextColor(pal[KollegenTheme.MUTED]);
            time.setTextSize(11);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.gravity = mine ? Gravity.END : Gravity.START;
            time.setLayoutParams(tp);
            row.addView(time);

            if (mMessages.getChildCount() == 0 && incremental && ts > mLatestTs) {
                mMessages.addView(row);
            } else {
                mMessages.addView(row);
            }
            mLatestTs = Math.max(mLatestTs, ts);
        }
        if (scrollToBottom) mScroller.post(() -> mScroller.fullScroll(View.FOCUS_DOWN));
    }

    private String formatTime(long ts) {
        if (ts <= 0) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ts));
    }
}