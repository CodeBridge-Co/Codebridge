package za.co.eduvos.codebridge.core.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import za.co.eduvos.codebridge.core.git.CommandParser.Command;
import za.co.eduvos.codebridge.core.git.CommandParser.Verb;

public class CommandParserTest {

    private static Command parse(String s) throws Exception {
        return CommandParser.parse(s);
    }

    @Test
    public void parsesCommitWithQuotedMessage() throws Exception {
        Command c = parse("git commit -m \"fix the merge conflict\"");
        assertEquals(Verb.COMMIT, c.verb);
        assertEquals("fix the merge conflict", c.message);
    }

    @Test
    public void parsesSingleQuotesAndCombinedAmFlag() throws Exception {
        Command c = parse("git commit -am 'quick save'");
        assertTrue(c.has("a"));
        assertEquals("quick save", c.message);
    }

    @Test
    public void parsesLongMessageForms() throws Exception {
        assertEquals("x y", parse("git commit --message \"x y\"").message);
        assertEquals("abc", parse("git commit --message=abc").message);
    }

    @Test
    public void parsesAddCheckoutMergeAndFlags() throws Exception {
        assertEquals(Verb.ADD, parse("git add .").verb);
        assertEquals(".", parse("git add .").args.get(0));
        Command co = parse("git checkout -b feature/x");
        assertEquals(Verb.CHECKOUT, co.verb);
        assertTrue(co.has("b"));
        assertEquals("feature/x", co.args.get(0));
        assertEquals(Verb.CHECKOUT, parse("git switch main").verb);
        assertTrue(parse("git merge --abort").has("abort"));
        assertTrue(parse("git log --oneline").has("oneline"));
    }

    @Test
    public void extraWhitespaceIsIgnored() throws Exception {
        Command c = parse("   git    status   ");
        assertEquals(Verb.STATUS, c.verb);
        assertNull(c.message);
        assertTrue(c.args.isEmpty());
    }

    @Test
    public void shellHelpers() throws Exception {
        assertEquals(Verb.LS, parse("ls").verb);
        assertEquals("a.txt", parse("cat a.txt").args.get(0));
    }

    @Test
    public void knownButUnimplementedCommandsAreFlagged() throws Exception {
        for (String name : new String[]{"rebase", "stash", "pull", "reset", "fetch"}) {
            assertEquals(Verb.UNSUPPORTED, parse("git " + name).verb);
        }
    }

    @Test
    public void rejectsBadInput() {
        String[] bad = {"rm -rf /", "git", "git frobnicate", "git commit -m", "git commit -m \"unterminated"};
        for (String line : bad) {
            try {
                CommandParser.parse(line);
                fail("Expected ParseException for: " + line);
            } catch (CommandParser.ParseException expected) {
                assertFalse(expected.getMessage().isEmpty());
            }
        }
    }

    @Test
    public void emptyLineIsAnEmptyParseException() {
        try {
            CommandParser.parse("   ");
            fail();
        } catch (CommandParser.ParseException e) {
            assertEquals("", e.getMessage());
        }
    }
}
