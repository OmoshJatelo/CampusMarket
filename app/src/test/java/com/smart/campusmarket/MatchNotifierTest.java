package com.smart.campusmarket;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchNotifierTest {

    private static final long NOW = 1000000L;

    // ---- parseSeen tests ----

    @Test
    public void parseSeenNewFormatEntriesKeepTimestamps() {
        Set<String> stored = new HashSet<>(Arrays.asList("a|b@12345", "c|d@67890"));
        Map<String, Long> result = MatchNotifier.parseSeen(stored, NOW);
        assertEquals(2, result.size());
        assertEquals(Long.valueOf(12345), result.get("a|b"));
        assertEquals(Long.valueOf(67890), result.get("c|d"));
    }

    @Test
    public void parseSeenLegacyEntryGetsNow() {
        Set<String> stored = new HashSet<>(Arrays.asList("a|b", "c|d"));
        Map<String, Long> result = MatchNotifier.parseSeen(stored, NOW);
        assertEquals(2, result.size());
        assertEquals(NOW, result.get("a|b").longValue());
        assertEquals(NOW, result.get("c|d").longValue());
    }

    @Test
    public void parseSeenUnparsableSuffixGetsNowAndSaneKey() {
        Set<String> stored = new HashSet<>(Arrays.asList("a|b@notanumber", "x|y@abc"));
        Map<String, Long> result = MatchNotifier.parseSeen(stored, NOW);
        assertEquals(2, result.size());
        assertEquals(NOW, result.get("a|b").longValue());
        assertEquals(NOW, result.get("x|y").longValue());
    }

    @Test
    public void parseSeenKeyContainingPipeAndHyphenSurvives() {
        // UUID-style ids with hyphens, and the '|' separator
        Set<String> stored = new HashSet<>(Arrays.asList("550e8400-e29b-41d4-a716-446655440000|550e8400-e29b-41d4-a716-446655440001@12345"));
        Map<String, Long> result = MatchNotifier.parseSeen(stored, NOW);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(12345), result.get("550e8400-e29b-41d4-a716-446655440000|550e8400-e29b-41d4-a716-446655440001"));
    }

    @Test
    public void parseSeenNullOrEmptyInput() {
        Map<String, Long> result1 = MatchNotifier.parseSeen(null, NOW);
        assertTrue(result1.isEmpty());

        Map<String, Long> result2 = MatchNotifier.parseSeen(new HashSet<>(), NOW);
        assertTrue(result2.isEmpty());
    }

    @Test
    public void parseSeenBlankKeyIgnored() {
        Set<String> stored = new HashSet<>(Arrays.asList("@12345", " @67890"));
        Map<String, Long> result = MatchNotifier.parseSeen(stored, NOW);
        assertTrue(result.isEmpty());
    }

    // ---- expireSeen tests ----

    @Test
    public void expireSeenOlderThanMaxDropped() {
        Map<String, Long> seen = new HashMap<>();
        seen.put("a|b", NOW - 1000);
        seen.put("c|d", NOW - 500);
        long maxAgeMs = 750;
        Map<String, Long> result = MatchNotifier.expireSeen(seen, NOW, maxAgeMs);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("c|d"));
        assertFalse(result.containsKey("a|b"));
    }

    @Test
    public void expireSeenExactlyMaxAgeKept() {
        Map<String, Long> seen = new HashMap<>();
        seen.put("a|b", NOW - 1000);
        long maxAgeMs = 1000;
        Map<String, Long> result = MatchNotifier.expireSeen(seen, NOW, maxAgeMs);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a|b"));
    }

    @Test
    public void expireSeenNewerKept() {
        Map<String, Long> seen = new HashMap<>();
        seen.put("a|b", NOW - 100);
        seen.put("c|d", NOW);
        long maxAgeMs = 500;
        Map<String, Long> result = MatchNotifier.expireSeen(seen, NOW, maxAgeMs);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("a|b"));
        assertTrue(result.containsKey("c|d"));
    }

    @Test
    public void expireSeenReturnsNewMapLeavesInputUnchanged() {
        Map<String, Long> seen = new HashMap<>();
        seen.put("a|b", NOW - 1000);
        seen.put("c|d", NOW - 100);
        Map<String, Long> originalCopy = new HashMap<>(seen);
        long maxAgeMs = 500;
        MatchNotifier.expireSeen(seen, NOW, maxAgeMs);
        assertEquals(originalCopy, seen);
    }

    @Test
    public void expireSeenNullInput() {
        Map<String, Long> result = MatchNotifier.expireSeen(null, NOW, 1000);
        assertTrue(result.isEmpty());
    }

    // ---- serializeSeen tests ----

    @Test
    public void serializeSeenParseSeenRoundTrip() {
        Map<String, Long> seen = new HashMap<>();
        seen.put("a|b", 12345L);
        seen.put("c|d", 67890L);

        Set<String> serialized = MatchNotifier.serializeSeen(seen);
        Map<String, Long> parsed = MatchNotifier.parseSeen(serialized, NOW);

        assertEquals(2, parsed.size());
        assertEquals(Long.valueOf(12345), parsed.get("a|b"));
        assertEquals(Long.valueOf(67890), parsed.get("c|d"));
    }

    // ---- Regression for the bug being fixed ----

    @Test
    public void entryNotInCurrentListSurvivesIfYoungerThanMaxAge() {
        // An entry whose pair is NOT in any "current" list should survive expireSeen
        // while it is younger than the max age.
        long timestamp = NOW - 1000; // age = 1000
        Set<String> stored = new HashSet<>(Arrays.asList("x|y@" + timestamp));
        Map<String, Long> seenMap = MatchNotifier.parseSeen(stored, NOW);
        Map<String, Long> expired = MatchNotifier.expireSeen(seenMap, NOW, 5000); // maxAge = 5000
        assertTrue(expired.containsKey("x|y"));
    }

    @Test
    public void legacyEntryStampedNowNotExpiredAtNowPlusMaxAgeMinusOne() {
        // Legacy entry gets stamped with 'now' when first read
        Set<String> stored = new HashSet<>(Arrays.asList("legacy|key"));
        Map<String, Long> seenMap = MatchNotifier.parseSeen(stored, NOW);
        long maxAgeMs = 5000;
        // At NOW + maxAge - 1, age = maxAge - 1, should be kept
        Map<String, Long> expired = MatchNotifier.expireSeen(seenMap, NOW + maxAgeMs - 1, maxAgeMs);
        assertTrue(expired.containsKey("legacy|key"));
    }

    @Test
    public void legacyEntryStampedNowExpiredAtNowPlusMaxAgePlusOne() {
        // At NOW + maxAge + 1, age = maxAge + 1, should be dropped
        Set<String> stored = new HashSet<>(Arrays.asList("legacy|key"));
        Map<String, Long> seenMap = MatchNotifier.parseSeen(stored, NOW);
        long maxAgeMs = 5000;
        Map<String, Long> expired = MatchNotifier.expireSeen(seenMap, NOW + maxAgeMs + 1, maxAgeMs);
        assertFalse(expired.containsKey("legacy|key"));
    }
}