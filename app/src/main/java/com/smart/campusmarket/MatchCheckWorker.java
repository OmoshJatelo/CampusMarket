package com.smart.campusmarket;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Runs in the background about every 15 minutes (Android's minimum) while the user is signed in.
 * This is the free alternative to push notifications from a server: it checks for new matches
 * and shows a local notification. Battery savers on some phones can delay it.
 */
public class MatchCheckWorker extends Worker {

    private static final String WORK_NAME = "match_check";

    public MatchCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();   // signed out: nothing to do

        try {
            List<Product> all = ListingRepository.fetchFeedBlocking();
            Set<String> blocked = ModerationRepository.fetchBlockedIdsBlocking(user.getUid());
            MatchNotifier.notifyNewMatches(getApplicationContext(), user.getUid(),
                    ListingRepository.excludeOwners(ListingRepository.activeOnly(all), blocked));
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                MatchCheckWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}