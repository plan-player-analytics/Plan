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

import com.djrapitops.plan.delivery.domain.container.CachingSupplier;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class StoreJoinAddressTransaction extends Transaction {

    @Untrusted
    private final Supplier<String> joinAddress;
    private int newId;

    public StoreJoinAddressTransaction(@Untrusted String joinAddress) {
        this(() -> joinAddress);
    }

    public StoreJoinAddressTransaction(Supplier<String> joinAddress) {
        this.joinAddress = new CachingSupplier<>(joinAddress);
    }

    @Override
    protected boolean shouldBeExecuted() {
        return !query(hasAddressAlready());
    }

    private Query<Boolean> hasAddressAlready() {
        String sql = SELECT + "COUNT(*) as c" +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.JOIN_ADDRESS + "=?" + lockForUpdate();
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                @Untrusted String address = getAddress();
                statement.setString(1, address);
            }
        };
    }

    @Untrusted
    private String getAddress() {
        return StringUtils.truncate(joinAddress.get(), JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH);
    }

    @Override
    protected void performOperations() {
        newId = executeReturningId(new ExecStatement(JoinAddressTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getAddress());
            }
        });
    }

    public Optional<Integer> getNewId() {
        return newId == -1 ? Optional.empty() : Optional.of(newId);
    }
}
