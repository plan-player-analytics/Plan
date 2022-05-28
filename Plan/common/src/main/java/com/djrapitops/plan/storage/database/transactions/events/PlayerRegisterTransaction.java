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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Transaction for registering player's BaseUser to the database.
 *
 * @author AuroraLS3
 */
public class PlayerRegisterTransaction extends Transaction {

    protected final UUID playerUUID;
    protected final LongSupplier registered;
    private final String playerName;

    private Integer userId;

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
        if (Boolean.FALSE.equals(query(PlayerFetchQueries.isPlayerRegistered(playerUUID)))) {
            long registerDate = registered.getAsLong();
            insertUser(registerDate);
            SessionCache.getCachedSession(playerUUID).ifPresent(session -> session.setAsFirstSessionIfMatches(registerDate));
        }
        if (!playerUUID.toString().equals(playerName)) {
            execute(DataStoreQueries.updatePlayerName(playerUUID, playerName));
        }
    }

    private void insertUser(long registerDate) {
        try {
            userId = executeReturningId(DataStoreQueries.registerBaseUser(playerUUID, registerDate, playerName));
        } catch (DBOpException failed) {
            boolean alreadySaved = failed.getMessage().contains("Duplicate entry");
            if (!alreadySaved) {
                throw failed;
            }
        }
    }

    public Optional<Integer> getUserId() {
        return Optional.ofNullable(userId);
    }
}