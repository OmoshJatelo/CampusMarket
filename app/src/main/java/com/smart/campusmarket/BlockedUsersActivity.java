package com.smart.campusmarket;

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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/** Lets the user see and undo their blocks. */
public class BlockedUsersActivity extends AppCompatActivity {

    private final ModerationRepository moderation = new ModerationRepository();
    private final List<ModerationRepository.BlockedUser> blocked = new ArrayList<>();

    private Adapter adapter;
    private TextView emptyText;
    private ListenerRegistration listener;
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
        setContentView(R.layout.activity_blocked_users);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.blockedMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        emptyText = findViewById(R.id.textBlockedEmpty);
        RecyclerView recycler = findViewById(R.id.recyclerBlocked);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(blocked, entry ->
                moderation.unblock(uid, entry.id, new ListingRepository.Done() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(BlockedUsersActivity.this, R.string.blocked_unblocked,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(BlockedUsersActivity.this,
                                getString(R.string.my_error, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                }));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;
        listener = moderation.listenBlocked(uid,
                new ListingRepository.Callback<List<ModerationRepository.BlockedUser>>() {
                    @Override
                    public void onResult(List<ModerationRepository.BlockedUser> list) {
                        blocked.clear();
                        blocked.addAll(list);
                        adapter.notifyDataSetChanged();
                        emptyText.setVisibility(blocked.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(BlockedUsersActivity.this,
                                getString(R.string.my_error, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    // ---------------------------------------------------------------- adapter

    interface OnUnblock {
        void unblock(ModerationRepository.BlockedUser entry);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<ModerationRepository.BlockedUser> items;
        private final OnUnblock onUnblock;

        Adapter(List<ModerationRepository.BlockedUser> items, OnUnblock onUnblock) {
            this.items = items;
            this.onUnblock = onUnblock;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_blocked, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ModerationRepository.BlockedUser entry = items.get(position);
            h.label.setText(h.itemView.getContext().getString(R.string.blocked_label, entry.label));
            h.unblock.setOnClickListener(v -> onUnblock.unblock(entry));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView label;
            final MaterialButton unblock;

            VH(@NonNull View v) {
                super(v);
                label = v.findViewById(R.id.textBlockedLabel);
                unblock = v.findViewById(R.id.btnUnblock);
            }
        }
    }
}