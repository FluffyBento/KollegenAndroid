package net.kdt.pojavlaunch.kollegen;

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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

public class KollegenGroupsFragment extends Fragment {
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private static String sMyId;

    private LinearLayout mRoot;
    private ScrollView mThreadScroller;
    private LinearLayout mThreadBody;
    private String mOpenGroupId;
    private boolean mPolling;

    public KollegenGroupsFragment() {
        super(R.layout.fragment_kollegen_groups);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRoot = new LinearLayout(requireContext());
        mRoot.setOrientation(LinearLayout.VERTICAL);
        ((ViewGroup) view).removeAllViews();
        ((ViewGroup) view).addView(mRoot);
        ensureMe(() -> buildList());
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
        if (mOpenGroupId != null) startPollingThread();
    }

    private void ensureMe(final Runnable next) {
        if (sMyId != null) {
            next.run();
            return;
        }
        KollegenApi.get("/api/profil/me", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (json instanceof JSONObject) {
                    String id = ((JSONObject) json).optString("discordId", "");
                    if (!id.isEmpty()) sMyId = id;
                }
                next.run();
            }

            @Override
            public void onError(String message) {
                next.run();
            }
        });
    }

    private void renderRootTitle(String title) {
        int[] pal = KollegenTheme.palette();
        mRoot.setBackgroundColor(pal[KollegenTheme.BG]);
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8));
        header.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        if (mOpenGroupId != null) {
            Button back = KollegenKit.tinyButton(requireContext(), "\u2190");
            back.setOnClickListener(v -> {
                mOpenGroupId = null;
                mPolling = false;
                sHandler.removeCallbacksAndMessages(null);
                buildList();
            });
            header.addView(back);
        }
        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTextColor(pal[KollegenTheme.TEXT]);
        t.setTextSize(16);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(KollegenKit.dp(requireContext(), 10), 0, 0, 0);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setLayoutParams(tp);
        header.addView(t);
        mRoot.addView(header);
    }

    private void buildList() {
        mRoot.removeAllViews();
        renderRootTitle(getString(R.string.kollegen_tab_groups));
        int[] pal = KollegenTheme.palette();

        LinearLayout createRow = new LinearLayout(requireContext());
        createRow.setOrientation(LinearLayout.HORIZONTAL);
        createRow.setGravity(Gravity.CENTER_VERTICAL);
        createRow.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 10), 0);

        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint(R.string.kollegen_group_name_hint);
        nameInput.setTextColor(pal[KollegenTheme.TEXT]);
        nameInput.setHintTextColor(pal[KollegenTheme.MUTED]);
        nameInput.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        nameInput.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nameInput.setLayoutParams(ip);
        createRow.addView(nameInput);

        Button create = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_group_create));
        create.setOnClickListener(v -> {
            Editable e = nameInput.getText();
            String name = e == null ? "" : e.toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.kollegen_group_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject body = new JSONObject();
            try { body.put("name", name); } catch (Exception ignored) {}
            KollegenApi.post("/api/profil/group/create", body, new KollegenApi.Callback() {
                @Override
                public void onResult(Object json) {
                    if (!isAdded()) return;
                    String gid = "";
                    if (json instanceof JSONObject) {
                        JSONObject d = ((JSONObject) json).optJSONObject("data");
                        if (d != null) gid = d.optString("id", "");
                    }
                    if (gid.isEmpty()) gid = ((JSONObject) json).optString("id", "");
                    Toast.makeText(requireContext(), R.string.kollegen_group_created, Toast.LENGTH_SHORT).show();
                    nameInput.setText("");
                    buildList();
                }

                @Override
                public void onError(String message) {
                    if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        createRow.addView(create);

        mRoot.addView(createRow);

        final LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        mRoot.addView(list);

        KollegenApi.get("/api/profil/groups", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded()) return;
                JSONArray arr = KollegenApi.optArray(json, "");
                list.removeAllViews();
                if (arr == null || arr.length() == 0) {
                    list.addView(KollegenKit.empty(requireContext(), getString(R.string.kollegen_no_groups)));
                    return;
                }
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject g = arr.optJSONObject(i);
                    if (g == null) continue;
                    list.addView(groupRow(g));
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View groupRow(final JSONObject g) {
        int[] pal = KollegenTheme.palette();
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 10));
        row.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.setMargins(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6), KollegenKit.dp(requireContext(), 10), 0);
        row.setLayoutParams(rp);

        TextView name = new TextView(requireContext());
        name.setText(g.optString("name", ""));
        name.setTextColor(pal[KollegenTheme.TEXT]);
        name.setTextSize(16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(name);

        TextView meta = new TextView(requireContext());
        int count = g.optInt("memberCount", 0);
        JSONObject last = g.optJSONObject("last");
        String lastText = last != null ? last.optString("text", "") : "";
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.kollegen_member_count, count));
        if (!lastText.isEmpty()) sb.append(" \u00b7 ").append(lastText);
        meta.setText(sb.toString());
        meta.setTextColor(pal[KollegenTheme.MUTED]);
        meta.setTextSize(13);
        row.addView(meta);

        row.setOnClickListener(v -> openGroup(g.optString("id", ""), g.optString("name", "")));
        return row;
    }

    private void openGroup(String groupId, String groupName) {
        mOpenGroupId = groupId;
        buildThread(groupName);
    }

    private void buildThread(String groupName) {
        mRoot.removeAllViews();
        renderRootTitle(groupName);
        int[] pal = KollegenTheme.palette();

        mThreadScroller = new ScrollView(requireContext());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        mThreadScroller.setLayoutParams(sp);
        mThreadBody = new LinearLayout(requireContext());
        mThreadBody.setOrientation(LinearLayout.VERTICAL);
        mThreadBody.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 10));
        mThreadScroller.addView(mThreadBody);
        mRoot.addView(mThreadScroller);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(KollegenKit.dp(requireContext(), 10), 0, KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 8));
        Button add = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_group_add));
        add.setOnClickListener(v -> promptAddMember());
        actions.addView(add);
        Button call = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_call));
        call.setOnClickListener(v -> KollegenCall.startGroup(requireContext(), mOpenGroupId, groupName));
        actions.addView(call);
        Button leave = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_group_leave));
        leave.setOnClickListener(v -> leaveGroup());
        actions.addView(leave);
        mRoot.addView(actions);

        LinearLayout inputRow = new LinearLayout(requireContext());
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 8));
        inputRow.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));

        final EditText input = new EditText(requireContext());
        input.setHint(R.string.kollegen_chat_hint);
        input.setTextColor(pal[KollegenTheme.TEXT]);
        input.setHintTextColor(pal[KollegenTheme.MUTED]);
        input.setBackground(null);
        LinearLayout.LayoutParams inp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        input.setLayoutParams(inp);
        inputRow.addView(input);

        Button send = KollegenKit.tinyButton(requireContext(), getString(R.string.kollegen_chat_send));
        send.setOnClickListener(v -> {
            Editable e = input.getText();
            String text = e == null ? "" : e.toString().trim();
            if (text.isEmpty()) return;
            sendGroupMessage(text, input);
        });
        inputRow.addView(send);

        mRoot.addView(inputRow);

        startPollingThread();
    }

    private void promptAddMember() {
        int[] pal = KollegenTheme.palette();
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.kollegen_group_code_hint);
        input.setTextColor(pal[KollegenTheme.TEXT]);
        input.setHintTextColor(pal[KollegenTheme.MUTED]);
        input.setBackground(KollegenTheme.rounded(pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        input.setPadding(KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6), KollegenKit.dp(requireContext(), 10), KollegenKit.dp(requireContext(), 6));
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.kollegen_group_add)
                .setView(input)
                .setPositiveButton(R.string.kollegen_ok, (d, w) -> {
                    Editable e = input.getText();
                    String code = e == null ? "" : e.toString().trim().toUpperCase();
                    if (code.isEmpty()) return;
                    JSONObject body = new JSONObject();
                    try {
                        body.put("groupId", mOpenGroupId);
                        body.put("memberId", code);
                    } catch (Exception ignored) {}
                    KollegenApi.post("/api/profil/group/add", body, new KollegenApi.Callback() {
                        @Override
                        public void onResult(Object json) {
                            if (isAdded()) Toast.makeText(requireContext(), R.string.kollegen_group_added, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.kollegen_close, null)
                .show();
    }

    private void leaveGroup() {
        JSONObject body = new JSONObject();
        try { body.put("groupId", mOpenGroupId); } catch (Exception ignored) {}
        KollegenApi.post("/api/profil/group/leave", body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded()) return;
                mOpenGroupId = null;
                mPolling = false;
                sHandler.removeCallbacksAndMessages(null);
                buildList();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendGroupMessage(final String text, final EditText input) {
        JSONObject body = new JSONObject();
        try {
            body.put("groupId", mOpenGroupId);
            body.put("text", text);
        } catch (Exception ignored) {}
        KollegenApi.post("/api/profil/group/send", body, new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (isAdded()) input.setText("");
                loadThread(true);
            }

            @Override
            public void onError(String message) {
                if (isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPollingThread() {
        if (mPolling || mOpenGroupId == null || !isAdded()) return;
        mPolling = true;
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mPolling) return;
                loadThread(true);
                sHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void loadThread(boolean incremental) {
        if (mOpenGroupId == null) return;
        KollegenApi.get("/api/profil/group/poll?groupId=" + KollegenApi.encode(mOpenGroupId) + "&sinceMsg=0&sinceSig=0", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (!isAdded() || mThreadBody == null) return;
                JSONArray msgs = json instanceof JSONObject ? ((JSONObject) json).optJSONArray("messages") : null;
                if (msgs == null || msgs.length() == 0) {
                    if (!incremental) mThreadBody.addView(KollegenKit.empty(requireContext(), getString(R.string.kollegen_no_messages)));
                    return;
                }
                for (int i = 0; i < msgs.length(); i++) {
                    JSONObject m = msgs.optJSONObject(i);
                    if (m != null) mThreadBody.addView(groupMessageRow(m));
                }
                mThreadScroller.post(() -> mThreadScroller.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onError(String message) {}
        });
    }

    private View groupMessageRow(final JSONObject m) {
        int[] pal = KollegenTheme.palette();
        String fromId = m.optString("from", "");
        String text = m.optString("text", "");
        boolean mine = sMyId != null && sMyId.equals(fromId);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.topMargin = KollegenKit.dp(requireContext(), 6);
        row.setLayoutParams(rp);

        TextView bubble = new TextView(requireContext());
        bubble.setText(text);
        bubble.setTextSize(15);
        bubble.setTextColor(pal[KollegenTheme.TEXT]);
        bubble.setPadding(KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 8), KollegenKit.dp(requireContext(), 12), KollegenKit.dp(requireContext(), 8));
        bubble.setBackground(KollegenTheme.rounded(mine ? pal[KollegenTheme.PANEL2] : pal[KollegenTheme.PANEL], pal[KollegenTheme.BORDER]));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.gravity = mine ? Gravity.END : Gravity.START;
        bubble.setLayoutParams(bp);
        row.addView(bubble);

        long ts = m.optLong("ts", 0);
        TextView time = new TextView(requireContext());
        time.setText(ts > 0 ? new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(new Date(ts)) : "");
        time.setTextColor(pal[KollegenTheme.MUTED]);
        time.setTextSize(11);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.gravity = mine ? Gravity.END : Gravity.START;
        time.setLayoutParams(tp);
        row.addView(time);
        return row;
    }
}