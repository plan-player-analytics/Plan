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
package com.djrapitops.plan.storage.database.transactions.webuser;

import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupToPermissionTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.DELETE_FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Adds or updates a web permission group with specific permissions.
 *
 * @author AuroraLS3
 */
public class StoreWebGroupTransaction extends Transaction {

    private final String name;
    private final Collection<String> permissions;

    public StoreWebGroupTransaction(String name, Collection<String> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    @Override
    protected void performOperations() {
        executeOther(new StoreMissingWebPermissionsTransaction(permissions));
        commitMidTransaction();

        Optional<Integer> id = query(WebUserQueries.fetchGroupId(name));
        if (id.isPresent()) {
            updateGroup(id.get());
        } else {
            insertGroup();
        }
    }

    private void insertGroup() {
        int id = executeReturningId(new ExecStatement(WebGroupTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, name);
            }
        });
        updateGroup(id);
    }

    private void deletePermissionsOfGroup(int id) {
        String sql = DELETE_FROM + WebGroupToPermissionTable.TABLE_NAME + WHERE + WebGroupToPermissionTable.GROUP_ID + "=?";
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, id);
            }
        });
    }

    private void insertPermissionIdsOfGroup(int id, List<Integer> permissionIds) {
        if (permissionIds.isEmpty()) return;

        execute(new ExecBatchStatement(WebGroupToPermissionTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Integer permissionId : permissionIds) {
                    statement.setInt(1, id);
                    statement.setInt(2, permissionId);
                    statement.addBatch();
                }
            }
        });
    }

    private void updateGroup(Integer id) {
        List<Integer> permissionIds = query(WebUserQueries.fetchPermissionIds(permissions));
        deletePermissionsOfGroup(id);
        insertPermissionIdsOfGroup(id, permissionIds);
    }
}
