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
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionInformationQueries;
import com.djrapitops.plan.extension.statistic.Flags;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagPlayerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagServerValueTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionFlagTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.TableType;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class StoreFlagsResultTransaction extends Transaction {

    private final ProviderInformation information;
    private final Parameters parameters;
    private final Flags value;
    private final TableType tableType;

    public StoreFlagsResultTransaction(ProviderInformation information, Parameters parameters, Flags value, TableType tableType) {
        this.information = information;
        this.parameters = parameters;
        this.value = value;
        this.tableType = tableType;
    }

    @Override
    protected void performOperations() {
        var serverId = query(ServerQueries.fetchServerId(parameters.getServerUUID()))
                .orElseThrow(() -> new DBOpException("Server not stored."));
        var providerId = query(ExtensionInformationQueries.findProviderId(
                        information.getPluginName(), information.getName(), parameters.getServerUUID()))
                .orElseThrow(() -> new DBOpException("Provider not stored."));
        execute(UPDATE + ExtensionFlagTable.TABLE_NAME + SET + ExtensionFlagTable.PARAMETER_TYPE + "=?" +
                WHERE + ExtensionFlagTable.PROVIDER_ID + "=?", tableType.getType(), providerId);
        Map<String, Integer> flagNameToId = storeMissingFlags(providerId, value.getValueByKey().keySet(), tableType);
        Map<Integer, Boolean> valuesById = getValuesById(flagNameToId);

        if (parameters.getPlayerUUID() == null) {
            Set<Integer> existingFlagIds = getStoredServerFlagIds(serverId, providerId);
            List<Integer> removedFlagIds = updateServerValues(serverId, existingFlagIds, valuesById);
            if (!removedFlagIds.isEmpty()) {
                removeServerValues(serverId, removedFlagIds);
            }
            insertServerValues(serverId, existingFlagIds, valuesById);
        } else {
            var userId = query(UserIdentifierQueries.fetchUserId(parameters.getPlayerUUID()))
                    .orElseThrow(() -> new DBOpException("Player not stored."));
            Set<Integer> existingFlagIds = getStoredPlayerFlagIds(userId, serverId, providerId);
            List<Integer> removedFlagIds = updatePlayerValues(userId, serverId, existingFlagIds, valuesById);
            if (!removedFlagIds.isEmpty()) {
                removePlayerValues(userId, serverId, removedFlagIds);
            }
            insertPlayerValues(userId, serverId, existingFlagIds, valuesById);
        }
    }

    private void insertServerValues(Integer serverId, Set<Integer> existingFlagIds, Map<Integer, Boolean> valuesById) {
        execute(new ExecBatchStatement(ExtensionFlagServerValueTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, serverId);
                for (var entry : valuesById.entrySet()) {
                    if (existingFlagIds.contains(entry.getKey())) continue;
                    statement.setInt(1, entry.getKey());
                    statement.setBoolean(3, entry.getValue());
                    statement.addBatch();
                }
            }
        });
    }

    private void insertPlayerValues(Integer userId, Integer serverId, Set<Integer> existingFlagIds, Map<Integer, Boolean> valuesById) {
        execute(new ExecBatchStatement(ExtensionFlagPlayerValueTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, userId);
                statement.setInt(3, serverId);
                for (var entry : valuesById.entrySet()) {
                    if (existingFlagIds.contains(entry.getKey())) continue;
                    statement.setInt(1, entry.getKey());
                    statement.setBoolean(4, entry.getValue());
                    statement.addBatch();
                }
            }
        });
    }

    private List<Integer> updateServerValues(Integer serverId, Set<Integer> existingFlagIds, Map<Integer, Boolean> valuesById) {
        List<Integer> removedIds = new ArrayList<>();
        String updateValue = UPDATE + ExtensionFlagServerValueTable.TABLE_NAME +
                SET + ExtensionFlagServerValueTable.VALUE + "=?" +
                WHERE + ExtensionFlagServerValueTable.FLAG_ID + "=?" +
                AND + ExtensionFlagServerValueTable.SERVER_ID + "=?";
        execute(new ExecBatchStatement(updateValue) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, serverId);
                for (Integer id : existingFlagIds) {
                    Boolean flagValue = valuesById.get(id);
                    if (flagValue == null) {
                        removedIds.add(id);
                        continue;
                    }
                    statement.setBoolean(1, flagValue);
                    statement.setInt(2, id);
                    statement.addBatch();
                }
            }
        });
        return removedIds;
    }

    private List<Integer> updatePlayerValues(Integer userId, Integer serverId, Set<Integer> existingFlagIds, Map<Integer, Boolean> valuesById) {
        List<Integer> removedIds = new ArrayList<>();
        String updateValue = UPDATE + ExtensionFlagPlayerValueTable.TABLE_NAME +
                SET + ExtensionFlagPlayerValueTable.VALUE + "=?" +
                WHERE + ExtensionFlagPlayerValueTable.FLAG_ID + "=?" +
                AND + ExtensionFlagPlayerValueTable.USER_ID + "=?" +
                AND + ExtensionFlagPlayerValueTable.SERVER_ID + "=?";
        execute(new ExecBatchStatement(updateValue) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(3, userId);
                statement.setInt(4, serverId);
                for (Integer id : existingFlagIds) {
                    Boolean flagValue = valuesById.get(id);
                    if (flagValue == null) {
                        removedIds.add(id);
                        continue;
                    }
                    statement.setBoolean(1, flagValue);
                    statement.setInt(2, id);
                    statement.addBatch();
                }
            }
        });
        return removedIds;
    }

    private void removeServerValues(Integer serverId, List<Integer> removedIds) {
        String deleteValue = DELETE_FROM + ExtensionFlagServerValueTable.TABLE_NAME +
                WHERE + ExtensionFlagServerValueTable.FLAG_ID + "=?" +
                AND + ExtensionFlagServerValueTable.SERVER_ID + "=?";
        execute(new ExecBatchStatement(deleteValue) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, serverId);
                for (Integer id : removedIds) {
                    statement.setInt(1, id);
                    statement.addBatch();
                }
            }
        });
    }

    private void removePlayerValues(Integer userId, Integer serverId, List<Integer> removedIds) {
        String deleteValue = DELETE_FROM + ExtensionFlagPlayerValueTable.TABLE_NAME +
                WHERE + ExtensionFlagPlayerValueTable.FLAG_ID + "=?" +
                AND + ExtensionFlagPlayerValueTable.USER_ID + "=?" +
                AND + ExtensionFlagPlayerValueTable.SERVER_ID + "=?";
        execute(new ExecBatchStatement(deleteValue) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, userId);
                statement.setInt(3, serverId);
                for (Integer id : removedIds) {
                    statement.setInt(1, id);
                    statement.addBatch();
                }
            }
        });
    }

    private Set<Integer> getStoredServerFlagIds(Integer serverId, Integer providerId) {
        String selectExistingFlagsForUpdate = SELECT + ExtensionFlagServerValueTable.FLAG_ID +
                FROM + ExtensionFlagServerValueTable.TABLE_NAME +
                WHERE + ExtensionFlagServerValueTable.SERVER_ID + "=?" +
                " AND " + ExtensionFlagServerValueTable.FLAG_ID + " IN (" +
                SELECT + ExtensionFlagTable.ID + FROM + ExtensionFlagTable.TABLE_NAME +
                WHERE + ExtensionFlagTable.PROVIDER_ID + "=?)" +
                lockForUpdate();

        return query(db -> db.querySet(selectExistingFlagsForUpdate, row -> row.getInt(1), serverId, providerId));
    }

    private Set<Integer> getStoredPlayerFlagIds(Integer userId, Integer serverId, Integer providerId) {
        String selectExistingFlagsForUpdate = SELECT + ExtensionFlagPlayerValueTable.FLAG_ID +
                FROM + ExtensionFlagPlayerValueTable.TABLE_NAME +
                WHERE + ExtensionFlagPlayerValueTable.USER_ID + "=?" +
                AND + ExtensionFlagPlayerValueTable.SERVER_ID + "=?" +
                " AND " + ExtensionFlagPlayerValueTable.FLAG_ID + " IN (" +
                SELECT + ExtensionFlagTable.ID + FROM + ExtensionFlagTable.TABLE_NAME +
                WHERE + ExtensionFlagTable.PROVIDER_ID + "=?)" +
                lockForUpdate();

        return query(db -> db.querySet(selectExistingFlagsForUpdate, row -> row.getInt(1), userId, serverId, providerId));
    }

    private Map<Integer, Boolean> getValuesById(Map<String, Integer> flagNameToId) {
        Map<Integer, Boolean> valuesById = new HashMap<>();
        for (var entry : value.getValueByKey().entrySet()) {
            if (entry.getValue() == null) continue;
            valuesById.put(flagNameToId.get(entry.getKey()), entry.getValue());
        }
        return valuesById;
    }

    private Map<String, Integer> storeMissingFlags(Integer providerId, Set<String> flagNames, TableType tableType) {
        String selectFlags = ExtensionFlagTable.SELECT_FLAGS + WHERE + ExtensionFlagTable.PROVIDER_ID + "=?";
        Map<String, Integer> flagNameToId = query(db -> db.queryMap(
                selectFlags,
                (row, map) -> map.put(row.getString(ExtensionFlagTable.FLAG_NAME), row.getInt(ExtensionFlagTable.ID)),
                providerId
        ));

        Set<String> missingFlags = new HashSet<>(flagNames);
        missingFlags.removeAll(flagNameToId.keySet());

        if (!missingFlags.isEmpty()) {
            execute(new ExecBatchStatement(ExtensionFlagTable.Row.INSERT_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setInt(1, providerId);
                    for (String flagName : missingFlags) {
                        statement.setString(2, flagName);
                        statement.setInt(3, tableType.getType());
                        statement.addBatch();
                    }
                }
            });
            commitMidTransaction();

            flagNameToId = query(db -> db.queryMap(
                    selectFlags,
                    (row, map) -> map.put(row.getString(ExtensionFlagTable.FLAG_NAME), row.getInt(ExtensionFlagTable.ID)),
                    providerId
            ));
        }
        return flagNameToId;
    }
}