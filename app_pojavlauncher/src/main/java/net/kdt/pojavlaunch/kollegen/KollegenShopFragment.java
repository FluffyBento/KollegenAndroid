package net.kdt.pojavlaunch.kollegen;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.KollegenTheme;
import net.kdt.pojavlaunch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KollegenShopFragment extends Fragment {
    private RecyclerView mList;
    private View mHeader;
    private TextView mHeaderText;
    private Button mLoginButton;
    private String mPointsDisplay = "";
    private ShopAdapter mAdapter;

    public KollegenShopFragment() {
        super(R.layout.fragment_kollegen_shop);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList = view.findViewById(R.id.koll_shop_list);
        mHeader = view.findViewById(R.id.koll_shop_header);
        mHeaderText = view.findViewById(R.id.koll_shop_header_text);
        mLoginButton = view.findViewById(R.id.koll_shop_login);

        mList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new ShopAdapter();
        mList.setAdapter(mAdapter);

        mLoginButton.setOnClickListener(v -> KollegenSession.login(requireActivity(), this::reload));

        applyTheme(view);
        reload();
    }

    private void reload() {
        if (getView() == null) return;
        KollegenSession.check(requireActivity(), () -> {
            if (getView() == null) return;
            loadStore();
        });
    }

    private void loadStore() {
        KollegenApi.get("/api/profil/store", new KollegenApi.Callback() {
            @Override
            public void onResult(Object json) {
                if (getView() == null) return;
                if (!(json instanceof JSONObject)) return;
                JSONObject obj = (JSONObject) json;
                JSONArray catalog = obj.optJSONArray("catalog");
                List<JSONObject> items = new ArrayList<>();
                if (catalog != null) {
                    for (int i = 0; i < catalog.length(); i++) items.add(catalog.optJSONObject(i));
                }
                mAdapter.set(items);
                boolean needsAuth = obj.has("needsAuth") && obj.optBoolean("needsAuth", false);
                boolean loggedIn = KollegenSession.isLoggedIn();
                if (!loggedIn || needsAuth) {
                    mPointsDisplay = getString(R.string.kollegen_shop_login_hint);
                } else {
                    mPointsDisplay = getString(R.string.kollegen_points_short, obj.optInt("points", 0));
                }
                bindHeader(loggedIn);
            }

            @Override
            public void onError(String message) {
                if (getView() == null) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindHeader(boolean loggedIn) {
        if (mHeader == null) return;
        mHeaderText.setText(mPointsDisplay);
        mHeaderText.setTextColor(KollegenTheme.color(KollegenTheme.ACCENT));
        mHeader.setVisibility(View.VISIBLE);
        if (mLoginButton != null) {
            mLoginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
            mLoginButton.setBackground(KollegenTheme.buttonBackground(KollegenTheme.ACCENT, KollegenTheme.ACCENT2));
            mLoginButton.setTextColor(KollegenTheme.color(KollegenTheme.BG));
        }
    }

    public void applyTheme(View view) {
        view.setBackgroundColor(KollegenTheme.color(KollegenTheme.BG));
        bindHeader(KollegenSession.isLoggedIn());
    }

    private class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.Holder> {
        private final List<JSONObject> mItems = new ArrayList<>();

        public void set(List<JSONObject> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kollegen_shop, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            final JSONObject item = mItems.get(position);
            holder.mName.setText(item.optString("name", ""));
            holder.mDesc.setText(item.optString("desc", ""));
            String rarity = item.optString("rarity", "");
            holder.mRarity.setText(rarity.toUpperCase());
            int price = item.optInt("price", 0);
            boolean owned = item.optBoolean("owned", false);
            boolean equipped = item.optBoolean("equippedCategory", false);
            String itemId = item.optString("id", "");

            boolean loggedIn = KollegenSession.isLoggedIn();
            holder.mPrice.setText(owned
                    ? getString(equipped ? R.string.kollegen_equipped_tag : R.string.kollegen_owned_tag)
                    : getString(R.string.kollegen_price, price));

            int[] pal = KollegenTheme.palette();
            int rarityColor = rarityColor(rarity);
            holder.mName.setTextColor(pal[KollegenTheme.TEXT]);
            holder.mDesc.setTextColor(pal[KollegenTheme.MUTED]);
            holder.mRarity.setTextColor(rarityColor);
            holder.mPrice.setTextColor(pal[KollegenTheme.ACCENT]);

            holder.mPreview.removeAllViews();
            View preview = KollegenKit.storePreview(requireContext(), item, dp(64));
            holder.mPreview.addView(preview);

            if (owned) {
                holder.mAction.setVisibility(View.GONE);
            } else if (!loggedIn) {
                holder.mAction.setVisibility(View.GONE);
            } else {
                holder.mAction.setVisibility(View.VISIBLE);
                holder.mAction.setText(R.string.kollegen_buy);
                holder.mAction.setBackground(KollegenTheme.buttonBackground(KollegenTheme.PANEL2, KollegenTheme.ACCENT2));
                holder.mAction.setTextColor(pal[KollegenTheme.TEXT]);
                holder.mAction.setOnClickListener(v -> buyItem(itemId, holder));
            }
        }

        private void buyItem(String itemId, Holder holder) {
            JSONObject body = new JSONObject();
            try {
                body.put("item_id", itemId);
            } catch (Exception e) {
                return;
            }
            KollegenApi.post("/api/profil/buy", body, new KollegenApi.Callback() {
                @Override
                public void onResult(Object json) {
                    if (getActivity() != null && isAdded()) {
                        Toast.makeText(requireContext(), R.string.kollegen_bought, Toast.LENGTH_SHORT).show();
                        reload();
                    }
                }

                @Override
                public void onError(String message) {
                    if (getActivity() != null && isAdded()) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private int rarityColor(String rarity) {
            switch (rarity) {
                case "legendary": return net.kdt.pojavlaunch.KollegenTheme.color(KollegenTheme.ACCENT);
                case "epic": return Color.parseColor("#b054d8");
                case "rare": return Color.parseColor("#39d7ff");
                default: return net.kdt.pojavlaunch.KollegenTheme.color(KollegenTheme.MUTED);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private class Holder extends RecyclerView.ViewHolder {
            final TextView mName;
            final TextView mDesc;
            final TextView mRarity;
            final TextView mPrice;
            final Button mAction;
            final android.widget.FrameLayout mPreview;

            Holder(@NonNull View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.koll_shop_name);
                mDesc = itemView.findViewById(R.id.koll_shop_desc);
                mRarity = itemView.findViewById(R.id.koll_shop_rarity);
                mPrice = itemView.findViewById(R.id.koll_shop_price);
                mAction = itemView.findViewById(R.id.koll_shop_action);
                mPreview = itemView.findViewById(R.id.koll_shop_preview);
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}