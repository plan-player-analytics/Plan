package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.UUID;

public interface RemoveOperations {

    void player(UUID uuid) throws DBException;

    void player(UUID player, UUID server) throws DBException;

    void server(UUID serverUUID) throws DBException;

    void everything() throws DBException;

    void webUser(String name) throws DBException;
}
