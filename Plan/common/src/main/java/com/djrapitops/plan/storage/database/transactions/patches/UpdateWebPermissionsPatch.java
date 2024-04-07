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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebPermissionTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds missing web permissions to the permission table.
 *
 * @author AuroraLS3
 */
public class UpdateWebPermissionsPatch extends Patch {

    private List<String> missingPermissions;

    @Override
    public boolean hasBeenApplied() {
        List<String> defaultPermissions = Arrays.stream(WebPermission.nonDeprecatedValues())
                .map(WebPermission::getPermission)
                .collect(Collectors.toList());
        List<String> storedPermissions = query(WebUserQueries.fetchAvailablePermissions());
        missingPermissions = new ArrayList<>();
        for (String permission : defaultPermissions) {
            if (!storedPermissions.contains(permission)) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions.isEmpty();
    }

    @Override
    protected void applyPatch() {
        storeMissing();
    }

    private void storeMissing() {
        execute(new ExecBatchStatement(WebPermissionTable.safeInsertSQL(dbType)) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String permission : missingPermissions) {
                    statement.setString(1, permission);
                    statement.addBatch();
                }
            }
        });
    }
}
