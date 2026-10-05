package za.co.eduvos.codebridge.core.sandbox;

import java.util.Collections;
import java.util.List;

public class TestSuite {

    /** One input/expected pair. Both are the raw strings from the JSON. */
    public static class Case {
        public final String input;
        public final String expected;

        public Case(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
    }

    public final List<Case> cases;
    public final String entryPoint;

    public TestSuite(List<Case> cases, String entryPoint) {
        this.cases = Collections.unmodifiableList(cases);
        this.entryPoint = entryPoint;
    }
}