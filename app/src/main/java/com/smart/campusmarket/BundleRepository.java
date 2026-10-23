package com.smart.campusmarket;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Firestore access for move-out bundles. Items link to a bundle through their bundleId field. */
public class BundleRepository {

    private static final String COLLECTION = "bundles";
    private static final String LISTINGS = "listings";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Live list of bundles whose pickup day has not passed. */
    public ListenerRegistration listenActive(ListingRepository.Callback<List<MoveOutBundle>> cb) {
        return db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        cb.onError(error);
                        return;
                    }
                    if (snap == null) return;
                    List<MoveOutBundle> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        MoveOutBundle b = fromDoc(d);
                        if (b != null && b.isActive()) out.add(b);
                    }
                    cb.onResult(out);
                });
    }

    /** Live list of the posts inside one bundle. */
    public ListenerRegistration listenItems(String bundleId,
                                            ListingRepository.Callback<List<Product>> cb) {
        return db.collection(LISTINGS).whereEqualTo("bundleId", bundleId)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        cb.onError(error);
                        return;
                    }
                    if (snap != null) cb.onResult(ListingRepository.toProducts(snap));
                });
    }

    /** Creates the bundle and links the chosen posts to it in one atomic write. */
    public void create(MoveOutBundle b, List<String> listingIds, ListingRepository.Done cb) {
        WriteBatch batch = db.batch();
        batch.set(db.collection(COLLECTION).document(b.getId()), toMap(b));
        for (String listingId : listingIds) {
            batch.update(db.collection(LISTINGS).document(listingId), "bundleId", b.getId());
        }
        batch.commit()
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /** Deletes the bundle and unlinks its posts (the posts themselves stay). */
    public void close(String bundleId, ListingRepository.Done cb) {
        db.collection(LISTINGS).whereEqualTo("bundleId", bundleId).get()
                .addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        batch.update(d.getReference(), "bundleId", FieldValue.delete());
                    }
                    batch.delete(db.collection(COLLECTION).document(bundleId));
                    batch.commit()
                            .addOnSuccessListener(v -> cb.onSuccess())
                            .addOnFailureListener(cb::onError);
                })
                .addOnFailureListener(cb::onError);
    }

    // ---------------------------------------------------------------- mapping

    private static MoveOutBundle fromDoc(DocumentSnapshot d) {
        String title = d.getString("title");
        Long pickupDate = d.getLong("pickupDate");
        Long createdAt = d.getLong("createdAt");
        if (title == null || pickupDate == null || createdAt == null) return null;
        Long count = d.getLong("itemCount");
        return new MoveOutBundle(
                d.getId(),
                d.getString("ownerId"),
                title,
                orEmpty(d.getString("description")),
                orEmpty(d.getString("pickupLocation")),
                orEmpty(d.getString("phone")),
                pickupDate,
                createdAt,
                count == null ? 0 : count.intValue());
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    private static Map<String, Object> toMap(MoveOutBundle b) {
        Map<String, Object> m = new HashMap<>();
        m.put("ownerId", b.getOwnerId());
        m.put("title", b.getTitle());
        m.put("description", b.getDescription());
        m.put("pickupLocation", b.getPickupLocation());
        m.put("phone", b.getPhone());
        m.put("pickupDate", b.getPickupDate());
        m.put("createdAt", b.getCreatedAt());
        m.put("itemCount", b.getItemCount());
        return m;
    }
}