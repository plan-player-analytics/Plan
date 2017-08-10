package main.java.com.djrapitops.plan.utilities.file.dump;

import main.java.com.djrapitops.plan.Log;
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

    private List<CharSequence> lines = new ArrayList<>();

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
        lines.add(line.toString());
    }

    /**
     * Uploads the dump log to Hastebin using HTTPS and POST
     *
     * @return The link to the Dump Log
     */
    String upload() {
        String content = this.toString();
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
            wr.writeBytes(this.toString());
            wr.flush();
            wr.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(rd.readLine());

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

    @Override
    public String toString() {
        return String.join("\n", lines);
    }
}
