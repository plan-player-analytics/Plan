package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.system.database.databases.operation.SearchOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;

import java.util.Collections;
import java.util.List;

public class SQLSearchOps extends SQLOps implements SearchOperations {

    public SQLSearchOps(SQLDB db) {
        super(db);
    }

    @Override
    public List<String> matchingPlayers(String search) {
        List<String> matchingNames = usersTable.getMatchingNames(search);
        Collections.sort(matchingNames);
        return matchingNames;
    }
}
