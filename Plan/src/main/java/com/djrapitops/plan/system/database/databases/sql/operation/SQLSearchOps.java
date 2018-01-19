package com.djrapitops.plan.system.database.databases.sql.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.operation.SearchOperations;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLErrorUtil;

import java.sql.SQLException;
import java.util.List;

public class SQLSearchOps extends SQLOps implements SearchOperations {

    public SQLSearchOps(SQLDB db) {
        super(db);
    }

    @Override
    public List<String> matchingPlayers(String search) throws DBException {
        try {
            return usersTable.getMatchingNames(search);
        } catch (SQLException e) {
            throw SQLErrorUtil.getExceptionFor(e);
        }
    }
}
