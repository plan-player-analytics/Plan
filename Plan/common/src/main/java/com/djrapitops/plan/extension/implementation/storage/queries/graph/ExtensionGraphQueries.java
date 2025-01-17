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
package com.djrapitops.plan.extension.implementation.storage.queries.graph;

import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import org.intellij.lang.annotations.Language;

import java.util.Collection;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class ExtensionGraphQueries {

    private ExtensionGraphQueries() {
        /* Static method class */
    }

    public static Query<List<String>> findGraphTableNames() {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME + FROM + ExtensionGraphMetadataTable.TABLE_NAME;
        return db -> db.queryList(sql, row -> row.getString(1));
    }

    public static Query<List<String>> findGraphTableNames(ExtensionGraphMetadataTable.TableType type) {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME +
                WHERE + ExtensionGraphMetadataTable.TABLE_TYPE + "=?";
        return db -> db.queryList(sql, row -> row.getString(1), type.getType());
    }

    public static Query<List<String>> findGraphTableNames(Collection<Integer> providerIds) {
        @Language("SQL")
        String sql = SELECT + ExtensionGraphMetadataTable.GRAPH_TABLE_NAME +
                FROM + ExtensionGraphMetadataTable.TABLE_NAME +
                WHERE + ExtensionGraphMetadataTable.PROVIDER_ID + " IN (" + Sql.nParameters(providerIds.size()) + ")";
        return db -> db.queryList(sql, row -> row.getString(1), providerIds);
    }
}
