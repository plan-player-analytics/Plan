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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Removes incorrectly formatted package data from the database.
 */
public class RemoveIncorrectTebexPackageDataPatch extends Patch {

    private static final String TABLE_NAME = "plan_tebex_payments";

    @Override
    public boolean hasBeenApplied() {
        return !hasTable(TABLE_NAME) || !query(hasWrongRows());
    }

    private Query<Boolean> hasWrongRows() {
        String sql = SELECT + "COUNT(*) as c" + FROM + TABLE_NAME +
                WHERE + "packages LIKE 'TebexPackage%'";
        return db -> db.queryOptional(sql, set -> set.getInt("c") > 0).orElse(false);
    }

    @Override
    protected void applyPatch() {
        execute(DELETE_FROM + TABLE_NAME + WHERE + "packages LIKE 'TebexPackage%'");
    }
}
