package za.co.eduvos.codebridge.core.speech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.ArrayList;

public class KeywordAnalyzerTest {

    @Test
    public void emptyAndNullTranscriptsScoreZero() {
        assertEquals(0f, KeywordAnalyzer.analyze("").density, 0f);
        assertEquals(0f, KeywordAnalyzer.analyze(null).density, 0f);
        assertEquals(0f, KeywordAnalyzer.analyze("   ,  .. ").density, 0f);
        assertEquals(0, KeywordAnalyzer.analyze("").totalWords);
    }

    @Test
    public void plainSpeechHasNoTechnicalWords() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("I went to the shop and bought some bread");
        assertEquals(9, r.totalWords);
        assertEquals(0, r.technicalWords);
        assertEquals(0f, r.density, 0f);
        assertTrue(r.occurrences.isEmpty());
    }

    @Test
    public void densityIsTechnicalWordsOverTotalWords() {
        // 8 words: "array" (1) + "loop" (1) are technical -> 2/8
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("I walk through the array in a loop");
        assertEquals(8, r.totalWords);
        assertEquals(2, r.technicalWords);
        assertEquals(0.25f, r.density, 1e-6f);
    }

    @Test
    public void multiWordTermsCountAllTheirWordsAndTheLongestMatchWins() {
        // "hash map" is one term of two words, not "hash" + "map"
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("use a hash map");
        assertEquals(4, r.totalWords);
        assertEquals(2, r.technicalWords);
        assertEquals(1, (int) r.occurrences.get("hash map"));
        assertFalse(r.occurrences.containsKey("hash"));
        assertFalse(r.occurrences.containsKey("map"));
    }

    @Test
    public void spokenBigONotationIsRecognised() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("The time complexity is O of n and space is O of one");
        assertTrue(r.occurrences.containsKey("time complexity"));
        assertTrue(r.occurrences.containsKey("o of n"));
        assertTrue(r.occurrences.containsKey("o of one"));
        assertTrue(r.density > 0.4f);
    }

    @Test
    public void writtenBigOIsTokenisedSensibly() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("It runs in O(n) time");
        assertTrue(r.occurrences.containsKey("o n"));
    }

    @Test
    public void pluralsMatchTheirSingularTerm() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("arrays and hash maps and queries");
        assertTrue(r.occurrences.containsKey("array"));
        assertTrue(r.occurrences.containsKey("hash map"));
    }

    @Test
    public void repeatedTermsAreCountedAndSortedMostFrequentFirst() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("loop loop loop array array stack");
        assertEquals(Arrays.asList("loop", "array", "stack"), new ArrayList<>(r.occurrences.keySet()));
        assertEquals(3, (int) r.occurrences.get("loop"));
        assertEquals(1f, r.density, 0f);
    }

    @Test
    public void caseAndPunctuationAreIgnored() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("Recursion, RECURSION! Base-case?");
        assertEquals(2, (int) r.occurrences.get("recursion"));
        assertTrue(r.occurrences.containsKey("base case"));
    }

    @Test
    public void densityNeverExceedsOne() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("hash map binary search tree linked list");
        assertTrue(r.density <= 1f);
        assertEquals(1f, r.density, 0f);
    }

    @Test
    public void extraProblemTermsAreCounted() {
        KeywordAnalyzer.Result without = KeywordAnalyzer.analyze("I check the complement for each number");
        KeywordAnalyzer.Result with = KeywordAnalyzer.analyze("I check the target sum for each number",
                Arrays.asList("target sum"));
        assertTrue(without.occurrences.containsKey("complement"));
        assertEquals(1, (int) with.occurrences.get("target sum"));
        assertEquals(2, with.technicalWords);
    }

    @Test
    public void extraTermsLongerThanTheVocabularyLimitStillMatch() {
        KeywordAnalyzer.Result r = KeywordAnalyzer.analyze("the sum of two distinct array elements here",
                Arrays.asList("sum of two distinct array elements"));
        assertEquals(1, (int) r.occurrences.get("sum of two distinct array elements"));
        assertEquals(6, r.technicalWords);
    }
}
