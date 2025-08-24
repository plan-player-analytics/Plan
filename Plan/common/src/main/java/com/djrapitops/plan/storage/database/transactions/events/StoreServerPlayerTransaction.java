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
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Transaction for registering player's BaseUser and UserInfo to the database.
 *
 * @author AuroraLS3
 */
public class StoreServerPlayerTransaction extends PlayerRegisterTransaction {

    private final ServerUUID serverUUID;
    private final Supplier<String> getJoinAddress;

    public StoreServerPlayerTransaction(UUID playerUUID, LongSupplier registered,
                                        String playerName, ServerUUID serverUUID, Supplier<String> getJoinAddress) {
        super(playerUUID, registered, playerName);
        this.serverUUID = serverUUID;
        this.getJoinAddress = getJoinAddress;
    }

    public StoreServerPlayerTransaction(UUID playerUUID, long registerDate, String name, ServerUUID serverUUID, String joinAddress) {
        this(playerUUID, () -> registerDate, name, serverUUID, () -> joinAddress);
    }

    @Override
    protected void performOperations() {
        super.performOperations();
        long registerDate = registered.getAsLong();
        String joinAddress = getJoinAddress();

        if (Boolean.FALSE.equals(query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)))) {
            execute(DataStoreQueries.registerUserInfo(playerUUID, registerDate, serverUUID, joinAddress));
        }

        // Updates register date to smallest possible value.
        Optional<Long> foundRegisterDate = query(PlayerFetchQueries.fetchRegisterDate(playerUUID));
        if (foundRegisterDate.isPresent() &&
                (foundRegisterDate.get() > registerDate
                        // Correct incorrect register dates https://github.com/plan-player-analytics/Plan/issues/2934
                        || foundRegisterDate.get() < System.currentTimeMillis() / 1000)
        ) {
            execute(DataStoreQueries.updateMainRegisterDate(playerUUID, registerDate));
        }

        execute(DataStoreQueries.updateJoinAddress(playerUUID, serverUUID, joinAddress));
    }

    private String getJoinAddress() {
        String joinAddress = this.getJoinAddress.get();
        // Removes client information given by Forge Mod Loader or Geysir
        if (joinAddress != null && StringUtils.contains(joinAddress, '\u0000')) {
            String[] split = StringUtils.split(joinAddress, "\u0000", 2);
            joinAddress = split.length > 0 ? split[0] : joinAddress;
        }

        // Truncates the address to fit database.
        return StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH);
    }
}