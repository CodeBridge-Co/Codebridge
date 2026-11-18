package za.co.eduvos.codebridge.core.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** The commit list on the Git screen is read from GitRepositoryState: history plus what is unpushed. */
public class GitCommitHistoryTest {

    private static final String FILE = "src/solution.js";

    private AtomicLong time;
    private GitSimulationManager git;

    @Before
    public void setUp() {
        time = new AtomicLong(100_000_000L);
        CommandLogger logger = new CommandLogger((id, cmd, err, ts) -> { }, 7, () -> time.addAndGet(1000));
        git = new GitSimulationManager(MergeConflictGenerator.generate(0, () -> time.get()),
                logger, new GitTelemetryCollector(() -> time.addAndGet(1000)), () -> time.addAndGet(1000));
    }

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

    private void mergeAndCommit() {
        git.execute("git merge main");
        git.writeFile(FILE, takeOurs(git.readFile(FILE)));
        git.execute("git add " + FILE);
        git.execute("git commit -m \"Merge main into feature\"");
    }

    @Test
    public void historyIsNewestFirstAndStartsWithTheSeededCommits() {
        GitRepositoryState st = git.getState();
        List<GitRepositoryState.Commit> history = st.getHistory();
        assertFalse(history.isEmpty());
        assertEquals(st.getHead().id, history.get(0).id);
        for (int i = 1; i < history.size(); i++) {
            assertTrue(history.get(i - 1).time >= history.get(i).time);
        }
    }

    @Test
    public void newCommitAppearsAtTheTopAndIsUnpushed() {
        int before = git.getState().getHistory().size();
        mergeAndCommit();
        GitRepositoryState st = git.getState();

        List<GitRepositoryState.Commit> history = st.getHistory();
        assertTrue("merge adds the new commit and brings in main's commits", history.size() > before);
        assertEquals("Merge main into feature", history.get(0).message);
        assertEquals(2, history.get(0).parents.size());
        assertTrue(st.getUnpushedCommitIds().contains(history.get(0).id));
    }

    @Test
    public void unpushedIdsMatchTheCountAndEmptyAfterPush() {
        mergeAndCommit();
        GitRepositoryState st = git.getState();
        assertEquals(st.unpushedCommitCount(), st.getUnpushedCommitIds().size());
        assertTrue(st.unpushedCommitCount() >= 1);

        assertFalse(git.execute("git push").isError);
        assertEquals(0, st.getUnpushedCommitIds().size());
        assertEquals(0, st.unpushedCommitCount());
    }

    @Test
    public void gitLogListsTheSameCommitsAsTheScreen() {
        mergeAndCommit();
        List<GitRepositoryState.Commit> history = git.getState().getHistory();
        String[] logLines = git.execute("git log --oneline").output.split("\n");
        assertEquals(history.size(), logLines.length);
        assertTrue(logLines[0].contains("Merge main into feature"));
        assertTrue(logLines[0].startsWith(history.get(0).shortId()));
    }
}
