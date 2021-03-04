/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility for performing Base64 operations.
 *
 * @author AuroraLS3
 */
public class Base64Util {

    /**
     * Hides public constructor.
     */
    private Base64Util() {
    }

    public static String encodeBytes(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static String encode(String decoded) {
        byte[] encoded = Base64.getEncoder().encode(decoded.getBytes());
        return new String(encoded);
    }

    public static byte[] decodeBytes(String encoded) {
        return Base64.getDecoder().decode(encoded.getBytes());
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
