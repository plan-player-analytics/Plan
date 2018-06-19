/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility for performing Base64 operations.
 *
 * @author Rsl1122
 */
public class Base64Util {

    /**
     * Hides public constructor.
     */
    private Base64Util() {
    }

    public static String encode(String decoded) {
        byte[] encoded = Base64.getEncoder().encode(decoded.getBytes());
        return new String(encoded);
    }

    public static String decode(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded.getBytes());
        return new String(decoded);
    }

    public static List<String> split(String encoded, long partLength) {
        List<String> split = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        long length = 0;
        for (char c : encoded.toCharArray()) {
            builder.append(c);
            length++;
            if (length >= partLength) {
                split.add(builder.toString());
                builder = new StringBuilder();
                length = 0;
            }
        }

        // Add the last part even if it isn't full length.
        if (length != 0) {
            split.add(builder.toString());
        }

        return split;
    }
}
