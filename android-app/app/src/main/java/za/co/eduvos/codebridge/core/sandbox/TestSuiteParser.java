package za.co.eduvos.codebridge.core.sandbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses test_cases_json. Accepted shapes:
 * <pre>
 *   {"cases":[{"input":"[2,7,11,15], 9","expected":"[0,1]"}], "entryPoint":"twoSum"}
 *   [{"input":"hello","expected":"olleh"}]
 * </pre>
 * "input" is the argument list as it would be written between the parentheses of a call.
 * Non-string JSON values are accepted and kept as their JSON text.
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
            cases.add(new TestSuite.Case(asText(c.get("input")), asText(c.get("expected"))));
        }

        if (cases.isEmpty()) {
            throw new ParseException("Test suite contains no test cases");
        }
        return new TestSuite(cases, entryPoint);
    }

    private static String asText(JsonElement e) {
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
            return e.getAsString();
        }
        return e.toString();
    }
}
