package za.co.eduvos.codebridge.core.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GitSimulationManagerTest {

    private static final String FILE = "src/solution.js";

    private AtomicLong time;
    private List<String> logged;
    private List<Boolean> loggedErrors;
    private GitSimulationManager git;

    @Before
    public void setUp() {
        time = new AtomicLong(100_000_000L);
        logged = new ArrayList<>();
        loggedErrors = new ArrayList<>();
        CommandLogger logger = new CommandLogger((id, cmd, err, ts) -> {
            logged.add(cmd);
            loggedErrors.add(err);
        }, 7, () -> time.addAndGet(1000));
        git = new GitSimulationManager(MergeConflictGenerator.generate(0, () -> time.get()),
                logger, new GitTelemetryCollector(() -> time.addAndGet(1000)), () -> time.addAndGet(1000));
    }

    private GitSimulationManager.Result run(String cmd) {
        return git.execute(cmd);
    }

    /** Keeps our side of every conflict block and drops the markers. */
    private static String takeOurs(String text) {
        StringBuilder out = new StringBuilder();
        int mode = 0;
        for (String line : text.split("\n", -1)) {
            if (line.startsWith("<<<<<<< ")) mode = 1;
            else if (line.equals("=======") && mode == 1) mode = 2;
            else if (line.startsWith(">>>>>>> ")) mode = 0;
            else if (mode != 2) out.append(out.length() == 0 && line.isEmpty() ? "" : "\n").append(line);
        }
        return out.toString().replaceFirst("^\n", "");
    }

    @Test
    public void startsCleanOnTheFeatureBranch() {
        GitSimulationManager.Result r = run("git status");
        assertFalse(r.isError);
        assertTrue(r.output.contains("On branch feature/two-sum"));
        assertTrue(r.output.contains("nothing to commit, working tree clean"));
    }

    @Test
    public void mergeProducesRealConflictAndStagesTheCleanFile() {
        GitSimulationManager.Result r = run("git merge main");
        assertFalse(r.isError);
        assertTrue(r.output.contains("CONFLICT (content): Merge conflict in " + FILE));
        assertTrue(r.output.contains("Automatic merge failed"));

        assertTrue(git.getState().isMerging());
        assertTrue(git.getState().getConflictedFiles().contains(FILE));
        assertTrue(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));
        // README changed only on main, so it merges cleanly and is staged.
        assertTrue(git.readFile("README.md").contains("Aim for O(n)"));
        assertTrue(run("git status").output.contains("both modified:   " + FILE));
        assertTrue(run("git status").output.contains("modified:   README.md"));
    }

    @Test
    public void cannotCommitWhileConflictsRemain() {
        run("git merge main");
        GitSimulationManager.Result r = run("git commit -m \"oops\"");
        assertTrue(r.isError);
        assertTrue(r.output.contains("unmerged files"));
    }

    @Test
    public void fullWorkflowResolveCommitAndPush() {
        run("git merge main");
        assertTrue(git.writeFile(FILE, takeOurs(git.readFile(FILE))));
        assertFalse(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));

        // Editing is not enough; git still considers the file unmerged until it is added.
        assertTrue(git.getState().getConflictedFiles().contains(FILE));
        GitSimulationManager.Result add = run("git add " + FILE);
        assertFalse(add.isError);
        assertEquals("", add.output);
        assertTrue(run("git status").output.contains("All conflicts fixed but you are still merging"));

        GitSimulationManager.Result commit = run("git commit -m \"Merge main into feature\"");
        assertFalse(commit.output, commit.isError);
        assertTrue(commit.output.startsWith("[feature/two-sum "));
        assertEquals(2, git.getState().getHead().parents.size());
        assertFalse(git.getState().isMerging());
        assertFalse("not pushed yet", git.isTaskComplete());

        GitSimulationManager.Result push = run("git push");
        assertFalse(push.output, push.isError);
        assertTrue(push.output.contains("feature/two-sum -> feature/two-sum"));
        assertTrue(git.isTaskComplete());
        assertEquals("Everything up-to-date", run("git push origin feature/two-sum").output);
    }

    @Test
    public void commitWithoutMessageDuringMergeUsesDefaultMergeMessage() {
        run("git merge main");
        git.writeFile(FILE, takeOurs(git.readFile(FILE)));
        run("git add .");
        GitSimulationManager.Result r = run("git commit");
        assertFalse(r.output, r.isError);
        assertTrue(git.getState().getHead().message.startsWith("Merge branch 'main'"));
    }

    @Test
    public void stagingFileThatStillHasMarkersWarnsAndTheTaskIsNotComplete() {
        run("git merge main");
        GitSimulationManager.Result add = run("git add .");
        assertTrue(add.output.contains("still contains conflict markers"));
        run("git commit -m \"merge\"");
        run("git push");
        assertFalse("markers were committed", git.isTaskComplete());
        assertEquals(1, git.getTelemetry().snapshot().markersStaged);
    }

    @Test
    public void mergeAbortRestoresTheCleanTree() {
        run("git merge main");
        assertFalse(run("git merge --abort").isError);
        assertFalse(git.getState().isMerging());
        assertTrue(git.getState().getConflictedFiles().isEmpty());
        assertFalse(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));
        assertTrue(run("git status").output.contains("nothing to commit"));
        assertTrue(run("git merge --abort").isError);
    }

    @Test
    public void cannotStartASecondMergeOrSwitchBranchMidConflict() {
        run("git merge main");
        assertTrue(run("git merge main").isError);
        assertTrue(run("git checkout main").isError);
    }

    @Test
    public void addValidatesPaths() {
        assertTrue(run("git add nope.txt").output.contains("did not match any files"));
        assertTrue(run("git add").output.contains("Nothing specified"));
    }

    @Test
    public void nothingToCommitIsAnError() {
        GitSimulationManager.Result r = run("git commit -m \"empty\"");
        assertTrue(r.isError);
        assertTrue(r.output.contains("nothing to commit"));
    }

    @Test
    public void normalEditStageCommitCycle() {
        git.writeFile("README.md", "# changed");
        assertTrue(run("git status").output.contains("Changes not staged for commit"));
        assertEquals(1, git.getState().unstagedCount());
        run("git add README.md");
        assertEquals(1, git.getState().stagedCount());
        assertTrue(run("git commit").isError);                       // needs -m outside a merge
        assertFalse(run("git commit -m \"readme\"").isError);
        assertEquals(1, git.getState().unpushedCommitCount() - 1);   // the earlier "Start solution" is unpushed too
        assertTrue(run("git log --oneline").output.startsWith(git.getState().getHead().shortId() + " readme"));
    }

    @Test
    public void commitDashAStagesTrackedChanges() {
        git.writeFile("README.md", "# changed again");
        assertFalse(run("git commit -am \"tracked\"").isError);
        assertEquals("# changed again", git.getState().getHead().files.get("README.md"));
    }

    @Test
    public void branchingAndSwitching() {
        assertFalse(run("git checkout -b experiment").isError);
        assertTrue(run("git branch").output.contains("* experiment"));
        assertTrue(run("git checkout experiment").output.contains("Already on"));
        assertFalse(run("git switch main").isError);
        assertEquals("main", git.getState().getCurrentBranch());
        assertTrue(run("git checkout missing").isError);
        assertTrue(run("git branch main").isError);
    }

    @Test
    public void switchingWithDirtyTreeIsRefused() {
        git.writeFile("README.md", "dirty");
        assertTrue(run("git checkout main").isError);
    }

    @Test
    public void fastForwardAndUpToDateMerges() {
        run("git checkout main");
        GitSimulationManager.Result ff = run("git merge feature/two-sum");
        // main and feature have diverged, so this is a real merge, not a fast-forward
        assertTrue(ff.output.contains("CONFLICT"));
        run("git merge --abort");
        assertEquals("Already up to date.", run("git merge main").output);
    }

    @Test
    public void nonFastForwardPushIsRejected() {
        GitRepositoryState s = git.getState();
        // Pretend someone else already pushed a different commit to origin's copy of this branch.
        s.remoteBranches.put(s.getCurrentBranch(), s.branches.get("main"));
        GitSimulationManager.Result r = run("git push");
        assertTrue(r.isError);
        assertTrue(r.output.contains("non-fast-forward"));
    }

    @Test
    public void pushToUnknownRemoteFails() {
        assertTrue(run("git push upstream").output.contains("does not appear to be a git repository"));
    }

    @Test
    public void unsupportedAndUnknownCommandsAreErrorsAndCounted() {
        assertTrue(run("git rebase main").isError);
        assertTrue(run("git frobnicate").isError);
        assertTrue(run("rm -rf /").output.contains("command not found"));
        assertEquals(1, git.getTelemetry().snapshot().unsupportedCommands);
    }

    @Test
    public void shellHelpersListAndShowFiles() {
        assertTrue(run("ls").output.contains(FILE));
        assertTrue(run("cat " + FILE).output.contains("function"));
        assertTrue(run("cat missing.txt").isError);
    }

    @Test
    public void blankLineDoesNothingAndIsNotLogged() {
        assertEquals("", run("   ").output);
        assertTrue(logged.isEmpty());
    }

    @Test
    public void everyCommandIsLoggedWithItsErrorFlagAndSession() {
        run("git status");
        run("git commit -m \"x\"");
        assertEquals(2, logged.size());
        assertEquals("git status", logged.get(0));
        assertFalse(loggedErrors.get(0));
        assertTrue(loggedErrors.get(1));
    }

    @Test
    public void telemetryTracksConflictLifecycle() {
        run("git merge main");
        GitTelemetryCollector.Summary mid = git.getTelemetry().snapshot();
        assertTrue(mid.conflictEncountered);
        assertFalse(mid.conflictResolved);
        assertEquals(-1, mid.timeToResolveMs);

        run("git commit -m \"too early\"");           // one real mistake
        git.writeFile(FILE, takeOurs(git.readFile(FILE)));
        run("git add " + FILE);
        run("git commit -m \"merged\"");
        run("git push");

        GitTelemetryCollector.Summary end = git.getTelemetry().snapshot();
        assertTrue(end.conflictResolved);
        assertTrue(end.timeToResolveMs > 0);
        assertTrue(end.taskCompleted);
        assertTrue(end.timeToCompleteMs > 0);
        assertEquals(1, end.errorCommands);
        assertEquals(5, end.totalCommands);
    }

    @Test
    public void logShowsMergeCommitFirstWithHeadMarker() {
        run("git merge main");
        git.writeFile(FILE, takeOurs(git.readFile(FILE)));
        run("git add .");
        run("git commit -m \"merged\"");
        String full = run("git log").output;
        assertTrue(full.startsWith("commit " + git.getState().getHead().id + " (HEAD -> feature/two-sum)"));
        assertTrue(full.contains("    merged"));
        assertTrue(run("git log --oneline").output.split("\n").length >= 4);
    }
}
