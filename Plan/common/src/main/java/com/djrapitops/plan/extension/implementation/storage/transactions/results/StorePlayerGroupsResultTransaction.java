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
package com.djrapitops.plan.extension.implementation.storage.transactions.results;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionGroupsTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionProviderTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to store method result of player's groups.
 *
 * @author AuroraLS3
 */
public class StorePlayerGroupsResultTransaction extends ThrowawayTransaction {

    private final String pluginName;
    private final ServerUUID serverUUID;
    private final String providerName;
    private final UUID playerUUID;

    private final String[] value;

    public StorePlayerGroupsResultTransaction(ProviderInformation information, Parameters parameters, String[] value) {
        this.pluginName = information.getPluginName();
        this.providerName = information.getName();
        this.serverUUID = parameters.getServerUUID();
        this.playerUUID = parameters.getPlayerUUID();
        this.value = value;
    }

    @Override
    protected void performOperations() {
        Integer providerId = getProviderId();
        lockValues(providerId);
        deleteOldValues(providerId);
        insertGroups(providerId);
    }

    private void lockValues(Integer providerId) {
        if (dbType == DBType.SQLITE) return;

        query(db -> db.queryOptional(SELECT + ExtensionGroupsTable.ID + FROM + ExtensionGroupsTable.TABLE_NAME +
                        WHERE + ExtensionGroupsTable.PROVIDER_ID + "=?" +
                        AND + ExtensionGroupsTable.USER_UUID + "=?" +
                        lockForUpdate(),
                row -> null, providerId, playerUUID));
    }

    private void deleteOldValues(Integer providerId) {
        String sql = DELETE_FROM + ExtensionGroupsTable.TABLE_NAME +
                WHERE + ExtensionGroupsTable.PROVIDER_ID + "=?" +
                AND + ExtensionGroupsTable.USER_UUID + "=?";
        execute(sql, providerId, playerUUID);
    }

    private void insertGroups(Integer providerId) {
        String sql = INSERT_INTO + ExtensionGroupsTable.TABLE_NAME + "(" +
                ExtensionGroupsTable.GROUP_NAME + "," +
                ExtensionGroupsTable.USER_UUID + "," +
                ExtensionGroupsTable.PROVIDER_ID +
                ") VALUES (?,?,?)";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, providerId);
                for (String group : value) {
                    String groupName = StringUtils.truncate(group, 50);
                    statement.setString(1, groupName);
                    statement.setString(2, playerUUID.toString());
                    statement.addBatch();
                }
            }
        });
    }

    private @NonNull Integer getProviderId() {
        return query(db -> db.queryOptional(ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID,
                row -> row.getInt(1), providerName, pluginName, serverUUID))
                .orElseThrow(() -> new DBOpException("No id found for provider " + pluginName + "." + providerName));
    }
}