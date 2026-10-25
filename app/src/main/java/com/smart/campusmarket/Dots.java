package com.smart.campusmarket;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/** Little page-indicator dots for swipeable screens. */
public final class Dots {

    private Dots() { }

    public static void setup(LinearLayout container, int count) {
        container.removeAllViews();
        container.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
        Context ctx = container.getContext();
        int size = dp(ctx, 8);
        int margin = dp(ctx, 4);
        for (int i = 0; i < count; i++) {
            View dot = new View(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);
            container.addView(dot);
        }
        select(container, 0);
    }

    public static void select(LinearLayout container, int index) {
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setBackgroundResource(
                    i == index ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private static int dp(Context ctx, int value) {
        return Math.round(value * ctx.getResources().getDisplayMetrics().density);
    }
}