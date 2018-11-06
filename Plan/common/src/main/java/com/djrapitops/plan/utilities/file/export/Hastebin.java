/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.file.export;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * @author Fuzzlemann
 * @since 3.6.4
 */
public class Hastebin {

    /**
     * Constructor used to hide the public constructor
     */
    private Hastebin() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Uploads the content safely to Hastebin.
     * Longer than allowed content is being uploaded too.
     * <p>
     * Splits the content into parts of 390.000 chars each,
     * uploads the parts in reverse order and adds the last link (if present)
     * at each end of the following part, that's why the redundancy of 10.000 chars exists.
     *
     * @return The link to the Dump Log
     * @see #split(String)
     */
    public static String safeUpload(String content) throws ParseException, IOException {
        List<String> parts = ImmutableList.copyOf(split(content)).reverse();

        String lastLink = null;
        try {
            for (String part : parts) {
                if (lastLink != null) {
                    part += "\n" + lastLink;
                }

                lastLink = upload(part);
            }
        } catch (IOException e) {
            if (e.getMessage().contains("503")) {
                return "Hastebin unavailable";
            }
            throw e;
        }

        return lastLink;
    }

    /**
     * Uploads the content to Hastebin using HTTPS and POST
     *
     * @param content The content
     * @return The link to the content
     */
    public static String upload(String content) throws IOException, ParseException {
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

            writeData(connection.getOutputStream(), content);

            return getHastebinLink(connection.getInputStream());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Writes the data to the {@link OutputStream}
     *
     * @param outputStream The OutputStream that the data should be written to
     * @throws IOException when an error at the writing of the data happens
     */
    private static void writeData(OutputStream outputStream, String content) throws IOException {
        try (DataOutputStream wr = new DataOutputStream(outputStream)) {
            wr.writeBytes(content);
        }
    }

    /**
     * Gets the Hastebin Link from the {@link InputStream}
     *
     * @param inputStream The InputStream in which the Hastebin Key is included (encoded in JSON)
     * @return The full Hastebin Link ({@code https://hastebin.com/ + key})
     * @throws IOException    when an error at the reading of the InputStream happens
     * @throws ParseException when an error at the parsing of the line that was read happens
     */
    private static String getHastebinLink(InputStream inputStream) throws IOException, ParseException {
        String key;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader.readLine());

            key = (String) json.get("key");
        }

        return "https://hastebin.com/" + key;
    }

    /**
     * Splits the content in parts of 390.000 chars each
     *
     * @return The content that was splitted
     */
    public static Iterable<String> split(String content) {
        return Splitter.fixedLength(390000).split(content);
    }
}
