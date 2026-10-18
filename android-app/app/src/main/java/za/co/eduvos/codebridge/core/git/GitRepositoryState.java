package za.co.eduvos.codebridge.core.git;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * In-memory model of one repository: commits, branches, working tree, index and one remote ("origin").
 * Mutated only by GitSimulationManager; the UI reads it through the public getters.
 */
public class GitRepositoryState {

    public static final String REMOTE = "origin";

    public static class Commit {
        public final String id;
        public final String message;
        public final List<String> parents;
        public final Map<String, String> files;
        public final long time;

        Commit(String id, String message, List<String> parents, Map<String, String> files, long time) {
            this.id = id;
            this.message = message;
            this.parents = Collections.unmodifiableList(new ArrayList<>(parents));
            this.files = Collections.unmodifiableMap(new TreeMap<>(files));
            this.time = time;
        }

        public String shortId() {
            return id.substring(0, 7);
        }
    }

    final Map<String, Commit> commits = new LinkedHashMap<>();
    final Map<String, String> branches = new TreeMap<>();
    /** origin's branch tips: branch name to commit id. */
    final Map<String, String> remoteBranches = new HashMap<>();
    final Map<String, String> workingTree = new TreeMap<>();
    final Map<String, String> index = new TreeMap<>();
    final Set<String> conflicted = new TreeSet<>();
    String currentBranch;
    /** Commit being merged in while a conflicted merge is open, else null. */
    String mergeHead;
    String mergeBranch;
    private int commitCounter;

    // ---- read access for the UI ----

    public String getCurrentBranch() { return currentBranch; }

    public boolean isMerging() { return mergeHead != null; }

    public Set<String> getConflictedFiles() { return Collections.unmodifiableSet(conflicted); }

    public Map<String, String> getWorkingFiles() { return Collections.unmodifiableMap(workingTree); }

    public List<String> getBranchNames() { return new ArrayList<>(branches.keySet()); }

    public Commit getHead() { return commits.get(branches.get(currentBranch)); }

    /** Commits reachable from HEAD, newest first. */
    public List<Commit> getHistory() {
        List<Commit> out = new ArrayList<>();
        for (String id : reachable(branches.get(currentBranch))) out.add(commits.get(id));
        Collections.sort(out, (a, b) -> {
            int byTime = Long.compare(b.time, a.time);
            return byTime != 0 ? byTime : Integer.compare(order(b.id), order(a.id));
        });
        return out;
    }

    /** Changed-file counts the Git screen shows: unstaged, staged, commits not yet pushed, pushed flag. */
    public int unstagedCount() {
        Set<String> paths = new TreeSet<>();
        for (String p : workingTree.keySet()) {
            if (!conflicted.contains(p) && !workingTree.get(p).equals(index.get(p))) paths.add(p);
        }
        paths.addAll(conflicted);
        return paths.size();
    }

    public int stagedCount() {
        Map<String, String> headFiles = getHead().files;
        int n = 0;
        for (String p : index.keySet()) {
            if (!conflicted.contains(p) && !index.get(p).equals(headFiles.get(p))) n++;
        }
        return n;
    }

    public int unpushedCommitCount() {
        String remoteTip = remoteBranches.get(currentBranch);
        Set<String> remoteSet = remoteTip == null ? new HashSet<String>() : reachable(remoteTip);
        int n = 0;
        for (String id : reachable(branches.get(currentBranch))) if (!remoteSet.contains(id)) n++;
        return n;
    }

    public boolean isPushed() {
        String remoteTip = remoteBranches.get(currentBranch);
        return remoteTip != null && remoteTip.equals(branches.get(currentBranch));
    }

    // ---- internals used by the simulator ----

    Commit addCommit(String message, List<String> parents, Map<String, String> files, long time) {
        commitCounter++;
        StringBuilder seed = new StringBuilder(message).append('|').append(commitCounter);
        for (String p : parents) seed.append('|').append(p);
        for (Map.Entry<String, String> e : new TreeMap<>(files).entrySet()) {
            seed.append('|').append(e.getKey()).append('=').append(e.getValue());
        }
        Commit c = new Commit(sha1(seed.toString()), message, parents, files, time);
        commits.put(c.id, c);
        return c;
    }

    void checkoutFiles(Map<String, String> files) {
        workingTree.clear();
        workingTree.putAll(files);
        index.clear();
        index.putAll(files);
    }

    /** All commit ids reachable from {@code id}, including itself. */
    Set<String> reachable(String id) {
        Set<String> seen = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (id != null) queue.add(id);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (!seen.add(cur)) continue;
            queue.addAll(commits.get(cur).parents);
        }
        return seen;
    }

    boolean isAncestor(String ancestor, String descendant) {
        return reachable(descendant).contains(ancestor);
    }

    /** Nearest commit reachable from both, or null. */
    String mergeBase(String a, String b) {
        Set<String> fromA = reachable(a);
        Deque<String> queue = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        queue.add(b);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            if (!seen.add(cur)) continue;
            if (fromA.contains(cur)) return cur;
            queue.addAll(commits.get(cur).parents);
        }
        return null;
    }

    private int order(String id) {
        int i = 0;
        for (String k : commits.keySet()) {
            if (k.equals(id)) return i;
            i++;
        }
        return -1;
    }

    private static String sha1(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
