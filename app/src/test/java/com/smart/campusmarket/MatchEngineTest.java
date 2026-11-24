package com.smart.campusmarket;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MatchEngineTest {

    private static final long NOW = System.currentTimeMillis();
    private static final long FUTURE = NOW + 86400000L; // 1 day
    private static final long PAST = NOW - 86400000L;   // 1 day ago

    /** Helper to build a Product with the full constructor. */
    private Product sale(String id, String title, long price, String category, String description) {
        return new Product(id, Product.TYPE_SALE, title, price, false, category, "good", "Campus",
                description, "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "other");
    }

    private Product wanted(String id, String title, long price, String category, String description) {
        return new Product(id, Product.TYPE_WANTED, title, price, false, category, "good", "Campus",
                description, "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "me");
    }

    private Product expiredSale(String id, String title, long price, String category) {
        return new Product(id, Product.TYPE_SALE, title, price, false, category, "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, PAST, Product.STATUS_ACTIVE, "other");
    }

    private Product soldSale(String id, String title, long price, String category) {
        return new Product(id, Product.TYPE_SALE, title, price, false, category, "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_SOLD, "other");
    }

    @Test
    public void salePricedUnderBudgetMatches() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "iPhone 12", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void salePricedExactlyAtBudgetMatches() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "iPhone 12", 50000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void saleOverBudgetIsRejected() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "iPhone 12", 55000, "Electronics", "");
        assertFalse(MatchEngine.fits(w, s));
    }

    @Test
    public void differentNonEmptyCategoriesAreRejected() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "iPhone 12", 45000, "Furniture", "");
        assertFalse(MatchEngine.fits(w, s));
    }

    @Test
    public void sameCategoryDifferentCaseMatches() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "iPhone 12", 45000, "electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void oneEmptyCategoryIsAllowed() {
        Product w = wanted("w1", "iPhone", 50000, "", "");
        Product s = sale("s1", "iPhone 12", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void sameOwnerIsRejected() {
        Product w = new Product("w1", Product.TYPE_WANTED, "iPhone", 50000, false, "Electronics", "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "me");
        Product s = new Product("s1", Product.TYPE_SALE, "iPhone 12", 45000, false, "Electronics", "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "me");
        List<Product> all = Arrays.asList(w, s);
        List<MatchEngine.Match> matches = MatchEngine.findMatches("me", all);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void soldPostIsRejected() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = soldSale("s1", "iPhone 12", 45000, "Electronics");
        List<Product> all = Arrays.asList(w, s);
        List<MatchEngine.Match> matches = MatchEngine.findMatches("me", all);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void expiredPostIsRejected() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = expiredSale("s1", "iPhone 12", 45000, "Electronics");
        List<Product> all = Arrays.asList(w, s);
        List<MatchEngine.Match> matches = MatchEngine.findMatches("me", all);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void wantedVsWantedNeverMatches() {
        Product w1 = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product w2 = wanted("w2", "iPhone 12", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w1, w2));
    }

    @Test
    public void saleVsSaleNeverMatches() {
        Product s1 = sale("s1", "iPhone", 50000, "Electronics", "");
        Product s2 = sale("s2", "iPhone 12", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(s1, s2));
    }

    @Test
    public void noSharedKeywordIsRejected() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product s = sale("s1", "Samsung Galaxy", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w, s));
    }

    @Test
    public void stopWordsAloneDoNotCreateMatch() {
        Product w = wanted("w1", "looking for used one", 50000, "Electronics", "");
        Product s = sale("s1", "selling cheap very good", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w, s));
    }

    @Test
    public void findMatchesReturnsEmptyForNullUid() {
        Product s = sale("s1", "iPhone", 45000, "Electronics", "");
        List<Product> all = Arrays.asList(s);
        List<MatchEngine.Match> matches = MatchEngine.findMatches(null, all);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void findMatchesWorksForUserWantedAndUserSale() {
        // User's WANTED fits someone else's SALE
        Product myWanted = wanted("w1", "iPhone", 50000, "Electronics", "");
        Product theirSale = sale("s1", "iPhone 12", 45000, "Electronics", "");
        List<Product> all = Arrays.asList(myWanted, theirSale);
        List<MatchEngine.Match> matches = MatchEngine.findMatches("me", all);
        assertEquals(1, matches.size());
        assertEquals("w1", matches.get(0).mine.getId());
        assertEquals("s1", matches.get(0).other.getId());

        // User's SALE fits someone else's WANTED
        Product mySale = new Product("s2", Product.TYPE_SALE, "MacBook", 80000, false, "Electronics", "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "me");
        Product theirWanted = new Product("w2", Product.TYPE_WANTED, "MacBook Pro", 90000, false, "Electronics", "good", "Campus",
                "", "555-1234", new ArrayList<>(), NOW, FUTURE, Product.STATUS_ACTIVE, "other");
        all = Arrays.asList(mySale, theirWanted);
        matches = MatchEngine.findMatches("me", all);
        assertEquals(1, matches.size());
        assertEquals("s2", matches.get(0).mine.getId());
        assertEquals("w2", matches.get(0).other.getId());
    }

    // ---- New behaviour tests ----

    @Test
    public void wantedTvMatchesSale32InchTv() {
        Product w = wanted("w1", "TV", 50000, "Electronics", "");
        Product s = sale("s1", "32 inch TV", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedPcMatchesSaleGamingPc() {
        Product w = wanted("w1", "PC", 50000, "Electronics", "");
        Product s = sale("s1", "gaming pc", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void tvsMatchesTvAndPcsMatchesPc() {
        Product w = wanted("w1", "TVs", 50000, "Electronics", "");
        Product s = sale("s1", "TV", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));

        Product w2 = wanted("w2", "PCs", 50000, "Electronics", "");
        Product s2 = sale("s2", "PC", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w2, s2));
    }

    @Test
    public void twoLetterNoiseWordDoesNotMatch() {
        Product w = wanted("w1", "is", 50000, "Electronics", "");
        Product s = sale("s1", "is", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w, s));

        Product w2 = wanted("w2", "of", 50000, "Electronics", "");
        Product s2 = sale("s2", "of", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w2, s2));
    }

    @Test
    public void keywordOnlyInWantedDescriptionMatchesSaleTitle() {
        Product w = wanted("w1", "iPhone", 50000, "Electronics", "need 128gb model");
        Product s = sale("s1", "128gb iPhone 12", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void pluralFridgesMatchesFridge() {
        Product w = wanted("w1", "fridges", 50000, "Appliances", "");
        Product s = sale("s1", "fridge", 45000, "Appliances", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedRefrigeratorMatchesSaleMiniFridge() {
        Product w = wanted("w1", "refrigerator", 50000, "Appliances", "");
        Product s = sale("s1", "mini fridge", 45000, "Appliances", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedTelevisionMatchesSaleSamsungTv() {
        Product w = wanted("w1", "television", 50000, "Electronics", "");
        Product s = sale("s1", "samsung tv", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedSmartphoneMatchesSaleUsedPhone() {
        Product w = wanted("w1", "smartphone", 50000, "Electronics", "");
        Product s = sale("s1", "used phone", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedMobileMatchesSalePhone() {
        Product w = wanted("w1", "mobile", 50000, "Electronics", "");
        Product s = sale("s1", "phone", 45000, "Electronics", "");
        assertTrue(MatchEngine.fits(w, s));
    }

    @Test
    public void wantedLaptopDoesNotMatchSaleNotebook() {
        // "laptop" and "notebook" are deliberately NOT synonyms (ambiguous on campus)
        Product w = wanted("w1", "laptop", 50000, "Electronics", "");
        Product s = sale("s1", "notebook", 45000, "Electronics", "");
        assertFalse(MatchEngine.fits(w, s));
    }
}