package com.smart.campusmarket;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reports and blocks.
 *  - Reports go to the "reports" collection. Only you can read them (in the Firebase console).
 *  - Blocks live under users/{uid}/blocked/{sellerId}, readable only by the person who blocked.
 */
public class ModerationRepository {

    public static final class BlockedUser {
        public final String id;      // the blocked seller's user id
        public final String label;   // e.g. the title of the post they were blocked from

        BlockedUser(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static CollectionReference blockedRef(FirebaseFirestore db, String uid) {
        return db.collection("users").document(uid).collection("blocked");
    }

    // ----------------------------------------------------------------- reports

    /** One report per user per post: the document id is listingId_reporterId. */
    public void report(String listingId, String sellerId, String listingTitle, String reason,
                       ListingRepository.Done cb) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            cb.onError(new IllegalStateException("Not signed in"));
            return;
        }
        String uid = user.getUid();
        Map<String, Object> m = new HashMap<>();
        m.put("reporterId", uid);
        m.put("listingId", listingId);
        m.put("sellerId", sellerId);
        m.put("listingTitle", listingTitle);
        m.put("reason", reason);
        m.put("status", "OPEN");
        m.put("createdAt", System.currentTimeMillis());

        db.collection("reports").document(listingId + "_" + uid).set(m)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    // ------------------------------------------------------------------ blocks

    public void block(String uid, String blockedId, String label, ListingRepository.Done cb) {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("createdAt", System.currentTimeMillis());
        blockedRef(db, uid).document(blockedId).set(m)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void unblock(String uid, String blockedId, ListingRepository.Done cb) {
        blockedRef(db, uid).document(blockedId).delete()
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public ListenerRegistration listenBlocked(String uid,
                                              ListingRepository.Callback<List<BlockedUser>> cb) {
        return blockedRef(db, uid).addSnapshotListener((snap, error) -> {
            if (error != null) {
                cb.onError(error);
                return;
            }
            if (snap != null) cb.onResult(toBlockedUsers(snap));
        });
    }

    public void fetchBlockedIds(String uid, ListingRepository.Callback<Set<String>> cb) {
        blockedRef(db, uid).get()
                .addOnSuccessListener(snap -> cb.onResult(ids(toBlockedUsers(snap))))
                .addOnFailureListener(cb::onError);
    }

    /** Blocking fetch for background workers. Never call this on the main thread. */
    public static Set<String> fetchBlockedIdsBlocking(String uid) throws Exception {
        QuerySnapshot snap = Tasks.await(blockedRef(FirebaseFirestore.getInstance(), uid).get());
        return ids(toBlockedUsers(snap));
    }

    public static Set<String> ids(List<BlockedUser> blocked) {
        Set<String> out = new HashSet<>();
        for (BlockedUser b : blocked) out.add(b.id);
        return out;
    }

    private static List<BlockedUser> toBlockedUsers(QuerySnapshot snap) {
        List<BlockedUser> out = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) {
            String label = d.getString("label");
            out.add(new BlockedUser(d.getId(), label == null ? "" : label));
        }
        return out;
    }
}