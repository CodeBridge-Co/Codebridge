package za.co.eduvos.codebridge.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import za.co.eduvos.codebridge.core.sandbox.CodeExecutionEngine;
import za.co.eduvos.codebridge.core.sandbox.ExecutionResult;

/** Proves every bundled problem has a valid suite and that a correct solution passes it. */
public class ProblemRepositoryTest {

    private static final Map<Integer, String> REFERENCE = new HashMap<>();

    static {
        REFERENCE.put(1, "function twoSum(nums, target) { var m = {};"
                + " for (var i = 0; i < nums.length; i++) {"
                + " var j = m[target - nums[i]]; if (j !== undefined) return [j, i]; m[nums[i]] = i; } }");
        REFERENCE.put(2, "function reverseList(head) { return head.slice().reverse(); }");
        REFERENCE.put(3, "function isValid(s) { var st = [], p = {')':'(',']':'[','}':'{'};"
                + " for (var i = 0; i < s.length; i++) { var c = s[i];"
                + " if (p[c]) { if (st.pop() !== p[c]) return false; } else st.push(c); }"
                + " return st.length === 0; }");
        REFERENCE.put(4, "function mergeTwoLists(a, b) { return a.concat(b).sort(function(x, y) { return x - y; }); }");
        REFERENCE.put(5, "function maxArea(h) { var l = 0, r = h.length - 1, best = 0;"
                + " while (l < r) { best = Math.max(best, Math.min(h[l], h[r]) * (r - l));"
                + " if (h[l] < h[r]) l++; else r--; } return best; }");
    }

    @Test
    public void everyProblemHasAReferenceSolutionThatPasses() {
        CodeExecutionEngine engine = new CodeExecutionEngine();
        try {
            for (ProblemDetail p : ProblemRepository.INSTANCE.getProblems()) {
                String code = REFERENCE.get(p.getId());
                assertTrue("no reference for problem " + p.getId(), code != null);
                ExecutionResult r = engine.execute(code, p.getTestCasesJson());
                assertNull(p.getTitle() + ": " + r.fatalError, r.fatalError);
                assertEquals(p.getTitle(), 3, r.totalCount());
                assertTrue(p.getTitle() + " should pass", r.allPassed());
            }
        } finally {
            engine.shutdown();
        }
    }

    @Test
    public void starterCodeDoesNotPass() {
        CodeExecutionEngine engine = new CodeExecutionEngine();
        try {
            for (ProblemDetail p : ProblemRepository.INSTANCE.getProblems()) {
                ExecutionResult r = engine.execute(p.getInitialCode(), p.getTestCasesJson());
                assertFalse(p.getTitle() + " starter must not pass", r.allPassed());
            }
        } finally {
            engine.shutdown();
        }
    }
}
