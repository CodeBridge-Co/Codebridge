package za.co.eduvos.codebridge.core.git;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Builds the merge-conflict exercises and does the line-based 3-way merge (diff3 style) that
 * produces real conflict markers. No Android or JGit dependency.
 */
public final class MergeConflictGenerator {

    public static final String MARK_OURS = "<<<<<<< ";
    public static final String MARK_SEP = "=======";
    public static final String MARK_THEIRS = ">>>>>>> ";

    public static final class MergeResult {
        public final String content;
        public final boolean conflict;

        MergeResult(String content, boolean conflict) {
            this.content = content;
            this.conflict = conflict;
        }
    }

    /** One exercise: a repo where merging {@link #targetBranch} into HEAD conflicts in {@link #conflictFile}. */
    public static final class Scenario {
        public final String title;
        public final GitRepositoryState state;
        public final String targetBranch;
        public final String targetTip;
        public final String conflictFile;

        Scenario(String title, GitRepositoryState state, String targetBranch, String targetTip, String conflictFile) {
            this.title = title;
            this.state = state;
            this.targetBranch = targetBranch;
            this.targetTip = targetTip;
            this.conflictFile = conflictFile;
        }
    }

    private static final class Template {
        final String title;
        final String slug;
        final String[] baseLines;
        final int conflictLine;
        final String oursLine;
        final String theirsLine;

        Template(String title, String slug, String[] baseLines, int conflictLine, String oursLine, String theirsLine) {
            this.title = title;
            this.slug = slug;
            this.baseLines = baseLines;
            this.conflictLine = conflictLine;
            this.oursLine = oursLine;
            this.theirsLine = theirsLine;
        }
    }

    private static final Template[] TEMPLATES = {
            new Template("Two Sum", "two-sum", new String[]{
                    "function twoSum(nums, target) {",
                    "  const seen = {};",
                    "  for (let i = 0; i < nums.length; i++) {",
                    "    const need = target - nums[i];",
                    "    // TODO: look up need",
                    "    seen[nums[i]] = i;",
                    "  }",
                    "  return [];",
                    "}"},
                    4,
                    "    if (seen[need] !== undefined) return [seen[need], i];",
                    "    if (need in seen) return [seen[need], i];"),
            new Template("Reverse String", "reverse-string", new String[]{
                    "function reverseString(s) {",
                    "  // TODO: split into characters",
                    "  const out = [];",
                    "  for (let i = chars.length - 1; i >= 0; i--) out.push(chars[i]);",
                    "  return out.join(\"\");",
                    "}"},
                    1,
                    "  const chars = s.split(\"\");",
                    "  const chars = Array.from(s);"),
            new Template("Valid Parentheses", "valid-parentheses", new String[]{
                    "function isValid(s) {",
                    "  // TODO: define bracket pairs",
                    "  const stack = [];",
                    "  for (const c of s) {",
                    "    if (c in pairs) { if (stack.pop() !== pairs[c]) return false; }",
                    "    else stack.push(c);",
                    "  }",
                    "  return stack.length === 0;",
                    "}"},
                    1,
                    "  const pairs = { \")\": \"(\", \"]\": \"[\", \"}\": \"{\" };",
                    "  const pairs = Object.freeze({ \")\": \"(\", \"]\": \"[\", \"}\": \"{\" });")
    };

    private MergeConflictGenerator() {}

    /** Deterministic: the same seed always gives the same exercise. */
    public static Scenario generate(long seed, LongSupplier clock) {
        Template t = TEMPLATES[(int) Math.floorMod(seed, (long) TEMPLATES.length)];
        long now = clock.getAsLong();
        String file = "src/solution.js";
        String featureBranch = "feature/" + t.slug;

        String baseSolution = String.join("\n", t.baseLines);
        String baseReadme = "# " + t.title + "\nWrite your solution in src/solution.js.";
        String mainReadme = "# " + t.title + "\nWrite your solution in src/solution.js. Aim for O(n) time.";

        String[] oursLines = t.baseLines.clone();
        oursLines[t.conflictLine] = t.oursLine;
        String[] theirsLines = t.baseLines.clone();
        theirsLines[t.conflictLine] = t.theirsLine;

        GitRepositoryState s = new GitRepositoryState();

        Map<String, String> c0Files = new LinkedHashMap<>();
        c0Files.put(file, baseSolution);
        c0Files.put("README.md", baseReadme);
        GitRepositoryState.Commit c0 = s.addCommit("Initial commit", Collections.<String>emptyList(), c0Files, now - 3 * 3600_000L);

        Map<String, String> mainFiles = new LinkedHashMap<>();
        mainFiles.put(file, String.join("\n", theirsLines));
        mainFiles.put("README.md", mainReadme);
        GitRepositoryState.Commit m1 = s.addCommit("Add lookup approach and README note",
                Collections.singletonList(c0.id), mainFiles, now - 2 * 3600_000L);

        Map<String, String> featureFiles = new LinkedHashMap<>();
        featureFiles.put(file, String.join("\n", oursLines));
        featureFiles.put("README.md", baseReadme);
        GitRepositoryState.Commit f1 = s.addCommit("Start solution",
                Collections.singletonList(c0.id), featureFiles, now - 3600_000L);

        s.branches.put("main", m1.id);
        s.branches.put(featureBranch, f1.id);
        s.remoteBranches.put("main", m1.id);
        s.remoteBranches.put(featureBranch, c0.id);
        s.currentBranch = featureBranch;
        s.checkoutFiles(f1.files);

        return new Scenario(t.title, s, "main", m1.id, file);
    }

    /** Which side of a conflict to keep when resolving automatically. */
    public enum Resolution { OURS, THEIRS, BOTH }

    /**
     * Removes every conflict block from {@code text}, keeping our side, their side, or both (ours first).
     * Text outside conflict blocks is untouched. Returns null for null input.
     */
    public static String resolve(String text, Resolution keep) {
        if (text == null) return null;
        List<String> out = new ArrayList<>();
        int mode = 0; // 0 = normal text, 1 = inside our side, 2 = inside their side
        for (String line : text.split("\n", -1)) {
            if (mode == 0 && line.startsWith(MARK_OURS)) {
                mode = 1;
            } else if (mode == 1 && line.equals(MARK_SEP)) {
                mode = 2;
            } else if (mode == 2 && line.startsWith(MARK_THEIRS)) {
                mode = 0;
            } else if (mode == 0
                    || (mode == 1 && keep != Resolution.THEIRS)
                    || (mode == 2 && keep != Resolution.OURS)) {
                out.add(line);
            }
        }
        return String.join("\n", out);
    }

    public static boolean hasConflictMarkers(String text) {
        if (text == null) return false;
        for (String line : text.split("\n", -1)) {
            if (line.startsWith(MARK_OURS) || line.equals(MARK_SEP) || line.startsWith(MARK_THEIRS)) return true;
        }
        return false;
    }

    /**
     * Line-based 3-way merge. Lines kept by both sides anchor the merge; between anchors, a side
     * that didn't change takes the other's version, identical changes merge, and different changes conflict.
     */
    public static MergeResult merge3(String base, String ours, String theirs, String oursLabel, String theirsLabel) {
        List<String> b = lines(base);
        List<String> o = lines(ours);
        List<String> t = lines(theirs);
        int[] mo = match(b, o);
        int[] mt = match(b, t);

        List<String> out = new ArrayList<>();
        boolean conflict = false;
        int bi = 0, oi = 0, ti = 0;
        while (true) {
            int k = bi;
            while (k < b.size() && !(mo[k] >= 0 && mt[k] >= 0)) k++;
            int oEnd = k < b.size() ? mo[k] : o.size();
            int tEnd = k < b.size() ? mt[k] : t.size();

            List<String> bChunk = b.subList(bi, k);
            List<String> oChunk = o.subList(oi, oEnd);
            List<String> tChunk = t.subList(ti, tEnd);

            if (oChunk.equals(bChunk)) {
                out.addAll(tChunk);
            } else if (tChunk.equals(bChunk) || oChunk.equals(tChunk)) {
                out.addAll(oChunk);
            } else {
                conflict = true;
                out.add(MARK_OURS + oursLabel);
                out.addAll(oChunk);
                out.add(MARK_SEP);
                out.addAll(tChunk);
                out.add(MARK_THEIRS + theirsLabel);
            }

            if (k >= b.size()) break;
            out.add(b.get(k));
            bi = k + 1;
            oi = mo[k] + 1;
            ti = mt[k] + 1;
        }
        return new MergeResult(String.join("\n", out), conflict);
    }

    private static List<String> lines(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\n", -1)));
    }

    /** For each line of {@code a}, the index of its LCS partner in {@code b}, or -1. */
    private static int[] match(List<String> a, List<String> b) {
        int n = a.size(), m = b.size();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                dp[i][j] = a.get(i).equals(b.get(j)) ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }
        int[] res = new int[n];
        Arrays.fill(res, -1);
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (a.get(i).equals(b.get(j))) {
                res[i++] = j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return res;
    }
}
