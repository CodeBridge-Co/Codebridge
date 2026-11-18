package za.co.eduvos.codebridge.core.sandbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads test_cases_json. Two shapes work:
 * <pre>
 *   {"cases":[{"input":"[2,7,11,15], 9","expected":"[0,1]"}], "entryPoint":"twoSum"}
 *   [{"input":"hello","expected":"olleh"}]
 * </pre>
 * Optional extras on the object: "compare" (exact, unordered or checker), "checker" (a check(args, actual)
 * function), "timeLimitMinutes" and "interviewQuestions" (up to 10).
 * Optional on a case: "hidden": true.
 * "input" is written like the arguments of a function call. Numbers and arrays are kept as text.
 */
public final class TestSuiteParser {

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    private TestSuiteParser() {}

    public static TestSuite parse(String json) throws ParseException {
        if (json == null || json.trim().isEmpty()) {
            throw new ParseException("Test suite is empty");
        }

        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            throw new ParseException("Test suite is not valid JSON: " + e.getMessage());
        }

        String entryPoint = null;
        TestSuite.Mode mode = TestSuite.Mode.EXACT;
        String checker = null;
        int timeLimitMinutes = 0;
        List<String> questions = new ArrayList<>();
        JsonArray array;
        if (root.isJsonArray()) {
            array = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            JsonElement cases = obj.get("cases");
            if (cases == null || !cases.isJsonArray()) {
                throw new ParseException("Test suite has no \"cases\" array");
            }
            array = cases.getAsJsonArray();
            JsonElement ep = obj.get("entryPoint");
            if (ep != null && ep.isJsonPrimitive()) {
                entryPoint = ep.getAsString();
            }
            JsonElement cmp = obj.get("compare");
            if (cmp != null && cmp.isJsonPrimitive()) {
                String m = cmp.getAsString().trim().toLowerCase();
                if (m.equals("exact")) mode = TestSuite.Mode.EXACT;
                else if (m.equals("unordered")) mode = TestSuite.Mode.UNORDERED;
                else if (m.equals("checker")) mode = TestSuite.Mode.CHECKER;
                else throw new ParseException("Unknown compare mode \"" + cmp.getAsString() + "\"");
            }
            JsonElement tl = obj.get("timeLimitMinutes");
            if (tl != null && tl.isJsonPrimitive() && tl.getAsJsonPrimitive().isNumber()) {
                int minutes = tl.getAsInt();
                if (minutes <= 0) throw new ParseException("timeLimitMinutes must be greater than 0");
                timeLimitMinutes = minutes;
            }
            JsonElement qs = obj.get("interviewQuestions");
            if (qs != null && qs.isJsonArray()) {
                for (JsonElement q : qs.getAsJsonArray()) {
                    if (q.isJsonPrimitive() && !q.getAsString().trim().isEmpty() && questions.size() < 10) {
                        questions.add(q.getAsString().trim());
                    }
                }
            }
            JsonElement chk = obj.get("checker");
            if (chk != null && chk.isJsonPrimitive()) {
                checker = chk.getAsString();
            }
            if (mode == TestSuite.Mode.CHECKER && (checker == null || checker.trim().isEmpty())) {
                throw new ParseException("compare \"checker\" needs a \"checker\" function");
            }
        } else {
            throw new ParseException("Test suite must be an object or array");
        }

        List<TestSuite.Case> cases = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonElement el = array.get(i);
            if (!el.isJsonObject()) {
                throw new ParseException("Case " + (i + 1) + " is not an object");
            }
            JsonObject c = el.getAsJsonObject();
            if (!c.has("input") || !c.has("expected")) {
                throw new ParseException("Case " + (i + 1) + " needs \"input\" and \"expected\"");
            }
            JsonElement hid = c.get("hidden");
            boolean hidden = hid != null && hid.isJsonPrimitive() && hid.getAsJsonPrimitive().isBoolean()
                    && hid.getAsBoolean();
            cases.add(new TestSuite.Case(asText(c.get("input")), asText(c.get("expected")), hidden));
        }

        if (cases.isEmpty()) {
            throw new ParseException("Test suite contains no test cases");
        }
        return new TestSuite(cases, entryPoint, mode, checker, timeLimitMinutes, questions);
    }

    private static String asText(JsonElement e) {
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
            return e.getAsString();
        }
        return e.toString();
    }
}