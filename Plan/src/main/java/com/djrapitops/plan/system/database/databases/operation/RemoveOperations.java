package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.DBNoDataException;

import java.sql.SQLException;
import java.util.UUID;

public interface RemoveOperations {

    void removePlayer(UUID uuid) throws SQLException, DBNoDataException;

    void removePlayer(UUID player, UUID server) throws SQLException, DBNoDataException;

    void removeServer(UUID serverUUID) throws SQLException, DBNoDataException;

    void removeAll() throws SQLException, DBNoDataException;

    void removeWebUser(String name) throws SQLException, DBNoDataException;
}
