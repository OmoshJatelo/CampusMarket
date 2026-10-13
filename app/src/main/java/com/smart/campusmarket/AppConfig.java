package com.smart.campusmarket;

/** One place for the settings you may need to change. */
public final class AppConfig {

    private AppConfig() { }

    // ---- Cloudinary (free photo hosting). See the setup steps in the chat. ----
    public static final String CLOUDINARY_CLOUD_NAME = "urf4pcg4";
    public static final String CLOUDINARY_UPLOAD_PRESET = "campusMarket";

    /**
     * Only emails ending in this domain can sign up, e.g. "jkuat.ac.ke".
     * Leave it empty ("") to allow any email address (verification link is still required).
     */
    public static final String STUDENT_EMAIL_DOMAIN = "";

    /** How long a post stays active before the "still available?" prompt. */
    public static final int LISTING_DAYS = 14;

    /** Posts expiring within this many days are flagged for renewal. */
    public static final int EXPIRY_WARNING_DAYS = 3;

    /** Most recent posts loaded into the feed. Keeps Firestore reads low on the free plan. */
    public static final int MAX_FEED_LISTINGS = 200;
}