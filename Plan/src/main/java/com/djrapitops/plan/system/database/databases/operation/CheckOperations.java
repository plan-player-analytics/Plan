package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.UUID;

public interface CheckOperations {

    boolean isPlayerRegistered(UUID player) throws DBException;

    boolean isPlayerRegistered(UUID player, UUID server) throws DBException;

    boolean doesWebUserExists(String username) throws DBException;

}
