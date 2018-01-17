/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities;

/**
 * Utility class for performing actions if something is null.
 *
 * @author Rsl1122
 */
public class NullCheck {

    private NullCheck() {
    }

    public static <T extends Throwable> void check(Object toCheck, T throwIfNull) throws T {
        if (toCheck == null) {
            throw throwIfNull;
        }
    }

}