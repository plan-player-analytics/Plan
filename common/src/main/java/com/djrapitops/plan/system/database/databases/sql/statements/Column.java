/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.statements;

/**
 * Interface for SQL column enum compatibility.
 *
 * @author Rsl1122
 */
public interface Column {

    default String get() {
        return toString();
    }

    String toString();

}
