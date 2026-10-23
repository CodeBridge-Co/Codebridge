package za.co.eduvos.codebridge.core.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.LongSupplier;

import za.co.eduvos.codebridge.core.git.CommandParser.Command;
import za.co.eduvos.codebridge.core.git.GitRepositoryState.Commit;

/**
 * Runs terminal lines against a simulated repository. Supported: git status / add / commit / branch /
 * checkout (switch) / merge (incl. --abort) / log / push, plus ls and cat. Output mirrors real git
 * closely enough to teach from. All methods are synchronized; call from any thread.
 */
public class GitSimulationManager {

    public static final class Result {
        public final String output;
        public final boolean isError;

        Result(String output, boolean isError) {
            this.output = output;
            this.isError = isError;
        }
    }

    private final MergeConflictGenerator.Scenario scenario;
    private final GitRepositoryState s;
    private final CommandLogger logger;
    private final GitTelemetryCollector telemetry;
    private final LongSupplier clock;

    public GitSimulationManager(MergeConflictGenerator.Scenario scenario, CommandLogger logger,
                                GitTelemetryCollector telemetry, LongSupplier clock) {
        this.scenario = scenario;
        this.s = scenario.state;
        this.logger = logger;
        this.telemetry = telemetry;
        this.clock = clock;
    }

    public GitRepositoryState getState() { return s; }

    public MergeConflictGenerator.Scenario getScenario() { return scenario; }

    public GitTelemetryCollector getTelemetry() { return telemetry; }

    // ---- file access for the conflict editor ----

    public synchronized String readFile(String path) {
        return s.workingTree.get(path);
    }

    /** Saves edited content for an existing file. Conflicts stay open until the file is staged with git add. */
    public synchronized boolean writeFile(String path, String content) {
        if (!s.workingTree.containsKey(path)) return false;
        s.workingTree.put(path, content);
        return true;
    }

    /** True once the merge is committed without leftover markers and pushed. */
    public synchronized boolean isTaskComplete() {
        if (s.isMerging() || !s.conflicted.isEmpty()) return false;
        Commit head = s.getHead();
        // The target branch must be merged in, but HEAD need not be the merge commit itself:
        // a student who committed markers by mistake can finish with a follow-up fix commit.
        if (!s.isAncestor(scenario.targetTip, head.id)) return false;
        for (String content : head.files.values()) {
            if (MergeConflictGenerator.hasConflictMarkers(content)) return false;
        }
        return s.isPushed();
    }

    /** True once the target branch's tip is part of the current branch's history. */
    public synchronized boolean hasMergedTarget() {
        return s.isAncestor(scenario.targetTip, s.getHead().id);
    }

    /** Files in the working tree that still contain conflict markers, even after they were staged. */
    public synchronized List<String> filesWithMarkers() {
        List<String> found = new ArrayList<>();
        for (Map.Entry<String, String> e : s.workingTree.entrySet()) {
            if (MergeConflictGenerator.hasConflictMarkers(e.getValue())) found.add(e.getKey());
        }
        return found;
    }

    // ---- command execution ----

    public synchronized Result execute(String line) {
        String trimmed = line == null ? "" : line.trim();
        Command cmd;
        try {
            cmd = CommandParser.parse(trimmed);
        } catch (CommandParser.ParseException e) {
            if (e.getMessage().isEmpty()) return new Result("", false);
            return record(trimmed, new Result(e.getMessage(), true), false);
        }

        int before = s.conflicted.size();
        Result r = run(cmd);
        int after = s.conflicted.size();
        if (before == 0 && after > 0) telemetry.onConflictEntered();
        if (before > 0 && after == 0) telemetry.onConflictsCleared();

        record(trimmed, r, cmd.verb == CommandParser.Verb.UNSUPPORTED);
        if (isTaskComplete()) telemetry.onTaskCompleted();
        return r;
    }

    private Result record(String line, Result r, boolean unsupported) {
        logger.log(line, r.isError);
        telemetry.onCommand(r.isError, unsupported);
        return r;
    }

    private Result run(Command c) {
        switch (c.verb) {
            case STATUS: return status();
            case ADD: return add(c);
            case COMMIT: return commit(c);
            case BRANCH: return branch(c);
            case CHECKOUT: return checkout(c);
            case MERGE: return merge(c);
            case LOG: return log(c);
            case PUSH: return push(c);
            case LS: return new Result(String.join("\n", s.workingTree.keySet()), false);
            case CAT: return cat(c);
            default:
                return new Result("git: '" + c.name + "' is not available in this simulator.", true);
        }
    }

    // ---- status ----

    private Result status() {
        StringBuilder sb = new StringBuilder("On branch ").append(s.currentBranch).append('\n');
        if (s.isMerging()) {
            if (!s.conflicted.isEmpty()) {
                sb.append("You have unmerged paths.\n  (fix conflicts and run \"git commit\")\n")
                  .append("  (use \"git merge --abort\" to abort the merge)\n");
            } else {
                sb.append("All conflicts fixed but you are still merging.\n  (use \"git commit\" to conclude merge)\n");
            }
        }

        Map<String, String> headFiles = s.getHead().files;
        List<String> staged = new ArrayList<>();
        for (Map.Entry<String, String> e : s.index.entrySet()) {
            if (s.conflicted.contains(e.getKey())) continue;
            String inHead = headFiles.get(e.getKey());
            if (inHead == null) staged.add("new file:   " + e.getKey());
            else if (!inHead.equals(e.getValue())) staged.add("modified:   " + e.getKey());
        }
        List<String> unstaged = new ArrayList<>();
        List<String> untracked = new ArrayList<>();
        for (Map.Entry<String, String> e : s.workingTree.entrySet()) {
            String p = e.getKey();
            if (s.conflicted.contains(p)) continue;
            if (!s.index.containsKey(p)) untracked.add(p);
            else if (!s.index.get(p).equals(e.getValue())) unstaged.add("modified:   " + p);
        }

        if (!staged.isEmpty()) section(sb, "Changes to be committed:", staged);
        if (!s.conflicted.isEmpty()) {
            List<String> unmerged = new ArrayList<>();
            for (String p : s.conflicted) unmerged.add("both modified:   " + p);
            section(sb, "Unmerged paths:\n  (use \"git add <file>...\" to mark resolution)", unmerged);
        }
        if (!unstaged.isEmpty()) section(sb, "Changes not staged for commit:", unstaged);
        if (!untracked.isEmpty()) section(sb, "Untracked files:", untracked);
        if (staged.isEmpty() && unstaged.isEmpty() && untracked.isEmpty() && s.conflicted.isEmpty() && !s.isMerging()) {
            sb.append("nothing to commit, working tree clean");
        }
        return new Result(sb.toString().trim(), false);
    }

    private static void section(StringBuilder sb, String title, List<String> items) {
        sb.append('\n').append(title).append('\n');
        for (String i : items) sb.append('\t').append(i).append('\n');
    }

    // ---- add ----

    private Result add(Command c) {
        if (c.args.isEmpty()) {
            return new Result("Nothing specified, nothing added.\nhint: Maybe you wanted to say 'git add .'?", false);
        }
        List<String> paths = new ArrayList<>();
        for (String a : c.args) {
            if (a.equals(".") || a.equals("-A")) {
                paths.addAll(s.workingTree.keySet());
            } else if (s.workingTree.containsKey(a)) {
                paths.add(a);
            } else {
                return new Result("fatal: pathspec '" + a + "' did not match any files", true);
            }
        }
        StringBuilder warn = new StringBuilder();
        for (String p : new TreeSet<>(paths)) {
            String content = s.workingTree.get(p);
            s.index.put(p, content);
            if (s.conflicted.remove(p) && MergeConflictGenerator.hasConflictMarkers(content)) {
                telemetry.onMarkersStaged();
                warn.append("warning: ").append(p).append(" still contains conflict markers\n");
            }
        }
        return new Result(warn.toString().trim(), false);
    }

    // ---- commit ----

    private Result commit(Command c) {
        if (!s.conflicted.isEmpty()) {
            return new Result("error: Committing is not possible because you have unmerged files.\n"
                    + "hint: Fix them up in the work tree, and then use 'git add <file>'\n"
                    + "hint: as appropriate to mark resolution and make a commit.\n"
                    + "fatal: Exiting because of an unresolved conflict.", true);
        }
        if (c.has("a")) {
            for (String p : new ArrayList<>(s.index.keySet())) {
                if (s.workingTree.containsKey(p)) s.index.put(p, s.workingTree.get(p));
            }
        }
        if (s.stagedCount() == 0 && !s.isMerging()) {
            if (s.unstagedCount() > 0) {
                return new Result("no changes added to commit (use \"git add\" and/or \"git commit -a\")", true);
            }
            return new Result("nothing to commit, working tree clean", true);
        }
        String message = c.message;
        if (message == null && s.isMerging()) message = "Merge branch '" + s.mergeBranch + "'";
        if (message == null || message.trim().isEmpty()) {
            return new Result("error: commit message required. Use: git commit -m \"your message\"", true);
        }

        List<String> parents = new ArrayList<>();
        parents.add(s.branches.get(s.currentBranch));
        if (s.isMerging()) parents.add(s.mergeHead);
        int changed = s.stagedCount();
        Commit created = s.addCommit(message, parents, s.index, clock.getAsLong());
        s.branches.put(s.currentBranch, created.id);
        s.mergeHead = null;
        s.mergeBranch = null;
        return new Result("[" + s.currentBranch + " " + created.shortId() + "] " + message
                + (changed > 0 ? "\n " + changed + (changed == 1 ? " file changed" : " files changed") : ""), false);
    }

    // ---- branch / checkout ----

    private Result branch(Command c) {
        if (c.args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String b : s.branches.keySet()) {
                sb.append(b.equals(s.currentBranch) ? "* " : "  ").append(b).append('\n');
            }
            return new Result(sb.toString().trim(), false);
        }
        String name = c.args.get(0);
        if (s.branches.containsKey(name)) return new Result("fatal: a branch named '" + name + "' already exists", true);
        s.branches.put(name, s.branches.get(s.currentBranch));
        return new Result("", false);
    }

    /** git checkout --ours|--theirs <file>: take one side's version of a file during a merge. */
    private Result checkoutSide(Command c) {
        boolean ours = c.has("ours");
        if (c.args.isEmpty()) {
            return new Result("error: --" + (ours ? "ours" : "theirs") + " needs a file path, e.g. src/solution.js", true);
        }
        if (!s.isMerging()) {
            return new Result("error: --ours/--theirs only works while a merge is in progress. "
                    + "Edit the file instead.", true);
        }
        String path = c.args.get(0);
        Commit side = ours ? s.getHead() : s.commits.get(s.mergeHead);
        String content = side.files.get(path);
        if (content == null) {
            return new Result("error: path '" + path + "' does not have " + (ours ? "our" : "their") + " version", true);
        }
        s.workingTree.put(path, content);
        return new Result("Updated 1 path from the index", false);
    }

    private Result checkout(Command c) {
        if (c.has("ours") || c.has("theirs")) return checkoutSide(c);
        if (c.args.isEmpty()) return new Result("error: branch name required", true);
        String name = c.args.get(0);
        boolean create = c.has("b") || c.has("c");

        if (!create && !s.branches.containsKey(name)) {
            return new Result("error: pathspec '" + name + "' did not match any file(s) known to git", true);
        }
        if (create && s.branches.containsKey(name)) {
            return new Result("fatal: a branch named '" + name + "' already exists", true);
        }
        if (!create && name.equals(s.currentBranch)) return new Result("Already on '" + name + "'", false);
        if (!s.conflicted.isEmpty()) {
            return new Result("error: you need to resolve your current index first", true);
        }
        if (isDirty()) {
            return new Result("error: Your local changes would be overwritten by checkout.\n"
                    + "Please commit your changes before you switch branches.", true);
        }
        if (create) {
            s.branches.put(name, s.branches.get(s.currentBranch));
            s.currentBranch = name;
            return new Result("Switched to a new branch '" + name + "'", false);
        }
        s.currentBranch = name;
        s.checkoutFiles(s.getHead().files);
        return new Result("Switched to branch '" + name + "'", false);
    }

    private boolean isDirty() {
        return !s.workingTree.equals(s.index) || !s.index.equals(s.getHead().files);
    }

    // ---- merge ----

    private Result merge(Command c) {
        if (c.has("abort")) {
            if (!s.isMerging()) return new Result("fatal: There is no merge to abort (MERGE_HEAD missing).", true);
            s.checkoutFiles(s.getHead().files);
            s.conflicted.clear();
            s.mergeHead = null;
            s.mergeBranch = null;
            return new Result("", false);
        }
        if (c.args.isEmpty()) return new Result("fatal: No remote for the current branch.", true);
        String name = c.args.get(0);
        String targetId = s.branches.get(name);
        if (targetId == null) return new Result("merge: " + name + " - not something we can merge", true);
        if (s.isMerging()) {
            return new Result("fatal: You have not concluded your merge (MERGE_HEAD exists).\n"
                    + "Please, commit your changes before you merge.", true);
        }
        if (isDirty()) {
            return new Result("error: Your local changes would be overwritten by merge.\n"
                    + "Please commit your changes before you merge.\nAborting", true);
        }

        String headId = s.branches.get(s.currentBranch);
        if (s.isAncestor(targetId, headId)) return new Result("Already up to date.", false);
        if (s.isAncestor(headId, targetId)) {
            s.branches.put(s.currentBranch, targetId);
            s.checkoutFiles(s.commits.get(targetId).files);
            return new Result("Updating " + s.commits.get(headId).shortId() + ".." + s.commits.get(targetId).shortId()
                    + "\nFast-forward", false);
        }

        Map<String, String> base = s.commits.get(s.mergeBase(headId, targetId)).files;
        Map<String, String> ours = s.commits.get(headId).files;
        Map<String, String> theirs = s.commits.get(targetId).files;

        Set<String> paths = new TreeSet<>();
        paths.addAll(base.keySet());
        paths.addAll(ours.keySet());
        paths.addAll(theirs.keySet());

        Map<String, String> merged = new TreeMap<>();
        Map<String, String> staged = new TreeMap<>();
        Set<String> conflicts = new TreeSet<>();
        StringBuilder out = new StringBuilder();

        for (String p : paths) {
            String b = base.get(p), o = ours.get(p), t = theirs.get(p);
            if (eq(o, t)) {
                put(merged, staged, p, o, o);
            } else if (eq(b, o)) {
                put(merged, staged, p, t, t);
            } else if (eq(b, t)) {
                put(merged, staged, p, o, o);
            } else {
                out.append("Auto-merging ").append(p).append('\n');
                MergeConflictGenerator.MergeResult r = MergeConflictGenerator.merge3(
                        b == null ? "" : b, o == null ? "" : o, t == null ? "" : t, "HEAD", name);
                if (r.conflict) {
                    conflicts.add(p);
                    out.append("CONFLICT (content): Merge conflict in ").append(p).append('\n');
                    put(merged, staged, p, r.content, o);
                } else {
                    put(merged, staged, p, r.content, r.content);
                }
            }
        }

        s.workingTree.clear();
        s.workingTree.putAll(merged);
        s.index.clear();
        s.index.putAll(staged);

        if (!conflicts.isEmpty()) {
            s.conflicted.addAll(conflicts);
            s.mergeHead = targetId;
            s.mergeBranch = name;
            out.append("Automatic merge failed; fix conflicts and then commit the result.");
            return new Result(out.toString().trim(), false);
        }

        String msg = "Merge branch '" + name + "' into " + s.currentBranch;
        Commit mc = s.addCommit(msg, java.util.Arrays.asList(headId, targetId), s.index, clock.getAsLong());
        s.branches.put(s.currentBranch, mc.id);
        out.append("Merge made by the 'ort' strategy.");
        return new Result(out.toString().trim(), false);
    }

    /** null content means the file doesn't exist on that side (deleted). */
    private static void put(Map<String, String> work, Map<String, String> index, String path, String w, String i) {
        if (w != null) work.put(path, w);
        if (i != null) index.put(path, i);
    }

    private static boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    // ---- log / push / cat ----

    private Result log(Command c) {
        List<Commit> history = s.getHistory();
        StringBuilder sb = new StringBuilder();
        boolean oneline = c.has("oneline");
        for (int i = 0; i < history.size(); i++) {
            Commit k = history.get(i);
            if (oneline) {
                sb.append(k.shortId()).append(' ').append(k.message).append('\n');
            } else {
                sb.append("commit ").append(k.id);
                if (i == 0) sb.append(" (HEAD -> ").append(s.currentBranch).append(')');
                sb.append("\nAuthor: You <you@codebridge.app>\nDate:   ").append(new Date(k.time))
                  .append("\n\n    ").append(k.message).append("\n\n");
            }
        }
        return new Result(sb.toString().trim(), false);
    }

    private Result push(Command c) {
        List<String> args = c.args;
        if (!args.isEmpty() && !args.get(0).equals(GitRepositoryState.REMOTE)) {
            return new Result("fatal: '" + args.get(0) + "' does not appear to be a git repository", true);
        }
        String branch = args.size() >= 2 ? args.get(1) : s.currentBranch;
        String local = s.branches.get(branch);
        if (local == null) return new Result("error: src refspec " + branch + " does not match any", true);

        String remote = s.remoteBranches.get(branch);
        if (local.equals(remote)) return new Result("Everything up-to-date", false);
        if (remote != null && !s.isAncestor(remote, local)) {
            return new Result("To " + GitRepositoryState.REMOTE + "\n ! [rejected]        " + branch + " -> " + branch
                    + " (non-fast-forward)\nerror: failed to push some refs to '" + GitRepositoryState.REMOTE + "'\n"
                    + "hint: Updates were rejected because the remote contains work that you do not have locally.", true);
        }
        s.remoteBranches.put(branch, local);
        String range = remote == null
                ? " * [new branch]      " + branch + " -> " + branch
                : "   " + s.commits.get(remote).shortId() + ".." + s.commits.get(local).shortId() + "  " + branch + " -> " + branch;
        return new Result("To " + GitRepositoryState.REMOTE + "\n" + range, false);
    }

    private Result cat(Command c) {
        if (c.args.isEmpty()) return new Result("cat: missing file operand", true);
        String content = s.workingTree.get(c.args.get(0));
        if (content == null) return new Result("cat: " + c.args.get(0) + ": No such file or directory", true);
        return new Result(content, false);
    }

    /** Convenience for the UI. */
    public synchronized List<String> conflictedFiles() {
        return new ArrayList<>(Collections.unmodifiableSet(s.conflicted));
    }
}
