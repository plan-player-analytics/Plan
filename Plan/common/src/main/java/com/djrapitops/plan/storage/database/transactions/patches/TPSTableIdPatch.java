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
import com.djrapitops.plan.storage.database.sql.tables.TPSTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.TPSTable.*;

/**
 * Adds id to plan_tps table if it is not yet there.
 *
 * @author AuroraLS3
 */
public class TPSTableIdPatch extends Patch {

    private static final String TEMP_TABLE_NAME = "temp_tps_id_patch";
    private static final String TABLE_NAME = TPSTable.TABLE_NAME;

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(TABLE_NAME, TPSTable.ID) && !hasTable(TEMP_TABLE_NAME);
    }

    @Override
    protected void applyPatch() {
        try {
            tempOldTable();
            dropTable(TABLE_NAME);
            execute(TPSTable.createTableSQL(dbType));

            execute(INSERT_INTO + TABLE_NAME + " ("
                    + SERVER_ID + ','
                    + DATE + ','
                    + TPS + ','
                    + PLAYERS_ONLINE + ','
                    + CPU_USAGE + ','
                    + RAM_USAGE + ','
                    + ENTITIES + ','
                    + CHUNKS + ','
                    + FREE_DISK + ','
                    + MSPT_AVERAGE + ','
                    + MSPT_95TH_PERCENTILE
                    + ")" + SELECT
                    + SERVER_ID + ','
                    + DATE + ','
                    + TPS + ','
                    + PLAYERS_ONLINE + ','
                    + CPU_USAGE + ','
                    + RAM_USAGE + ','
                    + ENTITIES + ','
                    + CHUNKS + ','
                    + FREE_DISK + ','
                    + MSPT_AVERAGE + ','
                    + MSPT_95TH_PERCENTILE +
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
