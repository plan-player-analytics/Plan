/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data;

import org.apache.commons.text.WordUtils;

/**
 * IDs of various actions
 *
 * @author Rsl1122
 */
public enum Actions {
    UNKNOWN(-1),
    FIRST_SESSION(1),
    FIRST_LOGOUT(2),
    NEW_NICKNAME(3),
    KILLED(-2), // Not stored in ActionsTable.
    ;

    private final int id;

    Actions(int id) {
        this.id = id;
    }

    public static Actions getById(int id) {
        for (Actions a : values()) {
            if (a.getId() == id) {
                return a;
            }
        }
        return UNKNOWN;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name(), '_').replace('_', ' ');
    }
}
