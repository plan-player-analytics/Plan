package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;

import java.util.List;

public interface SearchOperations {

    List<String> matchingPlayers(String search) throws DBException;

}
