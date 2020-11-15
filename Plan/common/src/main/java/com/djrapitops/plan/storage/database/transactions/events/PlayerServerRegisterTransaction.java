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

import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Transaction for registering player's BaseUser and UserInfo to the database.
 *
 * @author Rsl1122
 */
public class PlayerServerRegisterTransaction extends PlayerRegisterTransaction {

    private final UUID serverUUID;

    public PlayerServerRegisterTransaction(UUID playerUUID, LongSupplier registered, String playerName, UUID serverUUID) {
        super(playerUUID, registered, playerName);
        this.serverUUID = serverUUID;
    }

    @Override
    protected void performOperations() {
        super.performOperations();
        long registerDate = registered.getAsLong();
        if (Boolean.FALSE.equals(query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)))) {
            execute(DataStoreQueries.registerUserInfo(playerUUID, registerDate, serverUUID));
        }

        // Updates register date to smallest possible value.
        Optional<Long> foundRegisterDate = query(PlayerFetchQueries.fetchRegisterDate(playerUUID));
        if (foundRegisterDate.isPresent() && foundRegisterDate.get() > registerDate) {
            execute(DataStoreQueries.updateMainRegisterDate(playerUUID, registerDate));
        }
    }
}