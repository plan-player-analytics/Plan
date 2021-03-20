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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.storage.database.sql.tables.CookieTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CookieChangeTransaction extends Transaction {

    private final String username;
    private final String cookie; // Null if removing
    private final Long expires;

    private CookieChangeTransaction(String username, String cookie, Long expires) {
        this.username = username;
        this.cookie = cookie;
        this.expires = expires;
    }

    public static CookieChangeTransaction storeCookie(String username, String cookie, long expires) {
        return new CookieChangeTransaction(username, cookie, expires);
    }

    public static CookieChangeTransaction removeCookie(String username) {
        return new CookieChangeTransaction(username, null, null);
    }

    public static CookieChangeTransaction removeAll() {
        return new CookieChangeTransaction(null, null, null);
    }

    @Override
    protected void performOperations() {
        if (username == null) {
            execute(new ExecStatement(CookieTable.DELETE_ALL_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    // No parameters
                }
            });
        } else if (cookie == null) {
            execute(new ExecStatement(CookieTable.DELETE_BY_USER_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, username);
                }
            });
            // Perform cleanup at the same time
            execute(new ExecStatement(CookieTable.DELETE_OLDER_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setLong(1, System.currentTimeMillis());
                }
            });
        } else {
            execute(new ExecStatement(CookieTable.INSERT_STATEMENT) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, username);
                    statement.setString(2, cookie);
                    statement.setLong(3, expires);
                }
            });
        }
    }
}
