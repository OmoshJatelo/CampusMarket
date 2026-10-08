package com.smart.campusmarket;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A marketplace post: either something FOR SALE or a WANTED request.
 *
 * - Price is stored as a number (so you can sort/filter later) and formatted only for display.
 * - createdAt is a timestamp; "2h ago" is calculated when displayed.
 * - The old 5-argument constructor is kept so your existing seed data in MainActivity still compiles.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_WANTED = "WANTED";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_EXPIRED = "EXPIRED";

    private final String id;
    private final String type;
    private final String title;
    private final long price;
    private final boolean negotiable;
    private final String category;
    private final String condition;
    private final String location;
    private final String description;
    private final String phone;
    private final ArrayList<String> imageUris;
    private final long createdAt;
    private String status;

    // Only used by the legacy constructor so hard-coded labels like "2h ago" still show.
    private String timeLabelOverride;

    public Product(String type, String title, long price, boolean negotiable,
                   String category, String condition, String location,
                   String description, String phone, List<String> imageUris) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.price = price;
        this.negotiable = negotiable;
        this.category = category;
        this.condition = condition;
        this.location = location;
        this.description = description;
        this.phone = phone;
        this.imageUris = new ArrayList<>(imageUris);
        this.createdAt = System.currentTimeMillis();
        this.status = STATUS_ACTIVE;
    }

    /** Legacy constructor: keeps the seed data in MainActivity working unchanged. */
    public Product(String title, String priceText, String location, String timeLabel, String phone) {
        this(TYPE_SALE, title, parsePrice(priceText), false, "", "", location, "", phone,
                new ArrayList<String>());
        this.timeLabelOverride = timeLabel;
    }

    /** Seed-data constructor that also sets a category (used by the category chips). */
    public Product(String title, String priceText, String category, String location,
                   String timeLabel, String phone) {
        this(TYPE_SALE, title, parsePrice(priceText), false, category, "", location, "", phone,
                new ArrayList<String>());
        this.timeLabelOverride = timeLabel;
    }

    // ---------------------------------------------------------------- helpers

    /** "KSh 4,500" or "4500" -> 4500. Returns 0 if there are no digits. */
    public static long parsePrice(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---------------------------------------------------------------- getters

    public String getId() { return id; }

    public String getType() { return type; }

    public boolean isWanted() { return TYPE_WANTED.equals(type); }

    public String getTitle() { return title; }

    /** Raw numeric price in KSh (use this for sorting and filtering). */
    public long getPriceValue() { return price; }

    /** Display price, e.g. "KSh 4,500". Keeps your adapter's getPrice() call working. */
    public String getPrice() {
        return String.format(Locale.US, "KSh %,d", price);
    }

    public boolean isNegotiable() { return negotiable; }

    public String getCategory() { return category; }

    public String getCondition() { return condition; }

    public String getLocation() { return location; }

    public String getDescription() { return description; }

    public String getPhone() { return phone; }

    /** Alias: your original Product exposed the number as getSellerPhone(). */
    public String getSellerPhone() { return phone; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }

    public List<String> getImageUris() { return imageUris; }

    /** First photo, or null if the post has none. */
    public String getFirstImageUri() {
        return imageUris.isEmpty() ? null : imageUris.get(0);
    }

    /** "Just now", "5m ago", "3h ago", "2d ago". */
    public String getTimeAgo() {
        if (timeLabelOverride != null) return timeLabelOverride;
        long minutes = (System.currentTimeMillis() - createdAt) / 60000L;
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        return (hours / 24) + "d ago";
    }

    /** Aliases so older adapter/detail code calling getTimePosted() or getTime() keeps working. */
    public String getTimePosted() { return getTimeAgo(); }

    public String getTime() { return getTimeAgo(); }

    /** wa.me link with a prefilled message, ready for the Contact button. */
    public String getWhatsAppUrl() {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        String message = isWanted()
                ? "Hi, I saw your request for \"" + title + "\" on Campus Market. I may have one."
                : "Hi, I saw your Listing of \"" + title + "\" on Campus Market. Is it still available?";
        return "https://wa.me/" + digits + "?text=" + Uri.encode(message);
    }
}