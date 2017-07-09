package main.java.com.djrapitops.plan.ui.webserver;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Rsl1122
 */
public class Request {

    private InputStream input;
    private String uri;

    /**
     * Creates a new Request object.
     *
     * @param input InputStream to read the web request from.
     */
    public Request(InputStream input) {
        this.input = input;
    }

    /**
     * Parses the request URI.
     */
    public void parse() {
        // Read a set of characters from the socket
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];

        try {
            i = input.read(buffer);
        } catch (IOException e) {
            i = -1;
        }

        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        uri = parseUri(request.toString());
    }

    private String parseUri(String requestString) {
        int index1, index2;
        index1 = requestString.indexOf(' ');

        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                return requestString.substring(index1 + 1, index2);
            }
        }
        return null;
    }

    /**
     * @return Parsed URI
     */
    public String getUri() {
        return uri;
    }
}
