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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.db.access.ExecBatchStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.sql.tables.CommandUseTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Static method class for large storage queries.
 *
 * @author Rsl1122
 */
public class LargeStoreQueries {

    private LargeStoreQueries() {
        /* Static method class */
    }

    public static Executable storeAllCommandUsageData(Map<UUID, Map<String, Integer>> allCommandUsages) {
        if (allCommandUsages.isEmpty()) {
            return Executable.empty();
        }

        return new ExecBatchStatement(CommandUseTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (UUID serverUUID : allCommandUsages.keySet()) {
                    // Every Command
                    for (Map.Entry<String, Integer> entry : allCommandUsages.get(serverUUID).entrySet()) {
                        String command = entry.getKey();
                        int timesUsed = entry.getValue();

                        statement.setString(1, command);
                        statement.setInt(2, timesUsed);
                        statement.setString(3, serverUUID.toString());
                        statement.addBatch();
                    }
                }
            }
        };
    }
}