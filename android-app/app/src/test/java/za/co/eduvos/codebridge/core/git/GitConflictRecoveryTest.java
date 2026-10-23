package za.co.eduvos.codebridge.core.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import za.co.eduvos.codebridge.core.git.MergeConflictGenerator.Resolution;

/** The resolve helpers, git checkout --ours/--theirs, and recovering from committing markers by mistake. */
public class GitConflictRecoveryTest {

    private static final String FILE = "src/solution.js";
    private static final String CONFLICT =
            "keep\n<<<<<<< HEAD\nmine 1\nmine 2\n=======\ntheirs 1\n>>>>>>> main\nmiddle\n"
          + "<<<<<<< HEAD\nmine B\n=======\ntheirs B\n>>>>>>> main\nend";

    private GitSimulationManager git;

    @Before
    public void setUp() {
        AtomicLong time = new AtomicLong(100_000_000L);
        CommandLogger logger = new CommandLogger((id, cmd, err, ts) -> { }, 1, () -> time.addAndGet(1000));
        git = new GitSimulationManager(MergeConflictGenerator.generate(0, () -> time.get()),
                logger, new GitTelemetryCollector(() -> time.addAndGet(1000)), () -> time.addAndGet(1000));
    }

    private GitSimulationManager.Result run(String cmd) {
        return git.execute(cmd);
    }

    // ---- resolve() ----

    @Test
    public void resolveKeepsOneSideOrBothForEveryBlock() {
        assertEquals("keep\nmine 1\nmine 2\nmiddle\nmine B\nend",
                MergeConflictGenerator.resolve(CONFLICT, Resolution.OURS));
        assertEquals("keep\ntheirs 1\nmiddle\ntheirs B\nend",
                MergeConflictGenerator.resolve(CONFLICT, Resolution.THEIRS));
        assertEquals("keep\nmine 1\nmine 2\ntheirs 1\nmiddle\nmine B\ntheirs B\nend",
                MergeConflictGenerator.resolve(CONFLICT, Resolution.BOTH));
    }

    @Test
    public void resolveLeavesNoMarkersAndIgnoresPlainText() {
        for (Resolution r : Resolution.values()) {
            assertFalse(MergeConflictGenerator.hasConflictMarkers(MergeConflictGenerator.resolve(CONFLICT, r)));
        }
        assertEquals("a\nb", MergeConflictGenerator.resolve("a\nb", Resolution.OURS));
        assertEquals("", MergeConflictGenerator.resolve("", Resolution.THEIRS));
        assertNull(MergeConflictGenerator.resolve(null, Resolution.OURS));
    }

    // ---- git checkout --ours / --theirs ----

    @Test
    public void checkoutTheirsTakesMainsVersionAndCompletesTheTask() {
        run("git merge main");
        assertTrue(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));

        GitSimulationManager.Result r = run("git checkout --theirs " + FILE);
        assertFalse(r.output, r.isError);
        assertFalse(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));
        assertTrue(git.readFile(FILE).contains("need in seen"));            // main's line

        run("git add " + FILE);
        assertFalse(run("git commit -m \"merge\"").isError);
        run("git push");
        assertTrue(git.isTaskComplete());
        assertEquals(2, git.getState().getHead().parents.size());
    }

    @Test
    public void checkoutOursKeepsTheStudentsVersion() {
        run("git merge main");
        assertFalse(run("git checkout --ours " + FILE).isError);
        assertTrue(git.readFile(FILE).contains("seen[need] !== undefined"));   // the feature branch's line
        assertFalse(MergeConflictGenerator.hasConflictMarkers(git.readFile(FILE)));
        run("git add .");
        run("git commit -m \"merge\"");
        run("git push");
        assertTrue(git.isTaskComplete());
    }

    @Test
    public void checkoutSideDoesNotMarkTheFileResolvedUntilItIsAdded() {
        run("git merge main");
        run("git checkout --theirs " + FILE);
        assertTrue(git.getState().getConflictedFiles().contains(FILE));
        assertTrue(run("git commit -m \"too soon\"").isError);
    }

    @Test
    public void checkoutSideReportsMistakes() {
        assertTrue("outside a merge", run("git checkout --theirs " + FILE).isError);
        run("git merge main");
        assertTrue("no path", run("git checkout --ours").isError);
        assertTrue("unknown path", run("git checkout --theirs nope.js").isError);
    }

    // ---- the dead end: staging before editing, then recovering ----

    @Test
    public void stagingEarlyClearsTheConflictButMarkersAreStillReported() {
        run("git merge main");
        assertEquals(1, git.filesWithMarkers().size());

        GitSimulationManager.Result add = run("git add .");
        assertTrue(add.output.contains("still contains conflict markers"));
        assertTrue("git no longer lists a conflict", git.getState().getConflictedFiles().isEmpty());
        assertEquals("but the file still has markers", FILE, git.filesWithMarkers().get(0));
    }

    @Test
    public void committingMarkersByMistakeCanBeFixedWithAFollowUpCommit() {
        run("git merge main");
        run("git add .");
        run("git commit -m \"merge\"");
        run("git push");
        assertFalse("markers were committed", git.isTaskComplete());
        assertTrue(git.hasMergedTarget());

        // Student uses the editor's "keep mine" helper, saves, and finishes properly.
        String fixed = MergeConflictGenerator.resolve(git.readFile(FILE), Resolution.OURS);
        assertTrue(git.writeFile(FILE, fixed));
        assertTrue(git.filesWithMarkers().isEmpty());

        assertFalse(run("git add " + FILE).isError);
        assertFalse(run("git commit -m \"Remove conflict markers\"").isError);
        assertFalse("not pushed yet", git.isTaskComplete());
        run("git push");

        assertTrue(git.isTaskComplete());
        assertEquals("the fix commit has a single parent", 1, git.getState().getHead().parents.size());
    }

    @Test
    public void taskIsNotCompleteBeforeTheMergeEvenIfPushed() {
        assertFalse(git.hasMergedTarget());
        run("git push");
        assertFalse(git.isTaskComplete());
    }
}
