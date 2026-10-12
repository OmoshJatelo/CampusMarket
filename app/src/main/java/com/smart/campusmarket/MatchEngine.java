package com.smart.campusmarket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

        Set<String> wantedWords = keywords(wanted.getTitle());
        Set<String> saleWords = keywords(sale.getTitle() + " " + sale.getDescription());
        wantedWords.retainAll(saleWords);
        return !wantedWords.isEmpty();
    }

    private static Set<String> keywords(String text) {
        Set<String> out = new HashSet<>();
        if (text == null) return out;
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() < 3 || STOP_WORDS.contains(word)) continue;
            if (word.length() > 3 && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);   // "fridges" -> "fridge"
            }
            out.add(word);
        }
        return out;
    }
}