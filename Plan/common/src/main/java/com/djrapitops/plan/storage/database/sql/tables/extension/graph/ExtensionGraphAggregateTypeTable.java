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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents extension_graph_aggregate_type table.
 *
 * @author AuroraLS3
 */
public class ExtensionGraphAggregateTypeTable {

    public static final String TABLE_NAME = "extension_graph_aggregate_type";

    public static final String ID = "id";
    public static final String AGGREGATE_TYPE = "aggregate_type";

    public static final String SELECT_ID_STATEMENT = SELECT + ID + FROM + TABLE_NAME + WHERE + AGGREGATE_TYPE + "=?";
    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + AGGREGATE_TYPE + ") VALUES (?)";

    private ExtensionGraphAggregateTypeTable() {
        /* Static sql utility class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(AGGREGATE_TYPE, Sql.varchar(25)).notNull()
                .build();
    }

    public static String selectInSQL(int n) {
        // Nothing to select when n == 0
        return SELECT + '*' + FROM + TABLE_NAME + WHERE + AGGREGATE_TYPE + (n > 0 ? " IN (" + Sql.nParameters(n) + ")" : IS_NULL);
    }

    /**
     * Represents extension_graph_aggregate_type_to_graph table that joins extension_graph_aggregate_type to a specific graph.
     */
    public static class ToProviderTable {
        public static final String TABLE_NAME = "extension_graph_aggregate_type_to_graph";

        public static final String ID = "id";
        public static final String AGGREGATE_TYPE_ID = "aggregate_type_id";
        public static final String PROVIDER_ID = "provider_id";

        public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + AGGREGATE_TYPE_ID + ',' + PROVIDER_ID +
                ") VALUES (" + SELECT_ID_STATEMENT + ", " + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID + ")";
        public static final String DELETE_STATEMENT = "DELETE" + FROM + TABLE_NAME +
                WHERE + PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;

        private ToProviderTable() {
            /* Static sql utility class */
        }

        public static String createTableSQL(DBType dbType) {
            return CreateTableBuilder.create(TABLE_NAME, dbType)
                    .column(ID, Sql.INT).primaryKey()
                    .column(AGGREGATE_TYPE_ID, Sql.INT)
                    .column(PROVIDER_ID, Sql.INT)
                    .foreignKey(AGGREGATE_TYPE_ID, ExtensionGraphAggregateTypeTable.TABLE_NAME, ExtensionGraphAggregateTypeTable.ID)
                    .foreignKey(PROVIDER_ID, ExtensionProviderTable.TABLE_NAME, ExtensionProviderTable.ID)
                    .build();
        }
    }
}
