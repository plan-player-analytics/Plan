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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionPluginTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionTableProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Removes invalid data caused by <a href="https://github.com/plan-player-analytics/Plan/issues/1355">issue #1355</a>.
 *
 * @author AuroraLS3
 */
public class LitebansTableHeaderPatch extends Patch {

    private Set<Integer> found;

    @Override
    public boolean hasBeenApplied() {
        String sql = SELECT + DISTINCT + "pr." + ExtensionTableProviderTable.ID + " as id" +
                FROM + ExtensionServerTableValueTable.TABLE_NAME + " v" +
                INNER_JOIN + ExtensionTableProviderTable.TABLE_NAME + " pr on pr." + ExtensionTableProviderTable.ID + "=v." + ExtensionServerTableValueTable.TABLE_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " p on p." + ExtensionPluginTable.ID + "=pr." + ExtensionTableProviderTable.PLUGIN_ID +
                WHERE + "p." + ExtensionPluginTable.PLUGIN_NAME + "=?" +
                AND + "(pr." + ExtensionTableProviderTable.PROVIDER_NAME + "=?" +
                OR + "pr." + ExtensionTableProviderTable.PROVIDER_NAME + "=?" +
                OR + "pr." + ExtensionTableProviderTable.PROVIDER_NAME + "=?" +
                OR + "pr." + ExtensionTableProviderTable.PROVIDER_NAME + "=?)";
        found = query(new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "Litebans");
                statement.setString(2, "bans");
                statement.setString(3, "warns");
                statement.setString(4, "kicks");
                statement.setString(5, "mutes");
            }

            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                Set<Integer> ids = new HashSet<>();
                while (set.next()) {
                    ids.add(set.getInt("id"));
                }
                return ids;
            }
        });
        return found.isEmpty();
    }

    @Override
    protected void applyPatch() {
        String sql = DELETE_FROM + ExtensionServerTableValueTable.TABLE_NAME +
                WHERE + ExtensionServerTableValueTable.TABLE_ID + "=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Integer id : found) {
                    statement.setInt(1, id);
                    statement.addBatch();
                }
            }
        });
    }
}
