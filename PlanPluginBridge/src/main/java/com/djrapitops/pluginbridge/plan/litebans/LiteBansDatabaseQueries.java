package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
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
 * @since 3.5.0
 */
public class LiteBansDatabaseQueries extends Table {
    private final Database database;

    private final String banTable;
    private final String mutesTable;
    private final String warningsTable;
    private final String kicksTable;

    private final String selectSQL;

    public LiteBansDatabaseQueries(String tablePrefix) {
        super("litebans", null);
        database = Database.get();
        banTable = tablePrefix + "bans";
        mutesTable = tablePrefix + "mutes";
        warningsTable = tablePrefix + "warnings";
        kicksTable = tablePrefix + "kicks";
        selectSQL = "SELECT uuid, reason, banned_by_name, until, active, time FROM ";
    }

    @Override
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

    @Override
    public void createTable() {
        throw new IllegalStateException("Not Supposed to be called.");
    }
}
