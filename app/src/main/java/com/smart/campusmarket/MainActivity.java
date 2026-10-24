package com.smart.campusmarket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_COLUMNS = 2;
    private static final long SPLASH_MIN_MS = 700;    // always show the logo briefly
    private static final long SPLASH_MAX_MS = 3500;   // never make the user wait longer than this

    private final ListingRepository repository = new ListingRepository();
    private final ModerationRepository moderation = new ModerationRepository();

    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private TextView emptyState;
    private View progressFeed;
    private BottomNavigationView bottomNav;

    private ListenerRegistration feedListener;
    private ListenerRegistration blockListener;
    private String uid;

    private List<Product> latestFeed = new ArrayList<>();
    private Set<String> blockedIds = new HashSet<>();
    private boolean feedReady = false;
    private boolean loadedOnce = false;
    private boolean expiryWarningShown = false;
    private boolean splashTimedOut = false;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Preloader: the splash stays up until the first listings load (or the time limit hits).
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        final long splashStart = SystemClock.uptimeMillis();
        splash.setKeepOnScreenCondition(() ->
                uid != null
                        && (SystemClock.uptimeMillis() - splashStart < SPLASH_MIN_MS
                        || (!feedReady && !splashTimedOut)));
        new Handler(Looper.getMainLooper()).postDelayed(() -> splashTimedOut = true, SPLASH_MAX_MS);

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

        // The bottom bar adds its own bottom inset, so only pad the top and sides here.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, GRID_COLUMNS));
        emptyState = findViewById(R.id.textEmptyState);
        progressFeed = findViewById(R.id.progressFeed);

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

        // Category chips: tap to filter, tap again to clear
        setupCategoryButton(R.id.btnBooks, "Books");
        setupCategoryButton(R.id.btnElectronics, "Electronics");
        setupCategoryButton(R.id.btnUtensils, "Utensils");
        setupCategoryButton(R.id.btnClothes, "Clothes");

        findViewById(R.id.btnSell).setOnClickListener(v ->
                startActivity(new Intent(this, CreateListingActivity.class)));
        findViewById(R.id.btnMore).setOnClickListener(this::showOverflowMenu);

        setupBottomNav();

        askForNotificationPermission();
        MatchCheckWorker.schedule(this);   // background match check (free, about every 15 min)
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;   // we were redirected to sign-in

        blockListener = moderation.listenBlocked(uid,
                new ListingRepository.Callback<List<ModerationRepository.BlockedUser>>() {
                    @Override
                    public void onResult(List<ModerationRepository.BlockedUser> list) {
                        blockedIds = ModerationRepository.ids(list);
                        renderFeed();
                    }

                    @Override
                    public void onError(Exception e) { /* keep showing everything */ }
                });

        feedListener = repository.listenFeed(new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> all) {
                latestFeed = all;
                feedReady = true;
                renderFeed();
            }

            @Override
            public void onError(Exception e) {
                progressFeed.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,
                        getString(R.string.main_load_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (feedListener != null) feedListener.remove();
        if (blockListener != null) blockListener.remove();
        feedListener = null;
        blockListener = null;
    }

    // ------------------------------------------------------------ navigation

    private void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        // Home is this screen. The other tabs open their own screens, and Home stays highlighted.
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navBundles) {
                startActivity(new Intent(this, BundlesActivity.class));
                return false;
            }
            if (id == R.id.navMatches) {
                startActivity(new Intent(this, MatchesActivity.class));
                return false;
            }
            if (id == R.id.navMine) {
                startActivity(new Intent(this, MyListingsActivity.class));
                return false;
            }
            return true;
        });
        bottomNav.setOnItemReselectedListener(item -> recyclerView.smoothScrollToPosition(0));
    }

    private void showOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.main_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menuBlocked) {
                startActivity(new Intent(this, BlockedUsersActivity.class));
                return true;
            }
            if (id == R.id.menuSignOut) {
                signOut();
                return true;
            }
            return false;
        });
        popup.show();
    }

    // ------------------------------------------------------------------ feed

    /** Active posts, minus anything from sellers the user has blocked. */
    private void renderFeed() {
        if (!feedReady) return;
        List<Product> active = ListingRepository.excludeOwners(
                ListingRepository.activeOnly(latestFeed), blockedIds);
        loadedOnce = true;
        progressFeed.setVisibility(View.GONE);
        adapter.setProducts(active);

        // Instant in-app matching (the background worker covers the app being closed)
        MatchNotifier.notifyNewMatches(this, uid, active);
        updateMatchesBadge(MatchEngine.findMatches(uid, active).size());

        warnAboutExpiringPosts(latestFeed);
    }

    private void updateMatchesBadge(int count) {
        if (count > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.navMatches);
            badge.setNumber(count);
            badge.setVisible(true);
        } else {
            bottomNav.removeBadge(R.id.navMatches);
        }
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
                .setAnchorView(bottomNav)
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