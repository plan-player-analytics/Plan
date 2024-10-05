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
package com.djrapitops.plan.storage.database.sql.tables.extension.graph;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTabTable;
import org.apache.commons.text.TextStringBuilder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents plan_extension_graph_metadata table.
 *
 * @author AuroraLS3
 */
public class ExtensionGraphMetadataTable {

    public static final String TABLE_NAME = "plan_extension_graph_metadata";

    public static final String ID = "id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String TAB_ID = "tab_id";
    public static final String SUPPORTS_STACKING = "supports_stacking";
    public static final String Y_AXIS_SOFT_MAX = "y_axis_soft_max";
    public static final String Y_AXIS_SOFT_MIN = "y_axis_soft_min";
    public static final String X_AXIS_SOFT_MAX = "x_axis_soft_max";
    public static final String X_AXIS_SOFT_MIN = "x_axis_soft_min";
    public static final String GRAPH_TABLE_NAME = "graph_table_name";
    public static final String COLUMN_COUNT = "column_count";
    public static final String TABLE_TYPE = "table_type";
    @Language("SQL")
    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            SUPPORTS_STACKING + ',' +
            Y_AXIS_SOFT_MAX + ',' +
            Y_AXIS_SOFT_MIN + ',' +
            X_AXIS_SOFT_MAX + ',' +
            X_AXIS_SOFT_MIN + ',' +
            GRAPH_TABLE_NAME + ',' +
            TABLE_TYPE + ',' +
            TAB_ID + ',' +
            PROVIDER_ID +
            ") VALUES (?, ?, ?, ?, ?, ?, ?," +
            ExtensionTabTable.STATEMENT_SELECT_TAB_ID + ',' +
            ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID +
            ")";
    @Language("SQL")
    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " +
            SUPPORTS_STACKING + "=?," +
            Y_AXIS_SOFT_MAX + "=?," +
            Y_AXIS_SOFT_MIN + "=?," +
            X_AXIS_SOFT_MAX + "=?," +
            X_AXIS_SOFT_MIN + "=?," +
            GRAPH_TABLE_NAME + "=?," +
            TABLE_TYPE + "=?," +
            TAB_ID + "=" + ExtensionTabTable.STATEMENT_SELECT_TAB_ID +
            WHERE +
            PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;
    @Language("SQL")
    public static final String STATEMENT_SELECT_COLUMN_COUNT = SELECT + COLUMN_COUNT +
            FROM + TABLE_NAME +
            WHERE + PROVIDER_ID + '=' + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;
    @Language("SQL")
    public static final String UPDATE_COLUMN_COUNT_STATEMENT = "UPDATE " + TABLE_NAME + " SET " +
            COLUMN_COUNT + "=?" +
            WHERE +
            PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

    private ExtensionGraphMetadataTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(PROVIDER_ID, Sql.INT).notNull()
                .column(TAB_ID, Sql.INT)
                .column(SUPPORTS_STACKING, Sql.BOOL).notNull()
                .column(Y_AXIS_SOFT_MAX, Sql.LONG).notNull()
                .column(Y_AXIS_SOFT_MIN, Sql.LONG).notNull()
                .column(X_AXIS_SOFT_MAX, Sql.LONG).notNull()
                .column(X_AXIS_SOFT_MIN, Sql.LONG).notNull()
                .column(GRAPH_TABLE_NAME, Sql.varchar(116)).notNull() // plan_extension_pluginName_methodName
                .column(COLUMN_COUNT, Sql.INT).defaultValue("1").notNull()
                .column(TABLE_TYPE, INT).notNull()
                .foreignKey(PROVIDER_ID, ExtensionProviderTable.TABLE_NAME, ExtensionProviderTable.ID)
                .foreignKey(TAB_ID, ExtensionTabTable.TABLE_NAME, ExtensionTabTable.ID)
                .build();
    }

    public static String createGraphTableSQL(DBType dbType, String pluginName, String methodName, TableType type) {
        String serverIdColumn = "server_id";
        if (type == TableType.SERVER) {
            return CreateTableBuilder.create(getTableName(pluginName, methodName), dbType)
                    .column(ID, Sql.INT).primaryKey()
                    .column(serverIdColumn, INT).notNull()
                    .column("x", Sql.LONG).notNull()
                    .column("value_1", Sql.DOUBLE)
                    .foreignKey(serverIdColumn, ServerTable.TABLE_NAME, ServerTable.ID)
                    .build();
        } else if (type == TableType.PLAYER) {
            return CreateTableBuilder.create(getTableName(pluginName, methodName), dbType)
                    .column(ID, Sql.INT).primaryKey()
                    .column(serverIdColumn, INT).notNull()
                    .column("x", Sql.LONG).notNull()
                    .column("value_1", Sql.DOUBLE)
                    .column("user_id", INT)
                    .foreignKey("user_id", UsersTable.TABLE_NAME, UsersTable.ID)
                    .foreignKey(serverIdColumn, ServerTable.TABLE_NAME, ServerTable.ID)
                    .build();
        }
        throw new DBOpException("Unsupported table type " + type.name());
    }

    public static @NotNull String getTableName(String pluginName, String methodName) {
        return "plan_extension_" + pluginName + "_" + methodName;
    }

    public static List<String> addColumnsStatements(String pluginName, String methodName, int columnCount, int newColumnCount) {
        return IntStream.range(columnCount, newColumnCount)
                .mapToObj(i -> "ALTER TABLE " + getTableName(pluginName, methodName) + " ADD COLUMN value_" + (i + 1) + " " + DOUBLE)
                .collect(Collectors.toList());
    }

    public static String insertToGraphTableSql(String pluginName, String methodName, int columnCount, TableType type) {
        String valueList = new TextStringBuilder().appendWithSeparators(IntStream.range(0, columnCount)
                        .mapToObj(i -> "value_" + (i + 1))
                        .iterator(), ",")
                .toString();
        return "INSERT INTO " + getTableName(pluginName, methodName) + " (" +
                "server_id," +
                valueList +
                (type == TableType.PLAYER ? ",user_id" : "") +
                ") VALUES (" +
                ServerTable.SELECT_SERVER_ID + ',' +
                Sql.nParameters(columnCount) +
                (type == TableType.PLAYER ? ',' + UsersTable.SELECT_USER_ID : "") +
                ")";
    }

    public enum TableType {
        SERVER(0),
        PLAYER(1),
        GROUP(2);

        private final int type;

        TableType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
