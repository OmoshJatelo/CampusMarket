package com.smart.campusmarket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * The owner's view of their own posts: mark sold, renew ("still available?"), delete, share.
 * Swipe right to mark sold, swipe left to delete. Both can be undone from the message that appears.
 */
public class MyListingsActivity extends AppCompatActivity {

    private final ListingRepository repository = new ListingRepository();
    private final List<Product> posts = new ArrayList<>();

    private Adapter adapter;
    private TextView emptyText;
    private View emptyAction;
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
        setContentView(R.layout.activity_my_listings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myListingsMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        emptyText = findViewById(R.id.textMyEmpty);
        emptyAction = findViewById(R.id.btnMyEmptyPost);
        emptyAction.setOnClickListener(v ->
                startActivity(new Intent(this, CreateListingActivity.class)));
        findViewById(R.id.btnCreateBundle).setOnClickListener(v ->
                startActivity(new Intent(this, CreateBundleActivity.class)));

        RecyclerView recycler = findViewById(R.id.recyclerMyListings);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(posts, new Actions() {
            @Override public void markSold(Product p) { markSoldWithUndo(p); }

            @Override public void renew(Product p) { repository.renew(p.getId(), doneHandler()); }

            @Override public void delete(Product p) { confirmDelete(p); }

            @Override public void share(Product p) {
                ShareHelper.sharePost(MyListingsActivity.this, p);
            }
        });
        recycler.setAdapter(adapter);
        attachSwipe(recycler);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;
        listener = repository.listenMine(uid, new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> value) {
                posts.clear();
                posts.addAll(value);
                adapter.notifyDataSetChanged();
                int empty = posts.isEmpty() ? View.VISIBLE : View.GONE;
                emptyText.setVisibility(empty);
                emptyAction.setVisibility(empty);
            }

            @Override
            public void onError(Exception e) {
                showError(e);
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

    // ------------------------------------------------------- actions with undo

    private void markSoldWithUndo(Product p) {
        repository.markSold(p.getId(), new ListingRepository.Done() {
            @Override
            public void onSuccess() {
                Snackbar.make(findViewById(R.id.myListingsMain),
                                p.isWanted() ? R.string.my_marked_found : R.string.my_marked_sold,
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, v ->
                                repository.reopen(p.getId(), doneHandler()))
                        .show();
            }

            @Override
            public void onError(Exception e) {
                showError(e);
            }
        });
    }

    private void deleteWithUndo(Product p) {
        repository.delete(p.getId(), new ListingRepository.Done() {
            @Override
            public void onSuccess() {
                Snackbar bar = Snackbar.make(findViewById(R.id.myListingsMain),
                        R.string.my_deleted, Snackbar.LENGTH_LONG);
                // Only an active post can be put back (the posting rules require an active status)
                if (Product.STATUS_ACTIVE.equals(p.getStatus())) {
                    bar.setAction(R.string.action_undo, v -> repository.addListing(p, doneHandler()));
                }
                bar.show();
            }

            @Override
            public void onError(Exception e) {
                showError(e);
            }
        });
    }

    private void confirmDelete(Product p) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.my_delete_title)
                .setMessage(R.string.my_delete_message)
                .setPositiveButton(R.string.my_delete, (d, w) -> deleteWithUndo(p))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private ListingRepository.Done doneHandler() {
        return new ListingRepository.Done() {
            @Override public void onSuccess() { /* the live listener refreshes the list */ }

            @Override public void onError(Exception e) { showError(e); }
        };
    }

    private void showError(Exception e) {
        Toast.makeText(this, getString(R.string.my_error, e.getMessage()), Toast.LENGTH_LONG).show();
    }

    // ------------------------------------------------------------------ swipe

    private void attachSwipe(RecyclerView recycler) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int position = vh.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position >= posts.size()) return;
                Product p = posts.get(position);

                adapter.notifyItemChanged(position);   // slide the row back; the live list updates after

                if (direction == ItemTouchHelper.RIGHT) {
                    if (!Product.STATUS_SOLD.equals(p.getStatus())) markSoldWithUndo(p);
                } else {
                    deleteWithUndo(p);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View row = vh.itemView;
                if (dX != 0) {
                    ColorDrawable background = new ColorDrawable(dX > 0 ? 0xFF2E7D32 : 0xFFC62828);
                    if (dX > 0) {
                        background.setBounds(row.getLeft(), row.getTop(),
                                row.getLeft() + (int) dX, row.getBottom());
                    } else {
                        background.setBounds(row.getRight() + (int) dX, row.getTop(),
                                row.getRight(), row.getBottom());
                    }
                    background.draw(c);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recycler);
    }

    // ---------------------------------------------------------------- adapter

    interface Actions {
        void markSold(Product p);
        void renew(Product p);
        void delete(Product p);
        void share(Product p);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<Product> items;
        private final Actions actions;

        Adapter(List<Product> items, Actions actions) {
            this.items = items;
            this.actions = actions;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_listing, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Product p = items.get(position);
            Context ctx = h.itemView.getContext();

            boolean sold = Product.STATUS_SOLD.equals(p.getStatus());
            boolean expired = p.isExpired();
            boolean expiringSoon = !expired && p.daysLeft() <= AppConfig.EXPIRY_WARNING_DAYS;

            h.title.setText(p.getTitle());
            h.price.setText(p.isWanted()
                    ? ctx.getString(R.string.product_budget_format, p.getPrice()) : p.getPrice());

            String status;
            if (sold) {
                status = ctx.getString(p.isWanted() ? R.string.my_status_found : R.string.my_status_sold);
            } else if (expired) {
                status = ctx.getString(R.string.my_status_expired);
            } else if (expiringSoon) {
                status = ctx.getString(R.string.my_status_expiring, p.daysLeft());
            } else {
                status = ctx.getString(R.string.my_status_active, p.daysLeft());
            }
            h.status.setText(status);

            Glide.with(h.thumb)
                    .load(CloudinaryUploader.thumbnail(p.getFirstImageUri()))
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .fallback(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)
                    .centerCrop()
                    .into(h.thumb);

            h.sold.setText(p.isWanted() ? R.string.my_mark_found : R.string.my_mark_sold);
            h.sold.setVisibility(sold ? View.GONE : View.VISIBLE);
            h.renew.setVisibility(!sold && (expired || expiringSoon) ? View.VISIBLE : View.GONE);

            h.sold.setOnClickListener(v -> actions.markSold(p));
            h.renew.setOnClickListener(v -> actions.renew(p));
            h.delete.setOnClickListener(v -> actions.delete(p));

            h.share.setVisibility(!sold && p.isActive() ? View.VISIBLE : View.GONE);
            h.share.setOnClickListener(v -> actions.share(p));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView thumb;
            final TextView title, price, status;
            final MaterialButton sold, renew, delete, share;

            VH(@NonNull View v) {
                super(v);
                thumb = v.findViewById(R.id.thumbMy);
                title = v.findViewById(R.id.textMyTitle);
                price = v.findViewById(R.id.textMyPrice);
                status = v.findViewById(R.id.textMyStatus);
                sold = v.findViewById(R.id.btnMySold);
                renew = v.findViewById(R.id.btnMyRenew);
                delete = v.findViewById(R.id.btnMyDelete);
                share = v.findViewById(R.id.btnMyShare);
            }
        }
    }
}