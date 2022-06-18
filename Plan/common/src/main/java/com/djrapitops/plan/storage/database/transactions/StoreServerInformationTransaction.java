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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.identification.Server;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.tables.ServerTable.INSERT_STATEMENT;
import static com.djrapitops.plan.storage.database.sql.tables.ServerTable.UPDATE_STATEMENT;

/**
 * Transaction for keeping Plan Server information up to date in the database.
 *
 * @author AuroraLS3
 */
public class StoreServerInformationTransaction extends Transaction {

    private final Server server;

    public StoreServerInformationTransaction(Server server) {
        this.server = server;
    }

    @Override
    protected void performOperations() {
        if (!execute(updateServerInformation())) {
            execute(insertServerInformation());
        }
    }

    private Executable updateServerInformation() {
        return new ExecStatement(UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                String serverUUIDString = server.getUuid().toString();
                statement.setString(1, serverUUIDString);
                statement.setString(2, server.getName());
                statement.setString(3, server.getWebAddress());
                statement.setBoolean(4, true);
                statement.setBoolean(5, server.isProxy());
                statement.setString(6, server.getPlanVersion());
                statement.setString(7, serverUUIDString);
            }
        };
    }

    private Executable insertServerInformation() {
        return new ExecStatement(INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, server.getUuid().toString());
                statement.setString(2, server.getName());
                statement.setString(3, server.getWebAddress());
                statement.setBoolean(4, true);
                statement.setBoolean(5, server.isProxy());
                statement.setString(6, server.getPlanVersion());
            }
        };
    }
}