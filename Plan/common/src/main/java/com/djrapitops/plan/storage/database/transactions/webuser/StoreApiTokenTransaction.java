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

import com.djrapitops.plan.delivery.domain.auth.ApiToken;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.webuser.ApiTokenTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.ApiTokenToPermissionTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.jspecify.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Adds a new token or updates existing token identifier and permissions.
 *
 * @author AuroraLS3
 */
public class StoreApiTokenTransaction extends Transaction {

    private final ApiToken token;
    private final List<String> permissions;

    public StoreApiTokenTransaction(@Untrusted ApiToken token, List<String> permissions) {
        this.token = token;
        this.permissions = permissions;
    }

    @Override
    protected void performOperations() {
        updatePermissions(upsertToken());
    }

    private @NonNull Integer upsertToken() {
        var foundId = query(db -> db.queryOptional(ApiTokenTable.SELECT_TOKEN_ID + lockForUpdate(),
                row -> row.getInt(ApiTokenTable.ID), token.token()));

        return foundId.map(id -> {
            // Lock existing permission rows
            if (dbType == DBType.MYSQL) {
                query(db -> db.queryOptional(ApiTokenToPermissionTable.SELECT_IDS_BY_TOKEN_ID + lockForUpdate(),
                        row -> null, id));
            }
            execute(ApiTokenTable.UPDATE_IDENTIFIER_STATEMENT, token.identifier(), id);
            return id;
        }).orElseGet(() -> executeReturningId(new ExecStatement(ApiTokenTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, token.identifier());
                statement.setString(2, token.token());
                statement.setLong(3, token.issued());
                Sql.setLongOrNull(statement, 4, token.lastUsed());
            }
        }));
    }

    private void updatePermissions(Integer id) {
        execute(ApiTokenToPermissionTable.DELETE_BY_TOKEN_ID, id);
        executeOther(new StoreMissingWebPermissionsTransaction(permissions));
        commitMidTransaction();
        List<Integer> permissionIds = query(WebUserQueries.fetchPermissionIds(permissions));
        execute(new ExecBatchStatement(ApiTokenToPermissionTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, token.identifier());
                for (Integer permissionId : permissionIds) {
                    statement.setInt(2, permissionId);
                    statement.addBatch();
                }
            }
        });
    }
}
