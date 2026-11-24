package com.smart.campusmarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Decides whether a WANTED request and a FOR SALE post fit each other.
 * Rules (all must hold): different owners, both active, sale price within the budget,
 * categories not in conflict, and at least one shared keyword in the title.
 * Runs on the phone, so it costs no extra server reads.
 */
public final class MatchEngine {

    /** One pairing from the signed-in user's point of view. */
    public static final class Match {
        public final Product mine;    // the user's own post
        public final Product other;   // the other student's post that fits it

        Match(Product mine, Product other) {
            this.mine = mine;
            this.other = other;
        }

        /** Stable id used to avoid notifying twice about the same pairing. */
        public String key() {
            return mine.getId() + "|" + other.getId();
        }
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "with", "used", "new", "good", "looking", "wanted",
            "want", "need", "sale", "selling", "cheap", "very", "any", "one"));

    /** Short product abbreviations that should be kept even though they are < 3 chars. */
    private static final Set<String> SHORT_KEEP = new HashSet<>(Arrays.asList("tv", "pc"));

    /**
     * Synonym map: after lowercasing and plural stripping, replace token with canonical form.
     * Extend by adding entries here; keep it small and unambiguous for campus marketplace terms.
     */
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("refrigerator", "fridge");
        SYNONYMS.put("television", "tv");
        SYNONYMS.put("tvs", "tv");
        SYNONYMS.put("pcs", "pc");
        SYNONYMS.put("smartphone", "phone");
        SYNONYMS.put("cellphone", "phone");
        SYNONYMS.put("mobile", "phone");
    }

    private MatchEngine() { }

    /** All pairings between the user's active posts and other students' active posts. */
    public static List<Match> findMatches(String uid, List<Product> all) {
        List<Match> out = new ArrayList<>();
        if (uid == null) return out;

        for (Product mine : all) {
            if (!uid.equals(mine.getOwnerId()) || !mine.isActive()) continue;
            for (Product other : all) {
                if (uid.equals(other.getOwnerId()) || !other.isActive()) continue;
                boolean fits = mine.isWanted() ? fits(mine, other) : fits(other, mine);
                if (fits) out.add(new Match(mine, other));
            }
        }
        return out;
    }

    /** True if the sale post satisfies the wanted request. */
    public static boolean fits(Product wanted, Product sale) {
        if (!wanted.isWanted() || sale.isWanted()) return false;
        if (sale.getPriceValue() > wanted.getPriceValue()) return false;   // over budget

        String wc = wanted.getCategory();
        String sc = sale.getCategory();
        boolean bothHaveCategory = wc != null && !wc.isEmpty() && sc != null && !sc.isEmpty();
        if (bothHaveCategory && !wc.equalsIgnoreCase(sc)) return false;

        String wantedTitle = wanted.getTitle() == null ? "" : wanted.getTitle();
        String wantedDesc = wanted.getDescription() == null ? "" : wanted.getDescription();
        String saleTitle = sale.getTitle() == null ? "" : sale.getTitle();
        String saleDesc = sale.getDescription() == null ? "" : sale.getDescription();

        // Title words of wanted match against title + description of sale
        Set<String> wantedTitleWords = keywords(wantedTitle);
        Set<String> saleTitleDescWords = keywords(saleTitle + " " + saleDesc);
        wantedTitleWords.retainAll(saleTitleDescWords);
        boolean titleHit = !wantedTitleWords.isEmpty();

        // Description words of wanted match against title of sale ONLY (never sale description)
        Set<String> wantedDescWords = keywords(wantedDesc);
        Set<String> saleTitleWords = keywords(saleTitle);
        wantedDescWords.retainAll(saleTitleWords);
        boolean descHit = !wantedDescWords.isEmpty();

        return titleHit || descHit;
    }

    private static Set<String> keywords(String text) {
        Set<String> out = new HashSet<>();
        if (text == null) return out;
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() < 3 && !SHORT_KEEP.contains(word)) continue;
            if (STOP_WORDS.contains(word)) continue;
            if (word.length() > 3 && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);   // "fridges" -> "fridge"
            }
            // Apply synonym mapping after plural stripping
            String canonical = SYNONYMS.get(word);
            if (canonical != null) word = canonical;
            out.add(word);
        }
        return out;
    }
}