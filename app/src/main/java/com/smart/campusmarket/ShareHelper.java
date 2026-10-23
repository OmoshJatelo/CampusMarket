package com.smart.campusmarket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Locale;

/** Builds a ready-to-paste message and opens the Android share sheet (WhatsApp is one tap away). */
public final class ShareHelper {

    private static final int MAX_BUNDLE_LINES = 8;

    private ShareHelper() { }

    public static void sharePost(Context ctx, Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getString(p.isWanted() ? R.string.share_wanted : R.string.share_sale,
                p.getTitle(), p.getPrice()));
        if (!p.isWanted() && p.isNegotiable()) {
            sb.append(' ').append(ctx.getString(R.string.share_negotiable));
        }
        if (p.getLocation() != null && !p.getLocation().isEmpty()) {
            sb.append('\n').append(ctx.getString(R.string.share_pickup, p.getLocation()));
        }
        String photo = p.getFirstImageUri();
        if (photo != null && photo.startsWith("http")) {
            sb.append('\n').append(ctx.getString(R.string.share_photo, photo));
        }
        sb.append("\n\n").append(ctx.getString(R.string.share_footer));
        send(ctx, sb.toString());
    }

    public static void shareBundle(Context ctx, MoveOutBundle b, List<Product> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getString(R.string.share_bundle_title, b.getTitle())).append('\n');

        long total = 0;
        for (Product p : items) total += p.getPriceValue();

        int shown = 0;
        for (Product p : items) {
            if (shown++ >= MAX_BUNDLE_LINES) break;
            sb.append(ctx.getString(R.string.share_bundle_item, p.getTitle(), p.getPrice()))
                    .append('\n');
        }
        if (items.size() > MAX_BUNDLE_LINES) {
            sb.append(ctx.getString(R.string.share_bundle_more, items.size() - MAX_BUNDLE_LINES))
                    .append('\n');
        }
        sb.append('\n');
        sb.append(ctx.getString(R.string.share_bundle_total, items.size(),
                String.format(Locale.US, "KSh %,d", total))).append('\n');
        sb.append(ctx.getString(R.string.share_bundle_pickup,
                b.getPickupLocation(), b.getPickupDateText()));
        sb.append("\n\n").append(ctx.getString(R.string.share_footer));
        send(ctx, sb.toString());
    }

    private static void send(Context ctx, String text) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(send, ctx.getString(R.string.share_chooser));
        if (!(ctx instanceof Activity)) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(chooser);
    }
}