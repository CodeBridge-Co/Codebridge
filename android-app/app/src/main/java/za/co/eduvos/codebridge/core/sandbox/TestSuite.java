package za.co.eduvos.codebridge.core.sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSuite {

    /** How the student's output is checked. */
    public enum Mode {
        /** Must match exactly (default). */
        EXACT,
        /** Arrays can be in any order. */
        UNORDERED,
        /** A JavaScript function from the problem decides. */
        CHECKER
    }

    /** One input and its expected output (raw text from the JSON). */
    public static class Case {
        public final String input;
        public final String expected;
        /** Hidden cases still count, but the student never sees them. */
        public final boolean hidden;

        public Case(String input, String expected) {
            this(input, expected, false);
        }

        public Case(String input, String expected, boolean hidden) {
            this.input = input;
            this.expected = expected;
            this.hidden = hidden;
        }
    }

    public final List<Case> cases;
    public final String entryPoint;
    public final Mode mode;
    /** Code of check(args, actual). Only used in CHECKER mode. */
    public final String checkerSource;
    /** Minutes allowed for the challenge. 0 means not set. */
    public final int timeLimitMinutes;
    /** Interview questions. Empty means use the default ones. */
    public final List<String> interviewQuestions;

    public TestSuite(List<Case> cases, String entryPoint) {
        this(cases, entryPoint, Mode.EXACT, null, 0);
    }

    public TestSuite(List<Case> cases, String entryPoint, Mode mode, String checkerSource) {
        this(cases, entryPoint, mode, checkerSource, 0);
    }

    public TestSuite(List<Case> cases, String entryPoint, Mode mode, String checkerSource, int timeLimitMinutes) {
        this(cases, entryPoint, mode, checkerSource, timeLimitMinutes, Collections.<String>emptyList());
    }

    public TestSuite(List<Case> cases, String entryPoint, Mode mode, String checkerSource, int timeLimitMinutes,
                     List<String> interviewQuestions) {
        this.interviewQuestions = Collections.unmodifiableList(
                interviewQuestions == null ? new ArrayList<String>() : new ArrayList<>(interviewQuestions));
        this.timeLimitMinutes = Math.max(0, timeLimitMinutes);
        this.cases = Collections.unmodifiableList(cases);
        this.entryPoint = entryPoint;
        this.mode = mode == null ? Mode.EXACT : mode;
        this.checkerSource = checkerSource;
    }

    /** The cases the student is allowed to see. */
    public List<Case> visibleCases() {
        List<Case> out = new ArrayList<>();
        for (Case c : cases) if (!c.hidden) out.add(c);
        return out;
    }

    public int hiddenCount() {
        return cases.size() - visibleCases().size();
    }
}
