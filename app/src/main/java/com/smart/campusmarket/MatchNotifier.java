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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shows one notification per NEW match and remembers which ones it has already shown. */
public final class MatchNotifier {

    private static final String CHANNEL_ID = "matches";
    private static final String PREFS = "campus_market_prefs";

    /** Two listing lifetimes: an entry expires after its posts would have expired. */
    private static final long SEEN_MAX_AGE_MS = 2L * AppConfig.LISTING_DAYS * 24L * 60L * 60L * 1000L;

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
        Set<String> stored = new HashSet<>(prefs.getStringSet(prefKey, new HashSet<String>()));

        long now = System.currentTimeMillis();
        Map<String, Long> seenMap = expireSeen(parseSeen(stored, now), now, SEEN_MAX_AGE_MS);

        boolean channelEnsured = false;
        int shown = 0;

        for (MatchEngine.Match m : matches) {
            String key = m.key();
            if (seenMap.containsKey(key)) continue;   // already notified
            if (!channelEnsured) {
                ensureChannel(context);
                channelEnsured = true;
            }
            NotificationManagerCompat.from(context).notify(key.hashCode(), build(context, m));
            seenMap.put(key, now);
            shown++;
        }

        Set<String> serialized = serializeSeen(seenMap);
        if (!serialized.equals(stored)) {
            prefs.edit().putStringSet(prefKey, serialized).apply();
        }
        return shown;
    }

    /**
     * Parses a stored StringSet of seen entries into a map of key -> timestamp.
     * New-format entries: "key@timestamp"
     * Legacy entries (no '@'): treated as key with timestamp = now
     * Entries with unparsable suffix: treated as key (before last '@') with timestamp = now
     * Empty keys are ignored.
     */
    static Map<String, Long> parseSeen(Set<String> stored, long now) {
        Map<String, Long> result = new HashMap<>();
        if (stored == null || stored.isEmpty()) return result;

        for (String entry : stored) {
            if (entry == null || entry.isEmpty()) continue;
            int lastAt = entry.lastIndexOf('@');
            if (lastAt < 0) {
                // Legacy entry (no '@') - stamp with now
                result.put(entry, now);
            } else if (lastAt == 0) {
                // '@' is first character - empty key, ignore
                continue;
            } else {
                String key = entry.substring(0, lastAt);
                if (key.isEmpty() || key.trim().isEmpty()) continue;
                String suffix = entry.substring(lastAt + 1);
                try {
                    long timestamp = Long.parseLong(suffix);
                    result.put(key, timestamp);
                } catch (NumberFormatException e) {
                    // Unparsable suffix - treat as legacy, stamp with now
                    result.put(key, now);
                }
            }
        }
        return result;
    }

    /**
     * Returns a NEW map without entries older than maxAgeMs.
     * An entry whose age equals maxAgeMs is kept.
     * Never mutates the input; null input -> empty map.
     */
    static Map<String, Long> expireSeen(Map<String, Long> seen, long now, long maxAgeMs) {
        Map<String, Long> result = new HashMap<>();
        if (seen == null || seen.isEmpty()) return result;

        for (Map.Entry<String, Long> entry : seen.entrySet()) {
            long age = now - entry.getValue();
            if (age <= maxAgeMs) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Serializes a map of key -> timestamp into a Set<String> of "key@timestamp".
     */
    static Set<String> serializeSeen(Map<String, Long> seen) {
        Set<String> result = new HashSet<>();
        if (seen == null || seen.isEmpty()) return result;

        for (Map.Entry<String, Long> entry : seen.entrySet()) {
            result.add(entry.getKey() + "@" + entry.getValue());
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