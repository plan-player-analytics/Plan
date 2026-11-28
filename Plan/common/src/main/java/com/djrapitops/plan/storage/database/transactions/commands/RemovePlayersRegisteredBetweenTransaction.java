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
package com.djrapitops.plan.storage.database.transactions.commands;

import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Transaction for removing all players whose first join date is in a given range.
 */
public class RemovePlayersRegisteredBetweenTransaction extends Transaction {

    private final long after;
    private final long before;
    private final Set<UUID> removedPlayerUUIDs = new LinkedHashSet<>();

    public RemovePlayersRegisteredBetweenTransaction(long after, long before) {
        if (after > before) throw new IllegalArgumentException("after can not be later than before");
        this.after = after;
        this.before = before;
    }

    @Override
    protected void performOperations() {
        removedPlayerUUIDs.addAll(query(BaseUserQueries.playerUUIDsOfRegisteredBetween(after, before)));
        for (UUID playerUUID : removedPlayerUUIDs) {
            executeOther(new RemovePlayerTransaction(playerUUID));
        }
    }

    public Set<UUID> getRemovedPlayerUUIDs() {
        return Collections.unmodifiableSet(removedPlayerUUIDs);
    }
}
