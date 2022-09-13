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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Transaction to store player's nickname information in the database.
 *
 * @author AuroraLS3
 */
public class StoreNicknameTransaction extends ThrowawayTransaction {

    private final UUID playerUUID;
    private final Nickname nickname;
    private final BiPredicate<UUID, String> isNicknameCachedCheck;

    public StoreNicknameTransaction(UUID playerUUID, Nickname nickname, BiPredicate<UUID, String> isNicknameCachedCheck) {
        this.playerUUID = playerUUID;
        this.nickname = nickname;
        this.isNicknameCachedCheck = isNicknameCachedCheck;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return !isNicknameCachedCheck.test(playerUUID, nickname.getName());
    }

    @Override
    protected void performOperations() {
        execute(DataStoreQueries.storePlayerNickname(playerUUID, nickname));
    }
}