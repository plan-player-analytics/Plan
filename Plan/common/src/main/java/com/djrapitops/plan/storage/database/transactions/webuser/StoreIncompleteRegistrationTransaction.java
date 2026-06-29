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

import com.djrapitops.plan.delivery.webserver.auth.IncompleteRegistration;
import com.djrapitops.plan.storage.database.sql.tables.webuser.RegistrationTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert incomplete registration information to the database.
 *
 * @author AuroraLS3
 */
public class StoreIncompleteRegistrationTransaction extends Transaction {

    private final IncompleteRegistration registration;

    public StoreIncompleteRegistrationTransaction(IncompleteRegistration registration) {this.registration = registration;}

    @Override
    protected void performOperations() {
        execute(new ExecStatement(RegistrationTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, registration.getUsername());
                statement.setString(2, registration.getPasswordHash());
                statement.setString(3, registration.getCode());
                statement.setLong(4, registration.getExpiresAfter());
            }
        });
    }
}
