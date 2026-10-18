package za.co.eduvos.codebridge.core.speech;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Terms that count as "technical" when a student explains a solution out loud.
 * All entries are lower case, with words separated by single spaces. Spoken forms of Big-O
 * are included because speech recognition usually writes "o of n" or "o n", not "O(n)".
 */
public final class TechnicalVocabulary {

    /** Longest phrase in the list, in words. */
    public static final int MAX_PHRASE_WORDS = 5;

    private static final Set<String> TERMS;

    static {
        Set<String> t = new HashSet<>();
        t.addAll(Arrays.asList(
                // data structures
                "array", "list", "linked list", "doubly linked list", "stack", "queue", "deque",
                "heap", "priority queue", "hash map", "hashmap", "hash table", "hash set", "hashset",
                "dictionary", "map", "set", "tree", "binary tree", "binary search tree", "bst",
                "trie", "graph", "node", "pointer", "index", "bucket", "collision", "string", "matrix"));
        t.addAll(Arrays.asList(
                // complexity
                "complexity", "time complexity", "space complexity", "big o", "big o notation",
                "o of n", "o of one", "o of log n", "o of n squared", "o of n log n",
                "o n", "o 1", "o log n", "o n squared", "o n log n",
                "constant time", "linear time", "logarithmic", "quadratic", "linear", "constant",
                "amortized", "worst case", "best case", "average case", "memory", "runtime"));
        t.addAll(Arrays.asList(
                // algorithms and techniques
                "algorithm", "iterate", "iteration", "loop", "for loop", "while loop", "recursion",
                "recursive", "base case", "brute force", "binary search", "two pointers",
                "two pointer", "sliding window", "dynamic programming", "memoization", "greedy",
                "divide and conquer", "backtracking", "depth first", "breadth first", "dfs", "bfs",
                "sort", "sorting", "merge sort", "quick sort", "traverse", "traversal", "lookup",
                "complement", "optimize", "optimized", "optimal", "edge case", "invariant",
                "overflow", "in place", "reverse", "swap", "hash", "hashing", "stack overflow"));
        t.addAll(Arrays.asList(
                // version control, since the interview follows the Git stage
                "commit", "branch", "merge", "conflict", "merge conflict", "push", "repository",
                "rebase", "pull request"));
        TERMS = Collections.unmodifiableSet(t);
    }

    private TechnicalVocabulary() {}

    /** @param normalised lower-case words separated by single spaces */
    public static boolean isTerm(String normalised) {
        return TERMS.contains(normalised);
    }

    public static Set<String> all() {
        return TERMS;
    }
}
