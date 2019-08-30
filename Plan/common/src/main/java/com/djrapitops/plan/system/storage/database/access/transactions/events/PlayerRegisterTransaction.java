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
package com.djrapitops.plan.system.storage.database.access.transactions.events;

import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.storage.database.access.queries.DataStoreQueries;
import com.djrapitops.plan.system.storage.database.access.queries.PlayerFetchQueries;
import com.djrapitops.plan.system.storage.database.access.transactions.Transaction;

import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Transaction for registering player's BaseUser to the database.
 *
 * @author Rsl1122
 */
public class PlayerRegisterTransaction extends Transaction {

    protected final UUID playerUUID;
    protected final LongSupplier registered;
    private final String playerName;

    public PlayerRegisterTransaction(UUID playerUUID, LongSupplier registered, String playerName) {
        this.playerUUID = playerUUID;
        this.registered = registered;
        this.playerName = playerName;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return playerUUID != null && playerName != null;
    }

    @Override
    protected void performOperations() {
        if (!query(PlayerFetchQueries.isPlayerRegistered(playerUUID))) {
            long registerDate = registered.getAsLong();
            execute(DataStoreQueries.registerBaseUser(playerUUID, registerDate, playerName));
            SessionCache.getCachedSession(playerUUID).ifPresent(session -> session.setAsFirstSessionIfMatches(registerDate));
        }
        execute(DataStoreQueries.updatePlayerName(playerUUID, playerName));
    }
}