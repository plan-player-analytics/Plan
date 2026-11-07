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

import com.djrapitops.plan.storage.database.sql.tables.webuser.ApiTokenTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.ApiTokenToPermissionTable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.dev.Untrusted;

/**
 * Deletes a token from the database.
 *
 * @author AuroraLS3
 */
public class DeleteApiTokenTransaction extends Transaction {

    private final @Untrusted String token;

    public DeleteApiTokenTransaction(String token) {this.token = token;}

    @Override
    protected void performOperations() {
        var foundId = query(db -> db.queryOptional(ApiTokenTable.SELECT_TOKEN_ID,
                row -> row.getInt(ApiTokenTable.ID), token));
        if (foundId.isEmpty()) return;

        execute(ApiTokenToPermissionTable.DELETE_BY_TOKEN_ID, foundId.get());
        execute(ApiTokenTable.DELETE_BY_ID_STATEMENT, foundId.get());
    }
}
