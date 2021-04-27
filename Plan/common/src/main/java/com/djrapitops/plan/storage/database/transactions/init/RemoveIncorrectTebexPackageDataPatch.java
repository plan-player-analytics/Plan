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

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Removes incorrectly formatted package data from the database.
 */
public class RemoveIncorrectTebexPackageDataPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasTable("plan_tebex_payments") && !query(hasWrongRows());
    }

    private Query<Boolean> hasWrongRows() {
        return new HasMoreThanZeroQueryStatement(
                SELECT + "COUNT(*) as c" + FROM + "plan_tebex_payments" +
                        WHERE + "packages LIKE 'TebexPackage%'"
        ) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
            }
        };
    }

    @Override
    protected void applyPatch() {
        execute(DELETE_FROM + "plan_tebex_payments" + WHERE + "packages LIKE 'TebexPackage%'");
    }
}
