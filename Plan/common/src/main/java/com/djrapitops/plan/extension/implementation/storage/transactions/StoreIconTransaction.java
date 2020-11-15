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
package com.djrapitops.plan.extension.implementation.storage.transactions;

import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.ExtensionIconTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to store an Icon to the database.
 *
 * @author Rsl1122
 */
public class StoreIconTransaction extends ThrowawayTransaction {

    private final Icon icon;

    public StoreIconTransaction(Icon icon) {
        this.icon = icon;
    }

    @Override
    protected void performOperations() {
        if (Boolean.FALSE.equals(query(isIconStored()))) {
            execute(insertIcon());
        }
    }

    private Executable insertIcon() {
        String sql = "INSERT INTO " + ExtensionIconTable.TABLE_NAME + "(" +
                ExtensionIconTable.ICON_NAME + "," +
                ExtensionIconTable.FAMILY + "," +
                ExtensionIconTable.COLOR +
                ") VALUES (?,?,?)";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionIconTable.set3IconValuesToStatement(statement, icon);
            }
        };
    }

    private Query<Boolean> isIconStored() {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + ExtensionIconTable.TABLE_NAME +
                WHERE + ExtensionIconTable.ICON_NAME + "=?" +
                AND + ExtensionIconTable.FAMILY + "=?" +
                AND + ExtensionIconTable.COLOR + "=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionIconTable.set3IconValuesToStatement(statement, icon);
            }
        };
    }
}