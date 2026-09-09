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

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;

import java.sql.PreparedStatement;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Patch that resets AFK time of sessions with afk time of length of the session to 0.
 * <p>
 * This is a bug remedy patch that attempts to turn "bad" afk data to good.
 * In 4.5.2 there was a bug that caused some config setting defaults not being copied, along those
 * AFKThreshold setting, which lead to AFK threshold being read as 0.
 * This in turn lead to full sessions being regarded as having been AFK.
 *
 * @author AuroraLS3
 */
public class BadAFKThresholdValuePatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return !containsSessionsWithFullAFK();
    }

    private boolean containsSessionsWithFullAFK() {
        // where |afk - session_length| < 5
        String sql = SELECT + "COUNT(1) as found" + FROM + SessionsTable.TABLE_NAME +
                WHERE + "ABS(" +
                SessionsTable.AFK_TIME +
                " - (" + SessionsTable.SESSION_END + "-" + SessionsTable.SESSION_START +
                ")) < 5" +
                AND + SessionsTable.AFK_TIME + "!=0" + lockForUpdate();
        return query(new HasMoreThanZeroQueryStatement(sql, "found") {
            @Override
            public void prepare(PreparedStatement statement) {
                /* Nothing to prepare */
            }
        });
    }

    @Override
    protected void applyPatch() {
        // where |afk - session_length| < 5
        String sql = "UPDATE " + SessionsTable.TABLE_NAME + " SET " + SessionsTable.AFK_TIME + "=0" +
                WHERE + "ABS(" +
                SessionsTable.AFK_TIME +
                " - (" + SessionsTable.SESSION_END + "-" + SessionsTable.SESSION_START +
                ")) < 5" +
                AND + SessionsTable.AFK_TIME + "!=0";
        execute(sql);
    }
}