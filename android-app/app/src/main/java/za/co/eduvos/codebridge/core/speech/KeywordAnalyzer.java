package za.co.eduvos.codebridge.core.speech;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Scores how technical a spoken explanation is.
 *
 * <b>Keyword density = technical words / total words</b>, a value from 0.0 to 1.0. A multi-word
 * term such as "hash map" contributes all of its words, so the density never exceeds 1. The longest
 * matching phrase wins, and a plural ("arrays") matches its singular form.
 */
public final class KeywordAnalyzer {

    public static final class Result {
        public final int totalWords;
        public final int technicalWords;
        /** 0.0 to 1.0; 0 for an empty transcript. */
        public final float density;
        /** Each distinct term found and how often it was said, most frequent first. */
        public final Map<String, Integer> occurrences;

        Result(int totalWords, int technicalWords, Map<String, Integer> occurrences) {
            this.totalWords = totalWords;
            this.technicalWords = technicalWords;
            this.density = totalWords == 0 ? 0f : Math.min(1f, (float) technicalWords / totalWords);
            this.occurrences = Collections.unmodifiableMap(occurrences);
        }
    }

    private KeywordAnalyzer() {}

    public static Result analyze(String transcript) {
        return analyze(transcript, Collections.<String>emptyList());
    }

    /** @param extraTerms problem-specific words to count in addition to the shared vocabulary */
    public static Result analyze(String transcript, Collection<String> extraTerms) {
        List<String> words = tokenize(transcript);

        Set<String> extras = new HashSet<>();
        int maxWords = TechnicalVocabulary.MAX_PHRASE_WORDS;
        if (extraTerms != null) {
            for (String e : extraTerms) {
                String norm = join(tokenize(e));
                if (norm.isEmpty()) continue;
                extras.add(norm);
                maxWords = Math.max(maxWords, norm.split(" ").length);
            }
        }

        Map<String, Integer> counts = new TreeMap<>();
        int technical = 0;
        int i = 0;
        while (i < words.size()) {
            int matchedLen = 0;
            String matchedTerm = null;
            for (int len = Math.min(maxWords, words.size() - i); len >= 1 && matchedTerm == null; len--) {
                String phrase = join(words.subList(i, i + len));
                String term = lookup(phrase, extras);
                if (term != null) {
                    matchedTerm = term;
                    matchedLen = len;
                }
            }
            if (matchedTerm != null) {
                counts.merge(matchedTerm, 1, Integer::sum);
                technical += matchedLen;
                i += matchedLen;
            } else {
                i++;
            }
        }
        return new Result(words.size(), technical, sortByFrequency(counts));
    }

    /** The vocabulary entry for this phrase (trying its singular form), or null. */
    private static String lookup(String phrase, Set<String> extras) {
        if (TechnicalVocabulary.isTerm(phrase) || extras.contains(phrase)) return phrase;
        String singular = singular(phrase);
        if (singular != null && (TechnicalVocabulary.isTerm(singular) || extras.contains(singular))) return singular;
        return null;
    }

    private static String singular(String phrase) {
        if (phrase.length() > 4 && phrase.endsWith("ies")) return phrase.substring(0, phrase.length() - 3) + "y";
        if (phrase.length() > 4 && phrase.endsWith("es")) return phrase.substring(0, phrase.length() - 2);
        if (phrase.length() > 3 && phrase.endsWith("s")) return phrase.substring(0, phrase.length() - 1);
        return null;
    }

    /** Lower-cases and splits on anything that isn't a letter, digit or apostrophe. */
    static List<String> tokenize(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        for (String w : text.toLowerCase(Locale.ROOT).split("[^a-z0-9']+")) {
            if (!w.isEmpty()) out.add(w);
        }
        return out;
    }

    private static String join(List<String> words) {
        return String.join(" ", words);
    }

    private static Map<String, Integer> sortByFrequency(Map<String, Integer> counts) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        Collections.sort(entries, (a, b) -> {
            int byCount = Integer.compare(b.getValue(), a.getValue());
            return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
        });
        Map<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) sorted.put(e.getKey(), e.getValue());
        return sorted;
    }
}
