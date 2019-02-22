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
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.pluginbridge.plan.aac.HackerTable.*;

/**
 * Transaction to store kick information when AAC kicks a player for hacking.
 *
 * @author Rsl1122
 */
public class StoreHackViolationKickTransaction extends Transaction {

    private final HackObject info;

    public StoreHackViolationKickTransaction(HackObject info) {
        this.info = info;
    }

    @Override
    protected void performOperations() {
        execute(storeViolationKick());
    }

    private Executable storeViolationKick() {
        String sql = "INSERT INTO " + TABLE_NAME + " ("
                + COL_UUID + ", "
                + COL_DATE + ", "
                + COL_HACK_TYPE + ", "
                + COL_VIOLATION_LEVEL
                + ") VALUES (?, ?, ?, ?)";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, info.getUuid().toString());
                statement.setLong(2, info.getDate());
                statement.setString(3, info.getHackType());
                statement.setInt(4, info.getViolationLevel());
            }
        };
    }
}