package com.smart.campusmarket;

import android.net.Uri;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A move-out sale: several of one student's posts grouped under one pickup date and place.
 * (Named MoveOutBundle so it does not clash with android.os.Bundle.)
 */
public class MoveOutBundle implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private final String id;
    private final String ownerId;
    private final String title;
    private final String description;
    private final String pickupLocation;
    private final String phone;
    private final long pickupDate;   // midnight UTC of the chosen day
    private final long createdAt;
    private final int itemCount;

    public MoveOutBundle(String id, String ownerId, String title, String description,
                         String pickupLocation, String phone, long pickupDate,
                         long createdAt, int itemCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.pickupLocation = pickupLocation;
        this.phone = phone;
        this.pickupDate = pickupDate;
        this.createdAt = createdAt;
        this.itemCount = itemCount;
    }

    public String getId() { return id; }

    public String getOwnerId() { return ownerId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getPickupLocation() { return pickupLocation; }

    public String getPhone() { return phone; }

    public long getPickupDate() { return pickupDate; }

    public long getCreatedAt() { return createdAt; }

    public int getItemCount() { return itemCount; }

    /** Visible until the end of the pickup day. */
    public boolean isActive() {
        return System.currentTimeMillis() < pickupDate + DAY_MS;
    }

    public String getPickupDateText() {
        return formatDate(pickupDate);
    }

    /** The date picker returns midnight UTC, so format in UTC to show the day the user chose. */
    public static String formatDate(long utcMillis) {
        SimpleDateFormat format = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date(utcMillis));
    }

    public String getWhatsAppUrl() {
        String digits = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        String message = "Hi, I saw your move-out sale \"" + title
                + "\" on CampusMarket. Are the items still available?";
        return "https://wa.me/" + digits + "?text=" + Uri.encode(message);
    }
}