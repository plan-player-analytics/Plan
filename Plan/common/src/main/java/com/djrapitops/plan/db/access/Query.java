package com.djrapitops.plan.db.access;

import com.djrapitops.plan.db.SQLDB;

/**
 * Interface for everything that returns results from the database.
 *
 * @param <T> Type of the result.
 * @author Rsl1122
 */
public interface Query<T> {

    T executeQuery(SQLDB db);

}
