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
package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import litebans.api.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class responsible for making queries to LiteBans database.
 *
 * @author Rsl1122
 */
public class LiteBansDatabaseQueries {
    private final Database database;

    private final String banTable;
    private final String mutesTable;
    private final String warningsTable;
    private final String kicksTable;

    private final String selectSQL;

    public LiteBansDatabaseQueries() {
        database = Database.get();
        banTable = "{bans}";
        mutesTable = "{mutes}";
        warningsTable = "{warnings}";
        kicksTable = "{kicks}";
        selectSQL = "SELECT uuid, reason, banned_by_name, until, active, time FROM ";
    }

    protected <T> T query(QueryStatement<T> statement) {
        try (PreparedStatement preparedStatement = database.prepareStatement(statement.getSql())) {
            return statement.executeQuery(preparedStatement);
        } catch (SQLException e) {
            throw DBOpException.forCause(statement.getSql(), e);
        }
    }

    private List<LiteBansDBObj> getObjs(String table) {
        String sql = selectSQL + table + " LIMIT 5000";

        return query(new QueryAllStatement<List<LiteBansDBObj>>(sql, 2000) {
            @Override
            public List<LiteBansDBObj> processResults(ResultSet resultSet) throws SQLException {
                return processIntoObjects(resultSet);
            }
        });
    }

    public List<LiteBansDBObj> getBans() {
        return getObjs(banTable);
    }

    public List<LiteBansDBObj> getMutes() {
        return getObjs(mutesTable);
    }

    public List<LiteBansDBObj> getWarnings() {
        return getObjs(warningsTable);
    }

    public List<LiteBansDBObj> getKicks() {
        return getObjs(kicksTable);
    }

    private List<LiteBansDBObj> processIntoObjects(ResultSet set) throws SQLException {
        List<LiteBansDBObj> objs = new ArrayList<>();
        while (set.next()) {
            String uuidS = set.getString("uuid");
            if (uuidS == null) {
                continue;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidS);
            } catch (IllegalArgumentException e) {
                continue;
            }
            String reason = set.getString("reason");
            String bannedBy = set.getString("banned_by_name");
            long until = set.getLong("until");
            long time = set.getLong("time");
            boolean active = set.getBoolean("active");
            objs.add(new LiteBansDBObj(uuid, reason, bannedBy, until, active, time));
        }
        return objs;
    }

    public List<LiteBansDBObj> getBans(UUID playerUUID) {
        return getObjs(playerUUID, banTable);
    }

    public List<LiteBansDBObj> getMutes(UUID playerUUID) {
        return getObjs(playerUUID, mutesTable);
    }

    public List<LiteBansDBObj> getWarnings(UUID playerUUID) {
        return getObjs(playerUUID, warningsTable);
    }

    public List<LiteBansDBObj> getKicks(UUID playerUUID) {
        return getObjs(playerUUID, kicksTable);
    }

    private List<LiteBansDBObj> getObjs(UUID playerUUID, String table) {
        String sql = selectSQL + table + " WHERE uuid=?";

        return query(new QueryStatement<List<LiteBansDBObj>>(sql, 2000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<LiteBansDBObj> processResults(ResultSet resultSet) throws SQLException {
                return processIntoObjects(resultSet);
            }
        });
    }
}
