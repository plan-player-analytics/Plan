package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.DBType;
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
        String updateOnDuplicateKeyStatement = "INSERT INTO " + tableName + " (" +
                Col.SERVER_UUID + ", " +
                Col.UPDATED + ", " +
                Col.CONFIG_CONTENT +
                ") VALUES (?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE" +
                " " + Col.UPDATED + "=?," +
                " " + Col.CONFIG_CONTENT + "=?";
        String replaceIntoStatement = "REPLACE INTO " + tableName + " (" +
                Col.SERVER_UUID + ", " +
                Col.UPDATED + ", " +
                Col.CONFIG_CONTENT +
                ") VALUES (?, ?, ?)";

        String sql = db.getType() == DBType.H2 ? updateOnDuplicateKeyStatement : replaceIntoStatement;

        TextStringBuilder configTextBuilder = new TextStringBuilder();
        List<String> lines = new ConfigWriter().parseLines(config);
        configTextBuilder.appendWithSeparators(lines, "\n");
        String configSettings = configTextBuilder.toString();

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, configSettings);

                if (db.getType() == DBType.H2) {
                    statement.setLong(4, System.currentTimeMillis());
                    statement.setString(5, configSettings);
                }
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
