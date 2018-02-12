/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities;

import java.util.Base64;

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

}