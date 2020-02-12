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
package com.djrapitops.plan.storage.database.transactions.commands;

import com.djrapitops.plan.delivery.domain.WebUser_old;
import com.djrapitops.plan.storage.database.sql.tables.SecurityTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Transaction to save a new Plan {@link WebUser_old} to the database.
 *
 * @author Rsl1122
 */
public class RegisterWebUserTransaction extends Transaction {

    private final WebUser_old webUser;

    public RegisterWebUserTransaction(WebUser_old webUser) {
        this.webUser = webUser;
    }

    @Override
    protected void performOperations() {
        execute(new ExecStatement(SecurityTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, webUser.getName());
                statement.setString(2, webUser.getSaltedPassHash());
                statement.setInt(3, webUser.getPermLevel());
            }
        });
    }
}