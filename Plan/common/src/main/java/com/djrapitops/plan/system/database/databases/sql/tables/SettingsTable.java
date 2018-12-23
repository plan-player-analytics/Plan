package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;

public class SettingsTable extends Table {

    public static final String TABLE_NAME = "plan_settings";

    public SettingsTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(TABLE_NAME)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.SERVER_UUID, Sql.varchar(39)).notNull()
                .column(Col.UPDATED, Sql.LONG).notNull()
                .column(Col.CONFIG_CONTENT, "TEXT").notNull()
                .primaryKey(supportsMySQLQueries, Col.ID)
                .toString()
        );
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
