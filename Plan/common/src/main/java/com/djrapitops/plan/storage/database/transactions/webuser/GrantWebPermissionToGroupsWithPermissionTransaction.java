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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupToPermissionTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Adds a permission to any group that already has the other given permission.
 *
 * @author AuroraLS3
 */
public class GrantWebPermissionToGroupsWithPermissionTransaction extends Transaction {

    private final String permissionToGive;
    private final String whenHasPermission;

    public GrantWebPermissionToGroupsWithPermissionTransaction(String permissionToGive, String whenHasPermission) {
        this.permissionToGive = permissionToGive;
        this.whenHasPermission = whenHasPermission;
    }

    @Override
    protected void performOperations() {
        List<String> groupsWithPermission = query(WebUserQueries.fetchGroupNamesWithPermission(whenHasPermission));
        List<String> groupsWithPermissionGiven = query(WebUserQueries.fetchGroupNamesWithPermission(permissionToGive));
        groupsWithPermission.removeAll(groupsWithPermissionGiven);

        List<Integer> groupIds = query(WebUserQueries.fetchGroupIds(groupsWithPermission));
        if (groupIds.isEmpty()) return;
        Integer permissionId = query(WebUserQueries.fetchPermissionId(permissionToGive))
                .orElseThrow(() -> new DBOpException("Permission called '" + permissionToGive + "' not found in database."));

        @Language("SQL")
        String sql = INSERT_INTO + WebGroupToPermissionTable.TABLE_NAME + '(' +
                WebGroupToPermissionTable.GROUP_ID + ',' + WebGroupToPermissionTable.PERMISSION_ID +
                ") VALUES (?, ?)";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(2, permissionId);
                for (Integer groupId : groupIds) {
                    statement.setInt(1, groupId);
                    statement.addBatch();
                }
            }
        });
    }
}
