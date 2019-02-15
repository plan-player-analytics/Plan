package com.djrapitops.plan.db.access.transactions;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.system.info.server.Server;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.db.sql.tables.ServerTable.INSERT_STATEMENT;
import static com.djrapitops.plan.db.sql.tables.ServerTable.UPDATE_STATEMENT;

/**
 * Transaction for keeping Plan Server serverrmation up to date in the database.
 *
 * @author Rsl1122
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
                statement.setInt(5, server.getMaxPlayers());
                statement.setString(6, serverUUIDString);
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
                statement.setInt(5, server.getMaxPlayers());
            }
        };
    }
}