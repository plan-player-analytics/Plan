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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Transaction for registering player's BaseUser and UserInfo to the database.
 *
 * @author AuroraLS3
 */
public class PlayerServerRegisterTransaction extends PlayerRegisterTransaction {

    private final ServerUUID serverUUID;
    private final Supplier<String> hostname;

    public PlayerServerRegisterTransaction(UUID playerUUID, LongSupplier registered,
                                           String playerName, ServerUUID serverUUID, Supplier<String> hostname) {
        super(playerUUID, registered, playerName);
        this.serverUUID = serverUUID;
        this.hostname = hostname;
    }

    @Override
    protected void performOperations() {
        super.performOperations();
        long registerDate = registered.getAsLong();
        if (Boolean.FALSE.equals(query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)))) {
            execute(DataStoreQueries.registerUserInfo(playerUUID, registerDate, serverUUID, hostname.get()));
        }

        // Updates register date to smallest possible value.
        Optional<Long> foundRegisterDate = query(PlayerFetchQueries.fetchRegisterDate(playerUUID));
        if (foundRegisterDate.isPresent() && foundRegisterDate.get() > registerDate) {
            execute(DataStoreQueries.updateMainRegisterDate(playerUUID, registerDate));
        }
    }
}