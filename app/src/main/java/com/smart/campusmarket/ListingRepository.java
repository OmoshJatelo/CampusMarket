package com.smart.campusmarket;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All Firestore access for posts lives here, so the screens never touch Firebase directly.
 *
 * Deliberately avoids queries that need a composite index: the feed is "newest N posts" and
 * the active/expired filtering happens on the phone.
 */
public class ListingRepository {

    public interface Callback<T> {
        void onResult(T value);
        void onError(Exception e);
    }

    public interface Done {
        void onSuccess();
        void onError(Exception e);
    }

    private static final String COLLECTION = "listings";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ------------------------------------------------------------------ reads

    private static Query feedQuery(FirebaseFirestore db) {
        return db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(AppConfig.MAX_FEED_LISTINGS);
    }

    /** Live feed of the newest posts (all statuses). Remove the registration in onStop. */
    public ListenerRegistration listenFeed(Callback<List<Product>> cb) {
        return feedQuery(db).addSnapshotListener((snap, error) -> {
            if (error != null) {
                cb.onError(error);
                return;
            }
            if (snap != null) cb.onResult(toProducts(snap));
        });
    }

    /** One-off fetch of the feed. */
    public void fetchFeed(Callback<List<Product>> cb) {
        feedQuery(db).get()
                .addOnSuccessListener(snap -> cb.onResult(toProducts(snap)))
                .addOnFailureListener(cb::onError);
    }

    /** Blocking fetch for background workers. Never call this on the main thread. */
    public static List<Product> fetchFeedBlocking() throws Exception {
        QuerySnapshot snap = Tasks.await(feedQuery(FirebaseFirestore.getInstance()).get());
        return toProducts(snap);
    }

    /** Live list of one user's own posts, newest first. */
    public ListenerRegistration listenMine(String uid, Callback<List<Product>> cb) {
        return db.collection(COLLECTION).whereEqualTo("ownerId", uid)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        cb.onError(error);
                        return;
                    }
                    if (snap == null) return;
                    List<Product> list = toProducts(snap);
                    Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    cb.onResult(list);
                });
    }

    // ----------------------------------------------------------------- writes

    public void addListing(Product p, Done cb) {
        db.collection(COLLECTION).document(p.getId()).set(toMap(p))
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void markSold(String id, Done cb) {
        db.collection(COLLECTION).document(id).update("status", Product.STATUS_SOLD)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /** Relists a post for another LISTING_DAYS days. */
    public void renew(String id, Done cb) {
        long newExpiry = System.currentTimeMillis()
                + AppConfig.LISTING_DAYS * 24L * 60L * 60L * 1000L;
        Map<String, Object> changes = new HashMap<>();
        changes.put("status", Product.STATUS_ACTIVE);
        changes.put("expiresAt", newExpiry);
        db.collection(COLLECTION).document(id).update(changes)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void delete(String id, Done cb) {
        db.collection(COLLECTION).document(id).delete()
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // ---------------------------------------------------------------- mapping

    public static List<Product> activeOnly(List<Product> all) {
        List<Product> out = new ArrayList<>();
        for (Product p : all) if (p.isActive()) out.add(p);
        return out;
    }

    private static List<Product> toProducts(QuerySnapshot snap) {
        List<Product> out = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) {
            Product p = fromDoc(d);
            if (p != null) out.add(p);
        }
        return out;
    }

    private static String str(DocumentSnapshot d, String field, String fallback) {
        String v = d.getString(field);
        return v == null ? fallback : v;
    }

    @SuppressWarnings("unchecked")
    private static Product fromDoc(DocumentSnapshot d) {
        String title = d.getString("title");
        Long price = d.getLong("price");
        Long createdAt = d.getLong("createdAt");
        if (title == null || price == null || createdAt == null) return null;   // skip malformed docs

        Long expiresAt = d.getLong("expiresAt");
        Boolean negotiable = d.getBoolean("negotiable");
        List<String> images = (List<String>) d.get("imageUris");

        return new Product(
                d.getId(),
                str(d, "type", Product.TYPE_SALE),
                title,
                price,
                negotiable != null && negotiable,
                str(d, "category", ""),
                str(d, "condition", ""),
                str(d, "location", ""),
                str(d, "description", ""),
                str(d, "phone", ""),
                images,
                createdAt,
                expiresAt == null ? 0L : expiresAt,
                d.getString("status"),
                d.getString("ownerId"));
    }

    private static Map<String, Object> toMap(Product p) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", p.getType());
        m.put("title", p.getTitle());
        m.put("price", p.getPriceValue());
        m.put("negotiable", p.isNegotiable());
        m.put("category", p.getCategory());
        m.put("condition", p.getCondition());
        m.put("location", p.getLocation());
        m.put("description", p.getDescription());
        m.put("phone", p.getPhone());
        m.put("imageUris", new ArrayList<>(p.getImageUris()));
        m.put("createdAt", p.getCreatedAt());
        m.put("expiresAt", p.getExpiresAt());
        m.put("status", p.getStatus());
        m.put("ownerId", p.getOwnerId());
        return m;
    }
}