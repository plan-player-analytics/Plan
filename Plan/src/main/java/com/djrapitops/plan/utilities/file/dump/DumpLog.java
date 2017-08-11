package main.java.com.djrapitops.plan.utilities.file.dump;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import main.java.com.djrapitops.plan.Log;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fuzzlemann
 * @since 3.7.0
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
        if (line == null) {
            lines.add("\n");
            return;
        }

        lines.add(line.toString());
    }

    /**
     * Uploads the dump log to Hastebin using HTTPS and POST
     *
     * @return The link to the Dump Log
     */
    String upload() {
        List<String> parts = ImmutableList.copyOf(split()).reverse();

        Bukkit.broadcast(parts.size() + "", "asdasd");

        String lastLink = null;
        for (String part : parts) {
            if (lastLink != null) {
                part += "\n" + lastLink;
                Bukkit.broadcast("link not null " + lastLink, "asdasd");
            }

            lastLink = upload(part);
            Bukkit.broadcast(lastLink, "asdasd");
        }

        return lastLink;
    }

    /**
     * Uploads the content to Hastebin using HTTPS and POST
     *
     * @param content The content
     * @return The link to the content
     */
    private String upload(String content) {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL("https://hastebin.com/documents");
            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestProperty("Content-length", String.valueOf(content.length()));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(content);
            wr.flush();
            wr.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String response = reader.readLine();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response);

            return "https://hastebin.com/" + json.get("key");
        } catch (IOException | ParseException e) {
            Log.toLog("DumpLog.upload", e);
            return "Error";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Splits the content of the DumpLog into parts
     *
     * @return The splitted content
     */
    private Iterable<String> split() {
        return Splitter.fixedLength(390000).split(this.toString());
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }
}
