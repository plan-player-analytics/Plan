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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to store world name after an event.
 *
 * @author AuroraLS3
 */
public class StoreWorldNameTransaction extends Transaction {

    private final ServerUUID serverUUID;
    private final String worldName;

    public StoreWorldNameTransaction(ServerUUID serverUUID, String worldName) {
        this.serverUUID = serverUUID;
        this.worldName = worldName;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return doesWorldNameNotExist();
    }

    private boolean doesWorldNameNotExist() {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + WorldTable.TABLE_NAME +
                WHERE + WorldTable.NAME + "=?" +
                AND + WorldTable.SERVER_UUID + "=?" + lockForUpdate();
        return !query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, StringUtils.truncate(worldName, 100));
                statement.setString(2, serverUUID.toString());
            }
        });
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.insertWorldName(serverUUID, worldName));
    }
}