package za.co.eduvos.codebridge.core.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import za.co.eduvos.codebridge.core.git.MergeConflictGenerator.MergeResult;

public class MergeConflictGeneratorTest {

    @Test
    public void independentEditsMergeCleanly() {
        MergeResult r = MergeConflictGenerator.merge3(
                "a\nb\nc\nd", "a\nB\nc\nd", "a\nb\nc\nD", "HEAD", "main");
        assertFalse(r.conflict);
        assertEquals("a\nB\nc\nD", r.content);
    }

    @Test
    public void sameLineEditedDifferentlyConflictsWithRealMarkers() {
        MergeResult r = MergeConflictGenerator.merge3(
                "a\nb\nc", "a\nours\nc", "a\ntheirs\nc", "HEAD", "main");
        assertTrue(r.conflict);
        assertEquals("a\n<<<<<<< HEAD\nours\n=======\ntheirs\n>>>>>>> main\nc", r.content);
        assertTrue(MergeConflictGenerator.hasConflictMarkers(r.content));
    }

    @Test
    public void identicalChangeOnBothSidesIsNotAConflict() {
        MergeResult r = MergeConflictGenerator.merge3("a\nb", "a\nX", "a\nX", "HEAD", "main");
        assertFalse(r.conflict);
        assertEquals("a\nX", r.content);
    }

    @Test
    public void changeOnOneSideOnlyIsTaken() {
        assertEquals("a\nT", MergeConflictGenerator.merge3("a\nb", "a\nb", "a\nT", "HEAD", "main").content);
        assertEquals("a\nO", MergeConflictGenerator.merge3("a\nb", "a\nO", "a\nb", "HEAD", "main").content);
    }

    @Test
    public void insertionsAndDeletionsAnchorOnUnchangedLines() {
        MergeResult r = MergeConflictGenerator.merge3(
                "1\n2\n3\n4\n5", "1\n2\n2.5\n3\n4\n5", "1\n2\n3\n5", "HEAD", "main");
        assertFalse(r.conflict);
        assertEquals("1\n2\n2.5\n3\n5", r.content);
    }

    @Test
    public void bothAddDifferentContentToEmptyBaseConflicts() {
        MergeResult r = MergeConflictGenerator.merge3("", "mine", "yours", "HEAD", "main");
        assertTrue(r.conflict);
    }

    @Test
    public void markerDetectionIgnoresNormalText() {
        assertFalse(MergeConflictGenerator.hasConflictMarkers("a = b == c\nx >>> 2"));
        assertFalse(MergeConflictGenerator.hasConflictMarkers(null));
        assertTrue(MergeConflictGenerator.hasConflictMarkers("x\n=======\ny"));
    }

    @Test
    public void scenariosAreDeterministicAndEveryOneConflicts() {
        for (long seed = 0; seed < 6; seed++) {
            MergeConflictGenerator.Scenario a = MergeConflictGenerator.generate(seed, () -> 10_000_000L);
            MergeConflictGenerator.Scenario b = MergeConflictGenerator.generate(seed, () -> 10_000_000L);
            assertEquals(a.title, b.title);
            assertEquals(a.state.getHead().id, b.state.getHead().id);

            GitSimulationManager m = new GitSimulationManager(a,
                    new CommandLogger((id, cmd, err, ts) -> { }, 1, () -> 10_000_000L),
                    new GitTelemetryCollector(() -> 10_000_000L), () -> 10_000_000L);
            m.execute("git merge " + a.targetBranch);
            assertTrue("seed " + seed, a.state.getConflictedFiles().contains(a.conflictFile));
        }
    }
}
