package main.java.com.djrapitops.plan.ui.webserver;

import main.java.com.djrapitops.plan.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a HTTP Request.
 *
 * Request is read from the given InputStream.
 *
 * Closing the Request closes the InputStream. (Closing Socket InputStream
 * closes the socket.)
 *
 * Request Strings should not be logged because they may contain base64 encoded
 * user:password Authorization combinations.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class Request implements Closeable {

    private final InputStream input;
    private String request;
    private String target;
    private String authorization;

    private Closeable close;

    /**
     * Creates a new Request object.
     *
     * @param input InputStream to read the socket.
     */
    public Request(InputStream input) {
        this.input = input;
    }

    /**
     * Reads the information in the Request and parses required information.
     *
     * Parses Request (GET, POST etc.)
     *
     * Parses Target (/home/etc)
     *
     * Parses Authorization (Authorization header).
     *
     * @throws java.io.IOException if InputStream can not be read.
     */
    public void parse() throws IOException {
        StringBuilder headerB = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        close = in;
        String line;
        while ((line = in.readLine()) != null) {
            headerB.append(line);
            headerB.append(":::");
        }
        final String requestHeader = headerB.toString();
        String[] header = requestHeader.split(":::");
        parseRequestAndTarget(header);
        parseAuthorization(header);
    }

    /**
     * Check if the request has Authorization: Basic header.
     *
     * @return true/false
     */
    public boolean hasAuthorization() {
        return authorization != null;
    }

    /**
     * Returns the base64 encoded Authorization if present.
     *
     * @return base64 encoded user:password or null.
     */
    public String getAuthorization() {
        return authorization;
    }

    private void parseAuthorization(String[] header) {
        Optional<String> auth = Arrays.stream(header)
                .filter(l -> l.contains("Authorization: Basic "))
                .findFirst();
        if (auth.isPresent()) {
            Log.debug("Found Authorization.");
            authorization = auth.get().replace("Authorization: Basic ", "");
        } else {
            Log.debug("Not Authorized.");
        }
    }

    private void parseRequestAndTarget(String[] header) {
        String req = header[0];
        String[] reqLine = req.split(" ");
        if (reqLine.length >= 2) {
            request = reqLine[0];
            target = reqLine[1];
        } else {
            request = "GET";
            target = "/";
        }
    }

    /**
     * Used to get the request type.
     *
     * @return GET, POST, etc.
     */
    public String getRequest() {
        return request;
    }

    /**
     * Used to get the target.
     *
     * @return for example '/home/etc'
     */
    public String getTarget() {
        return target;
    }

    /**
     * Closes the Request.
     *
     * Closes the inputstream.
     *
     * @throws IOException if the stream can not be closed.
     */
    @Override
    public void close() throws IOException {
        close.close();
    }
}
