package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.settings.config.Config;
import com.djrapitops.plan.system.settings.config.ConfigReader;
import com.djrapitops.plan.system.settings.config.ConfigWriter;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

/**
 * Table that represents plan_settings.
 *
 * @author Rsl1122
 */
public class SettingsTable extends Table {

    public static final String TABLE_NAME = "plan_settings";

    public SettingsTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(TABLE_NAME)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.SERVER_UUID, Sql.varchar(39)).notNull().unique()
                .column(Col.UPDATED, Sql.LONG).notNull()
                .column(Col.CONFIG_CONTENT, "TEXT").notNull()
                .primaryKey(supportsMySQLQueries, Col.ID)
                .toString()
        );
    }

    /**
     * Place a config in the database for this server.
     * <p>
     * Only one config is stored per server uuid.
     *
     * @param serverUUID UUID of the server.
     * @param config     Config of the server.
     */
    public void storeConfig(UUID serverUUID, Config config) {
        TextStringBuilder configTextBuilder = new TextStringBuilder();
        List<String> lines = new ConfigWriter().parseLines(config);
        configTextBuilder.appendWithSeparators(lines, "\n");
        String configSettings = configTextBuilder.toString();
        if (isConfigStored(serverUUID)) {
            updateConfig(serverUUID, configSettings);
        } else {
            insertConfig(serverUUID, configSettings);
        }
    }

    private void insertConfig(UUID serverUUID, String configSettings) {
        String sql = "INSERT INTO " + tableName + " (" +
                Col.SERVER_UUID + ", " +
                Col.UPDATED + ", " +
                Col.CONFIG_CONTENT + ") VALUES (?,?,?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, configSettings);
            }
        });
    }

    private void updateConfig(UUID serverUUID, String configSettings) {
        String sql = "UPDATE " + tableName + " SET " +
                Col.CONFIG_CONTENT + "=?," +
                Col.UPDATED + "=? WHERE " +
                Col.SERVER_UUID + "=? AND " +
                Col.CONFIG_CONTENT + "!=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, configSettings);
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, serverUUID.toString());
                statement.setString(4, configSettings);
            }
        });
    }

    private boolean isConfigStored(UUID serverUUID) {
        String sql = "SELECT " + Col.SERVER_UUID + " FROM " + tableName + " WHERE " + Col.SERVER_UUID + "=? LIMIT 1";
        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getString(Col.SERVER_UUID.get()).equals(serverUUID.toString());
            }
        });
    }

    /**
     * Fetch a config that was placed into the database after a certain epoch ms.
     *
     * @param updatedAfter Epoch ms.
     * @param serverUUID   UUID of the server
     * @return Optional Config if a new config is found, empty if not.
     */
    public Optional<Config> fetchNewerConfig(long updatedAfter, UUID serverUUID) {
        String sql = "SELECT " + Col.CONFIG_CONTENT + " FROM " + tableName +
                " WHERE " + Col.UPDATED + ">? AND " +
                Col.SERVER_UUID + "=? LIMIT 1";

        return Optional.ofNullable(query(new QueryStatement<Config>(sql, 10) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, updatedAfter);
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Config processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return new ConfigReader(new Scanner(set.getString(Col.CONFIG_CONTENT.get()))).read();
                } else {
                    return null;
                }
            }
        }));
    }

    public enum Col implements Column {
        ID("id"),
        SERVER_UUID("server_uuid"),
        UPDATED("updated"),
        CONFIG_CONTENT("content");

        private final String name;

        Col(String name) {
            this.name = name;
        }

        @Override
        public String get() {
            return name;
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
