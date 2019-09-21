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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.*;

/**
 * Patch for removing ip_hash values from plan_ips table.
 * <p>
 * The patch is a response to a concern:
 * "Hashed IP addresses are pseudonymised not anonymised and can be easily decoded using a rainbow table".
 *
 * @author Rsl1122
 */
@Deprecated
public class DeleteIPHashesPatch extends Patch {

    private static final String IP_HASH = "ip_hash";

    private boolean hasNoHashColumn;

    @Override
    public boolean hasBeenApplied() {
        hasNoHashColumn = !hasColumn(GeoInfoTable.TABLE_NAME, IP_HASH);

        String sql = SELECT + "COUNT(1) as c" + FROM + GeoInfoTable.TABLE_NAME +
                WHERE + IP_HASH + IS_NOT_NULL;

        return hasNoHashColumn || !query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {
                /* No variables needed */
            }
        });
    }

    @Override
    protected void applyPatch() {
        if (hasNoHashColumn) {
            return;
        }

        String sql = "UPDATE " + GeoInfoTable.TABLE_NAME + " SET ip_hash=?" + WHERE + IP_HASH + IS_NOT_NULL;
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setNull(1, Types.VARCHAR);
            }
        });
    }

}