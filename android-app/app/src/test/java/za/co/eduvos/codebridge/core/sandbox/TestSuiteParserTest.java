package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestSuiteParserTest {

    @Test
    public void parsesBackendFormat() throws Exception {
        TestSuite s = TestSuiteParser.parse("{\"cases\":[{\"input\":\"[2,7,11,15], 9\",\"expected\":\"[0,1]\"}]}");
        assertEquals(1, s.cases.size());
        assertEquals("[2,7,11,15], 9", s.cases.get(0).input);
        assertEquals("[0,1]", s.cases.get(0).expected);
        assertNull(s.entryPoint);
    }

    @Test
    public void parsesBareArrayAndEntryPoint() throws Exception {
        TestSuite a = TestSuiteParser.parse("[{\"input\":\"hello\",\"expected\":\"olleh\"}]");
        assertEquals(1, a.cases.size());
        TestSuite b = TestSuiteParser.parse("{\"entryPoint\":\"twoSum\",\"cases\":[{\"input\":\"1\",\"expected\":\"1\"}]}");
        assertEquals("twoSum", b.entryPoint);
    }

    @Test
    public void keepsNonStringValuesAsJsonText() throws Exception {
        TestSuite s = TestSuiteParser.parse("{\"cases\":[{\"input\":[1,2],\"expected\":3}]}");
        assertEquals("[1,2]", s.cases.get(0).input);
        assertEquals("3", s.cases.get(0).expected);
    }

    @Test
    public void rejectsBadSuites() {
        String[] bad = {null, "", "   ", "not json", "{}", "{\"cases\":[]}", "[]",
                "{\"cases\":[{\"input\":\"x\"}]}", "{\"cases\":[1]}", "5"};
        for (String json : bad) {
            try {
                TestSuiteParser.parse(json);
                fail("Expected ParseException for: " + json);
            } catch (TestSuiteParser.ParseException expected) {
                // ok
            }
        }
    }
}
