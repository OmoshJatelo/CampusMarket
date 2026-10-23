package com.smart.campusmarket;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * A marketplace post: something FOR SALE or a WANTED request.
 * Stored in Firestore (see ListingRepository) and passed between screens as Serializable.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 2L;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

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
    private final long expiresAt;
    private String status;
    private String ownerId;
    private String bundleId;   // set when the post belongs to a move-out bundle

    // Only used by the legacy constructors so labels like "2h ago" still show.
    private String timeLabelOverride;

    /** Creates a brand-new post (new id, now, expires in LISTING_DAYS). */
    public Product(String type, String title, long price, boolean negotiable,
                   String category, String condition, String location,
                   String description, String phone, List<String> imageUris) {
        this(UUID.randomUUID().toString(), type, title, price, negotiable, category, condition,
                location, description, phone, imageUris, System.currentTimeMillis(), 0L,
                STATUS_ACTIVE, null);
    }

    /** Full constructor, used when rebuilding a post from Firestore. expiresAt <= 0 means default. */
    public Product(String id, String type, String title, long price, boolean negotiable,
                   String category, String condition, String location, String description,
                   String phone, List<String> imageUris, long createdAt, long expiresAt,
                   String status, String ownerId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.price = price;
        this.negotiable = negotiable;
        this.category = category;
        this.condition = condition;
        this.location = location;
        this.description = description;
        this.phone = phone;
        this.imageUris = imageUris == null ? new ArrayList<String>() : new ArrayList<String>(imageUris);
        this.createdAt = createdAt;
        this.expiresAt = expiresAt > 0 ? expiresAt : createdAt + AppConfig.LISTING_DAYS * DAY_MS;
        this.status = status == null ? STATUS_ACTIVE : status;
        this.ownerId = ownerId;
    }

    /** Legacy constructor kept so old code keeps compiling. */
    public Product(String title, String priceText, String location, String timeLabel, String phone) {
        this(TYPE_SALE, title, parsePrice(priceText), false, "", "", location, "", phone,
                new ArrayList<String>());
        this.timeLabelOverride = timeLabel;
    }

    /** Legacy constructor that also sets a category. */
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

    /** Same post, but with the uploaded photo links swapped in. */
    public Product copyWithImages(List<String> urls) {
        Product copy = new Product(id, type, title, price, negotiable, category, condition,
                location, description, phone, urls, createdAt, expiresAt, status, ownerId);
        copy.timeLabelOverride = timeLabelOverride;
        copy.bundleId = bundleId;
        return copy;
    }

    // ---------------------------------------------------------------- lifecycle

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    /** Visible in the feed: not sold, not expired. */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status) && !isExpired();
    }

    /** Whole days left before expiry (0 if expired). */
    public int daysLeft() {
        long ms = expiresAt - System.currentTimeMillis();
        if (ms <= 0) return 0;
        return (int) ((ms + DAY_MS - 1) / DAY_MS);
    }

    // ---------------------------------------------------------------- getters

    public String getId() { return id; }

    public String getType() { return type; }

    public boolean isWanted() { return TYPE_WANTED.equals(type); }

    public String getTitle() { return title; }

    /** Raw numeric price in KSh (use this for sorting and filtering). */
    public long getPriceValue() { return price; }

    /** Display price, e.g. "KSh 4,500". */
    public String getPrice() {
        return String.format(Locale.US, "KSh %,d", price);
    }

    public boolean isNegotiable() { return negotiable; }

    public String getCategory() { return category; }

    public String getCondition() { return condition; }

    public String getLocation() { return location; }

    public String getDescription() { return description; }

    public String getPhone() { return phone; }

    /** Alias: the original Product exposed the number as getSellerPhone(). */
    public String getSellerPhone() { return phone; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getOwnerId() { return ownerId; }

    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getBundleId() { return bundleId; }

    public void setBundleId(String bundleId) { this.bundleId = bundleId; }

    public long getCreatedAt() { return createdAt; }

    public long getExpiresAt() { return expiresAt; }

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

    public String getTimePosted() { return getTimeAgo(); }

    public String getTime() { return getTimeAgo(); }

    /** wa.me link with a prefilled message, ready for the Contact button. */
    public String getWhatsAppUrl() {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        String message = isWanted()
                ? "Hi, I saw your request for \"" + title + "\" on Campus Market. I may have one."
                : "Hi, is \"" + title + "\" still available? (Campus Market)";
        return "https://wa.me/" + digits + "?text=" + Uri.encode(message);
    }
}