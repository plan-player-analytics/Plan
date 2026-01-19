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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Adds id to plan_security if it is not yet there.
 *
 * @author AuroraLS3
 */
public class SecurityTableIdPatch extends Patch {

    private static final String TEMP_TABLE_NAME = "temp_security_id_patch";
    private static final String TABLE_NAME = SecurityTable.TABLE_NAME;
    private static final String PERMISSION_LEVEL = "permission_level";

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(TABLE_NAME, SecurityTable.ID) && !hasTable(TEMP_TABLE_NAME);
    }

    @Override
    protected void applyPatch() {
        boolean hasUuidLinks = hasColumn(TABLE_NAME, SecurityTable.LINKED_TO);
        if (hasUuidLinks) {
            patchWithLinkedUuids();
        } else {
            patchSimpleTable();
        }
    }

    private void patchSimpleTable() {
        try {
            tempOldTable();
            dropTable(TABLE_NAME);
            execute(CreateTableBuilder.create(TABLE_NAME, dbType)
                    .column(ID, INT).primaryKey()
                    .column(SecurityTable.USERNAME, varchar(100)).notNull().unique()
                    .column(SecurityTable.SALT_PASSWORD_HASH, varchar(100)).notNull().unique()
                    .column(PERMISSION_LEVEL, INT)
                    .toString());

            execute(INSERT_INTO + TABLE_NAME + " (" +
                    SecurityTable.USERNAME + ',' +
                    SecurityTable.SALT_PASSWORD_HASH + ',' +
                    PERMISSION_LEVEL +
                    ") " + SELECT +
                    SecurityTable.USERNAME + ',' +
                    SecurityTable.SALT_PASSWORD_HASH + ',' +
                    PERMISSION_LEVEL +
                    FROM + TEMP_TABLE_NAME
            );

            dropTable(TEMP_TABLE_NAME);
        } catch (Exception e) {
            throw new DBOpException(SecurityTableGroupPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void patchWithLinkedUuids() {
        try {
            tempOldTable();
            dropTable(TABLE_NAME);
            execute(CreateTableBuilder.create(TABLE_NAME, dbType)
                    .column(ID, INT).primaryKey()
                    .column(SecurityTable.USERNAME, varchar(100)).notNull().unique()
                    .column(SecurityTable.SALT_PASSWORD_HASH, varchar(100)).notNull().unique()
                    .column(SecurityTable.LINKED_TO, varchar(36)).defaultValue("''")
                    .column(PERMISSION_LEVEL, INT)
                    .toString());

            execute(INSERT_INTO + TABLE_NAME + " (" +
                    SecurityTable.USERNAME + ',' +
                    SecurityTable.SALT_PASSWORD_HASH + ',' +
                    SecurityTable.LINKED_TO + ',' +
                    PERMISSION_LEVEL +
                    ") " + SELECT +
                    SecurityTable.USERNAME + ',' +
                    SecurityTable.SALT_PASSWORD_HASH + ',' +
                    SecurityTable.LINKED_TO + ',' +
                    PERMISSION_LEVEL +
                    FROM + TEMP_TABLE_NAME
            );

            dropTable(TEMP_TABLE_NAME);
        } catch (Exception e) {
            throw new DBOpException(SecurityTableGroupPatch.class.getSimpleName() + " failed.", e);
        }
    }

    private void tempOldTable() {
        if (!hasTable(TEMP_TABLE_NAME)) {
            renameTable(TABLE_NAME, TEMP_TABLE_NAME);
        }
    }
}
