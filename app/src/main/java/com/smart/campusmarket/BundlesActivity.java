package com.smart.campusmarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Browse all upcoming move-out sales (hiding ones from blocked sellers). */
public class BundlesActivity extends AppCompatActivity {

    private final BundleRepository repository = new BundleRepository();
    private final ModerationRepository moderation = new ModerationRepository();
    private final List<MoveOutBundle> visible = new ArrayList<>();

    private List<MoveOutBundle> latest = new ArrayList<>();
    private Set<String> blocked = new HashSet<>();
    private Adapter adapter;
    private TextView emptyText;
    private View emptyAction;
    private ListenerRegistration bundleListener, blockListener;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bundles);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bundlesMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        emptyText = findViewById(R.id.textBundlesEmpty);
        emptyAction = findViewById(R.id.btnBundlesEmptyCreate);
        emptyAction.setOnClickListener(v ->
                startActivity(new Intent(this, CreateBundleActivity.class)));
        RecyclerView recycler = findViewById(R.id.recyclerBundles);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(visible);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;

        blockListener = moderation.listenBlocked(uid,
                new ListingRepository.Callback<List<ModerationRepository.BlockedUser>>() {
                    @Override
                    public void onResult(List<ModerationRepository.BlockedUser> list) {
                        blocked = ModerationRepository.ids(list);
                        render();
                    }

                    @Override
                    public void onError(Exception e) { /* keep showing everything */ }
                });

        bundleListener = repository.listenActive(new ListingRepository.Callback<List<MoveOutBundle>>() {
            @Override
            public void onResult(List<MoveOutBundle> list) {
                latest = list;
                render();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BundlesActivity.this,
                        getString(R.string.my_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bundleListener != null) bundleListener.remove();
        if (blockListener != null) blockListener.remove();
        bundleListener = null;
        blockListener = null;
    }

    private void render() {
        visible.clear();
        for (MoveOutBundle b : latest) {
            if (b.getOwnerId() == null || !blocked.contains(b.getOwnerId())) visible.add(b);
        }
        adapter.notifyDataSetChanged();
        emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        emptyAction.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ---------------------------------------------------------------- adapter

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<MoveOutBundle> items;

        Adapter(List<MoveOutBundle> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bundle, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            MoveOutBundle b = items.get(position);
            Context ctx = h.itemView.getContext();
            h.title.setText(b.getTitle());
            h.summary.setText(ctx.getString(R.string.bundle_summary,
                    b.getItemCount(), b.getPickupDateText(), b.getPickupLocation()));
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, BundleDetailActivity.class);
                intent.putExtra("extra_bundle", b);
                ctx.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title, summary;

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.textBundleTitle);
                summary = v.findViewById(R.id.textBundleSummary);
            }
        }
    }
}