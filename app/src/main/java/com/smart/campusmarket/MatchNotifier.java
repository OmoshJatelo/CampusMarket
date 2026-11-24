package com.smart.campusmarket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shows one notification per NEW match and remembers which ones it has already shown. */
public final class MatchNotifier {

    private static final String CHANNEL_ID = "matches";
    private static final String PREFS = "campus_market_prefs";

    private MatchNotifier() { }

    /**
     * Compares the user's posts against everyone else's and notifies about pairings not seen before.
     * Returns how many notifications were shown. Safe to call from a worker thread.
     */
    @SuppressLint("MissingPermission")   // permission is checked just below
    public static int notifyNewMatches(Context context, String uid, List<Product> activePosts) {
        if (uid == null) return 0;
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return 0;   // do not mark as seen, so the user is told once they allow notifications
        }

        List<MatchEngine.Match> matches = MatchEngine.findMatches(uid, activePosts);

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefKey = "notified_" + uid;
        Set<String> seen = new HashSet<>(prefs.getStringSet(prefKey, new HashSet<String>()));
        Set<String> originalSeen = new HashSet<>(seen); // keep copy to detect changes

        // Prune stale keys: keep only those that are still current matches
        Set<String> currentKeys = new HashSet<>();
        for (MatchEngine.Match m : matches) {
            currentKeys.add(m.key());
        }
        seen = pruneSeen(seen, currentKeys);

        ensureChannel(context);
        int shown = 0;
        for (MatchEngine.Match m : matches) {
            if (!seen.add(m.key())) continue;   // already notified
            NotificationManagerCompat.from(context).notify(m.key().hashCode(), build(context, m));
            shown++;
        }
        // Write back whenever the set differs from what was originally stored
        if (!seen.equals(originalSeen)) {
            prefs.edit().putStringSet(prefKey, seen).apply();
        }
        return shown;
    }

    /**
     * Returns a NEW set containing only entries of {@code seen} that are also in {@code currentKeys}.
     * Never mutates the inputs; returns an empty set for null input.
     */
    static Set<String> pruneSeen(Set<String> seen, Set<String> currentKeys) {
        if (seen == null || currentKeys == null) return new HashSet<>();
        Set<String> result = new HashSet<>();
        for (String key : seen) {
            if (currentKeys.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }

    private static android.app.Notification build(Context context, MatchEngine.Match m) {
        String title = context.getString(m.mine.isWanted()
                ? R.string.notif_title_wanted : R.string.notif_title_sale);
        String text = context.getString(R.string.notif_text, m.other.getTitle(), m.other.getPrice());

        Intent open = new Intent(context, MatchesActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}