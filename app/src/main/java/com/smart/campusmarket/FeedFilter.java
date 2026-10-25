package com.smart.campusmarket;

import java.util.HashSet;
import java.util.Set;

/** The user's current filter and sort choices for the home feed. */
public final class FeedFilter {

    public enum Sort { NEWEST, PRICE_LOW, PRICE_HIGH }

    public enum Type { ALL, SALE, WANTED }

    public Sort sort = Sort.NEWEST;
    public Type type = Type.ALL;
    public long minPrice = 0;   // 0 means no minimum
    public long maxPrice = 0;   // 0 means no maximum
    public final Set<String> conditions = new HashSet<>();

    public boolean matches(Product p) {
        if (type == Type.SALE && p.isWanted()) return false;
        if (type == Type.WANTED && !p.isWanted()) return false;
        if (minPrice > 0 && p.getPriceValue() < minPrice) return false;
        if (maxPrice > 0 && p.getPriceValue() > maxPrice) return false;
        // Wanted requests have no condition, so the condition filter only applies to items for sale
        if (!conditions.isEmpty() && !p.isWanted() && !conditions.contains(p.getCondition())) {
            return false;
        }
        return true;
    }

    /** How many different filters are switched on (shown on the Filters chip). */
    public int activeCount() {
        int n = 0;
        if (sort != Sort.NEWEST) n++;
        if (type != Type.ALL) n++;
        if (minPrice > 0 || maxPrice > 0) n++;
        if (!conditions.isEmpty()) n++;
        return n;
    }

    public FeedFilter copy() {
        FeedFilter f = new FeedFilter();
        f.sort = sort;
        f.type = type;
        f.minPrice = minPrice;
        f.maxPrice = maxPrice;
        f.conditions.addAll(conditions);
        return f;
    }
}