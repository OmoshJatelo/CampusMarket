package com.smart.campusmarket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final ListingRepository repository = new ListingRepository();

    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private TextView emptyState;
    private TextView btnMatches;

    private ListenerRegistration feedListener;
    private String uid;
    private boolean loadedOnce = false;
    private boolean expiryWarningShown = false;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gate: only signed-in, email-verified students get past this point.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !user.isEmailVerified()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        uid = user.getUid();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyState = findViewById(R.id.textEmptyState);
        btnMatches = findViewById(R.id.btnMatches);

        adapter = new ProductAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { updateEmptyState(); }
            @Override public void onItemRangeInserted(int start, int count) { updateEmptyState(); }
            @Override public void onItemRangeRemoved(int start, int count) { updateEmptyState(); }
        });

        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { adapter.filter(s.toString()); }
        });

        // Category buttons: tap to filter, tap again to clear
        setupCategoryButton(R.id.btnBooks, "Books");
        setupCategoryButton(R.id.btnElectronics, "Electronics");
        setupCategoryButton(R.id.btnUtensils, "Utensils");
        setupCategoryButton(R.id.btnClothes, "Clothes");

        findViewById(R.id.btnSell).setOnClickListener(v ->
                startActivity(new Intent(this, CreateListingActivity.class)));
        findViewById(R.id.btnMyListings).setOnClickListener(v ->
                startActivity(new Intent(this, MyListingsActivity.class)));
        btnMatches.setOnClickListener(v ->
                startActivity(new Intent(this, MatchesActivity.class)));
        findViewById(R.id.btnSignOut).setOnClickListener(v -> signOut());

        askForNotificationPermission();
        MatchCheckWorker.schedule(this);   // background match check (free, about every 15 min)
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;   // we were redirected to sign-in
        feedListener = repository.listenFeed(new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> all) {
                onFeedLoaded(all);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.main_load_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (feedListener != null) {
            feedListener.remove();
            feedListener = null;
        }
    }

    // ------------------------------------------------------------------ feed

    private void onFeedLoaded(List<Product> all) {
        List<Product> active = ListingRepository.activeOnly(all);
        loadedOnce = true;
        adapter.setProducts(active);

        // Instant in-app matching (the background worker covers the app being closed)
        MatchNotifier.notifyNewMatches(this, uid, active);
        int matchCount = MatchEngine.findMatches(uid, active).size();
        btnMatches.setText(getString(R.string.main_matches_count, matchCount));

        warnAboutExpiringPosts(all);
    }

    private void warnAboutExpiringPosts(List<Product> all) {
        if (expiryWarningShown) return;
        int expiring = 0;
        for (Product p : all) {
            if (uid.equals(p.getOwnerId()) && Product.STATUS_ACTIVE.equals(p.getStatus())
                    && p.daysLeft() <= AppConfig.EXPIRY_WARNING_DAYS) {
                expiring++;   // includes ones that already expired and can be renewed
            }
        }
        if (expiring == 0) return;
        expiryWarningShown = true;
        Snackbar.make(findViewById(R.id.main), getString(R.string.main_expiring, expiring),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.main_review, v ->
                        startActivity(new Intent(this, MyListingsActivity.class)))
                .show();
    }

    private void updateEmptyState() {
        if (!loadedOnce) return;
        emptyState.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // ---------------------------------------------------------------- actions

    private void setupCategoryButton(int buttonId, String category) {
        findViewById(buttonId).setOnClickListener(v -> {
            boolean alreadySelected = searchInput.getText().toString()
                    .trim().equalsIgnoreCase(category);
            searchInput.setText(alreadySelected ? "" : category);
            searchInput.setSelection(searchInput.getText().length());
        });
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void signOut() {
        MatchCheckWorker.cancel(this);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}