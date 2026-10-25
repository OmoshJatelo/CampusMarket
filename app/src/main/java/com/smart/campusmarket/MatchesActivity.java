package com.smart.campusmarket;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shows other students' posts that fit the user's own posts:
 * sellers matching your Wanted requests, and buyers wanting what you sell.
 * Posts from blocked sellers are left out.
 */
public class MatchesActivity extends AppCompatActivity {

    private final ListingRepository repository = new ListingRepository();
    private final ModerationRepository moderation = new ModerationRepository();

    private ProductAdapter adapter;
    private TextView emptyText;
    private View emptyAction;
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
        setContentView(R.layout.activity_matches);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.matchesMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        emptyText = findViewById(R.id.textMatchesEmpty);
        emptyAction = findViewById(R.id.btnMatchesEmptyPost);
        emptyAction.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, CreateListingActivity.class)));
        RecyclerView recycler = findViewById(R.id.recyclerMatches);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid == null) return;

        moderation.fetchBlockedIds(uid, new ListingRepository.Callback<Set<String>>() {
            @Override
            public void onResult(Set<String> blocked) {
                loadMatches(blocked);
            }

            @Override
            public void onError(Exception e) {
                loadMatches(new java.util.HashSet<String>());   // show matches even if this fails
            }
        });
    }

    private void loadMatches(Set<String> blocked) {
        repository.fetchFeed(new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> all) {
                List<Product> pool = ListingRepository.excludeOwners(
                        ListingRepository.activeOnly(all), blocked);
                List<MatchEngine.Match> matches = MatchEngine.findMatches(uid, pool);

                // One card per matching post, even if it fits several of mine
                Map<String, Product> unique = new LinkedHashMap<>();
                for (MatchEngine.Match m : matches) unique.put(m.other.getId(), m.other);

                adapter.setProducts(new ArrayList<>(unique.values()));
                emptyText.setVisibility(unique.isEmpty() ? View.VISIBLE : View.GONE);
                emptyAction.setVisibility(unique.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MatchesActivity.this,
                        getString(R.string.my_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }
}