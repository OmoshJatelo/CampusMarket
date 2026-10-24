package com.smart.campusmarket;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** One move-out sale: pickup details, total price, its items, contact and share buttons. */
public class BundleDetailActivity extends AppCompatActivity {

    private final BundleRepository repository = new BundleRepository();

    private MoveOutBundle bundle;
    private ProductAdapter adapter;
    private TextView totalText;
    private ListenerRegistration listener;
    private List<Product> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = IntentCompat.getSerializableExtra(getIntent(), "extra_bundle", MoveOutBundle.class);
        if (bundle == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bundle_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bundleDetailMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ((TextView) findViewById(R.id.textBundleDetailTitle)).setText(bundle.getTitle());
        ((TextView) findViewById(R.id.textBundleDetailPickup)).setText(getString(
                R.string.bundle_detail_pickup, bundle.getPickupLocation(), bundle.getPickupDateText()));

        TextView notes = findViewById(R.id.textBundleDetailNotes);
        if (bundle.getDescription() != null && !bundle.getDescription().isEmpty()) {
            notes.setText(bundle.getDescription());
            notes.setVisibility(View.VISIBLE);
        }
        totalText = findViewById(R.id.textBundleDetailTotal);

        RecyclerView recycler = findViewById(R.id.recyclerBundleItems);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBundleWhatsApp).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getWhatsAppUrl())));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.detail_error_open, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btnBundleShare).setOnClickListener(v ->
                ShareHelper.shareBundle(this, bundle, items));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(bundle.getOwnerId())) {
            View close = findViewById(R.id.btnBundleClose);
            close.setVisibility(View.VISIBLE);
            close.setOnClickListener(v -> confirmClose());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bundle == null) return;
        listener = repository.listenItems(bundle.getId(), new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> all) {
                items = ListingRepository.activeOnly(all);
                adapter.setProducts(items);
                long total = 0;
                for (Product p : items) total += p.getPriceValue();
                totalText.setText(getString(R.string.bundle_detail_total, items.size(),
                        String.format(Locale.US, "KSh %,d", total)));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BundleDetailActivity.this,
                        getString(R.string.my_error, e.getMessage()), Toast.LENGTH_LONG).show();
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

    private void confirmClose() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.bundle_close_title)
                .setMessage(R.string.bundle_close_message)
                .setPositiveButton(R.string.bundle_close, (d, w) ->
                        repository.close(bundle.getId(), new ListingRepository.Done() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(BundleDetailActivity.this, R.string.bundle_closed,
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(BundleDetailActivity.this,
                                        getString(R.string.my_error, e.getMessage()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}