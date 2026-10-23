package com.smart.campusmarket;

/** Phone number helpers shared by screens. */
public final class PhoneUtil {

    private PhoneUtil() { }

    /**
     * Accepts 0712345678, 712345678, 254712345678 and +254712345678 and returns +254712345678.
     * Returns null if it is not a valid Kenyan mobile number.
     */
    public static String normalize(String raw) {
        String d = raw.replaceAll("[^0-9+]", "");
        if (d.startsWith("0")) d = "+254" + d.substring(1);
        else if (d.startsWith("254")) d = "+" + d;
        else if (d.startsWith("7") || d.startsWith("1")) d = "+254" + d;
        return d.matches("^\\+254[17]\\d{8}$") ? d : null;
    }
}