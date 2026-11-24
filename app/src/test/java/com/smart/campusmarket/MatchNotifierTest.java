package com.smart.campusmarket;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchNotifierTest {

    @Test
    public void pruneSeenKeepsKeysThatAreStillCurrent() {
        Set<String> seen = new HashSet<>(Arrays.asList("a|b", "c|d", "e|f"));
        Set<String> current = new HashSet<>(Arrays.asList("a|b", "c|d"));
        Set<String> result = MatchNotifier.pruneSeen(seen, current);
        assertEquals(2, result.size());
        assertTrue(result.contains("a|b"));
        assertTrue(result.contains("c|d"));
        assertFalse(result.contains("e|f"));
    }

    @Test
    public void pruneSeenDropsKeysThatAreNoLongerCurrent() {
        Set<String> seen = new HashSet<>(Arrays.asList("a|b", "c|d", "e|f"));
        Set<String> current = new HashSet<>(Arrays.asList("x|y", "z|w"));
        Set<String> result = MatchNotifier.pruneSeen(seen, current);
        assertTrue(result.isEmpty());
    }

    @Test
    public void pruneSeenReturnsEmptyWhenNothingIsCurrent() {
        Set<String> seen = new HashSet<>(Arrays.asList("a|b", "c|d"));
        Set<String> current = new HashSet<>();
        Set<String> result = MatchNotifier.pruneSeen(seen, current);
        assertTrue(result.isEmpty());
    }

    @Test
    public void pruneSeenReturnsEmptyForNullOrEmptyInput() {
        Set<String> result1 = MatchNotifier.pruneSeen(null, new HashSet<>(Arrays.asList("a|b")));
        assertTrue(result1.isEmpty());

        Set<String> result2 = MatchNotifier.pruneSeen(new HashSet<>(Arrays.asList("a|b")), null);
        assertTrue(result2.isEmpty());

        Set<String> result3 = MatchNotifier.pruneSeen(null, null);
        assertTrue(result3.isEmpty());

        Set<String> result4 = MatchNotifier.pruneSeen(new HashSet<>(), new HashSet<>(Arrays.asList("a|b")));
        assertTrue(result4.isEmpty());
    }

    @Test
    public void pruneSeenDoesNotMutateEitherInputSet() {
        Set<String> seen = new HashSet<>(Arrays.asList("a|b", "c|d"));
        Set<String> current = new HashSet<>(Arrays.asList("a|b"));
        Set<String> seenCopy = new HashSet<>(seen);
        Set<String> currentCopy = new HashSet<>(current);

        MatchNotifier.pruneSeen(seen, current);

        assertEquals(seenCopy, seen);
        assertEquals(currentCopy, current);
    }
}