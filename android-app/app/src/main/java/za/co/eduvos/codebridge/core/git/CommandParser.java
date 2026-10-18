package za.co.eduvos.codebridge.core.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses one line typed in the terminal into a command. Understands git's quoting for
 * messages ("fix bug") and the short/long flags the exercises use; it never executes anything.
 */
public final class CommandParser {

    public enum Verb {
        STATUS, ADD, COMMIT, BRANCH, CHECKOUT, MERGE, LOG, PUSH,
        LS, CAT,
        /** A real git command this simulator doesn't implement (rebase, stash, ...). */
        UNSUPPORTED
    }

    public static final class Command {
        public final Verb verb;
        /** What the user typed after "git" (or the shell word), e.g. "rebase". */
        public final String name;
        public final List<String> args;
        /** Flags without dashes: "a", "b", "abort", "oneline". */
        public final Set<String> flags;
        /** Value of -m / --message, or null. */
        public final String message;

        Command(Verb verb, String name, List<String> args, Set<String> flags, String message) {
            this.verb = verb;
            this.name = name;
            this.args = Collections.unmodifiableList(args);
            this.flags = Collections.unmodifiableSet(flags);
            this.message = message;
        }

        public boolean has(String flag) {
            return flags.contains(flag);
        }
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    private CommandParser() {}

    public static Command parse(String line) throws ParseException {
        List<String> tokens = tokenize(line == null ? "" : line.trim());
        if (tokens.isEmpty()) throw new ParseException("");

        String first = tokens.get(0);
        if (first.equals("ls")) return build(Verb.LS, "ls", tokens.subList(1, tokens.size()));
        if (first.equals("cat")) return build(Verb.CAT, "cat", tokens.subList(1, tokens.size()));
        if (!first.equals("git")) throw new ParseException(first + ": command not found");
        if (tokens.size() == 1) throw new ParseException("usage: git <command> [<args>]");

        String name = tokens.get(1);
        List<String> rest = tokens.subList(2, tokens.size());
        switch (name) {
            case "status": return build(Verb.STATUS, name, rest);
            case "add": return build(Verb.ADD, name, rest);
            case "commit": return build(Verb.COMMIT, name, rest);
            case "branch": return build(Verb.BRANCH, name, rest);
            case "checkout":
            case "switch": return build(Verb.CHECKOUT, name, rest);
            case "merge": return build(Verb.MERGE, name, rest);
            case "log": return build(Verb.LOG, name, rest);
            case "push": return build(Verb.PUSH, name, rest);
            case "pull": case "fetch": case "rebase": case "reset": case "stash": case "diff":
            case "clone": case "init": case "remote": case "tag": case "cherry-pick": case "revert":
                return build(Verb.UNSUPPORTED, name, rest);
            default:
                throw new ParseException("git: '" + name + "' is not a git command. See 'git --help'.");
        }
    }

    private static Command build(Verb verb, String name, List<String> rest) throws ParseException {
        List<String> args = new ArrayList<>();
        Set<String> flags = new LinkedHashSet<>();
        String message = null;

        for (int i = 0; i < rest.size(); i++) {
            String t = rest.get(i);
            if (t.equals("-m") || t.equals("--message")) {
                if (i + 1 >= rest.size()) throw new ParseException("error: switch `m' requires a value");
                message = rest.get(++i);
            } else if (t.equals("-am") || t.equals("-ma")) {
                flags.add("a");
                if (i + 1 >= rest.size()) throw new ParseException("error: switch `m' requires a value");
                message = rest.get(++i);
            } else if (t.startsWith("--message=")) {
                message = t.substring("--message=".length());
            } else if (t.startsWith("--")) {
                flags.add(t.substring(2));
            } else if (t.startsWith("-") && t.length() > 1) {
                for (char c : t.substring(1).toCharArray()) flags.add(String.valueOf(c));
            } else {
                args.add(t);
            }
        }
        return new Command(verb, name, args, flags, message);
    }

    /** Splits on spaces, keeping "double" and 'single' quoted text together. */
    static List<String> tokenize(String line) throws ParseException {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        char quote = 0;
        boolean inToken = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quote != 0) {
                if (c == quote) quote = 0; else cur.append(c);
            } else if (c == '"' || c == '\'') {
                quote = c;
                inToken = true;
            } else if (Character.isWhitespace(c)) {
                if (inToken || cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                    inToken = false;
                }
            } else {
                cur.append(c);
                inToken = true;
            }
        }
        if (quote != 0) throw new ParseException("error: unterminated quote");
        if (inToken || cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
