package com.smart.campusmarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

/** Three short screens shown once, the first time a signed-in student opens the app. */
public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS = "campus_market_prefs";
    public static final String KEY_DONE = "onboarding_done";

    private static final int[] ICONS = {
            R.drawable.ic_nav_home, R.drawable.ic_nav_swap, R.drawable.ic_nav_offer};
    private static final int[] TITLES = {
            R.string.onb_title_1, R.string.onb_title_2, R.string.onb_title_3};
    private static final int[] BODIES = {
            R.string.onb_body_1, R.string.onb_body_2, R.string.onb_body_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboardingMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ViewPager2 pager = findViewById(R.id.onboardingPager);
        LinearLayout dots = findViewById(R.id.onboardingDots);
        MaterialButton next = findViewById(R.id.btnOnboardingNext);
        MaterialButton skip = findViewById(R.id.btnOnboardingSkip);

        pager.setAdapter(new Adapter());
        Dots.setup(dots, ICONS.length);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Dots.select(dots, position);
                boolean last = position == ICONS.length - 1;
                next.setText(last ? R.string.onb_start : R.string.onb_next);
                skip.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
            }
        });

        next.setOnClickListener(v -> {
            int position = pager.getCurrentItem();
            if (position >= ICONS.length - 1) finishOnboarding();
            else pager.setCurrentItem(position + 1, true);
        });
        skip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DONE, true).apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.page_onboarding, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            h.icon.setImageResource(ICONS[position]);
            h.title.setText(TITLES[position]);
            h.body.setText(BODIES[position]);
        }

        @Override
        public int getItemCount() {
            return ICONS.length;
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, body;

            VH(@NonNull View v) {
                super(v);
                icon = v.findViewById(R.id.pageIcon);
                title = v.findViewById(R.id.pageTitle);
                body = v.findViewById(R.id.pageBody);
            }
        }
    }
}