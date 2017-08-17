package main.java.com.djrapitops.plan.utilities.file.dump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fuzzlemann
 * @since 3.6.2
 */
public class DumpLog {

    private final List<CharSequence> lines = new ArrayList<>();

    /**
     * Writes a header
     *
     * @param header The name of the header
     */
    void addHeader(String header) {
        addLine("");
        addLine("--- " + header + " ---");
    }

    /**
     * Adds a String {@code value} to a String {@code key}
     *
     * @param key   The key
     * @param value The value
     */
    void add(String key, String value) {
        addLine(key + ": " + value);
    }

    /**
     * Adds a boolean {@code value} to a String {@code key}
     *
     * @param key   The key
     * @param value The value
     */
    void add(String key, boolean value) {
        addLine(key + ": " + value);
    }

    /**
     * Adds multiple {@link CharSequence CharSequences} stored in an {@link Iterable}
     * to a String {@code key}
     *
     * @param key   The key
     * @param value The CharSequences stored in an Iterable
     */
    void add(String key, Iterable<? extends CharSequence> value) {
        addLine(key + ": " + String.join(", ", value));
    }

    /**
     * Adds multiple lines
     *
     * @param lines The CharSequences stored in an Iterable
     */
    void addLines(Iterable<? extends CharSequence> lines) {
        lines.forEach(this::addLine);
    }

    /**
     * Adds multiple lines
     *
     * @param lines The lines
     */
    void addLines(CharSequence... lines) {
        Arrays.stream(lines).forEach(this::addLine);
    }

    /**
     * Adds one line
     *
     * @param line The content of the line
     */
    private void addLine(CharSequence line) {
        lines.add(line == null ? "\n" : line.toString());
    }

    /**
     * Uploads the dump log to Hastebin using HTTPS and POST
     *
     * @return The link to the Dump Log
     */
    String upload() {
        return Hastebin.safeUpload(this.toString());
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }
}
