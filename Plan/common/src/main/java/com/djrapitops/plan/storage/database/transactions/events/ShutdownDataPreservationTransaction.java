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

import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.PlayerKills;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.*;
import java.util.function.LongSupplier;

public class ShutdownDataPreservationTransaction extends Transaction {

    private final List<FinishedSession> finishedSessions;

    public ShutdownDataPreservationTransaction(List<FinishedSession> finishedSessions) {
        this.finishedSessions = finishedSessions;
    }

    @Override
    protected void performOperations() {
        ensureAllPlayersAreRegistered();

        execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(finishedSessions));
    }

    private void ensureAllPlayersAreRegistered() {
        Set<UUID> playerUUIDs = new HashSet<>();
        Map<UUID, String> playerNames = new HashMap<>();
        Map<UUID, Long> earliestDates = new HashMap<>();
        for (FinishedSession finishedSession : finishedSessions) {
            UUID playerUUID = finishedSession.getPlayerUUID();
            playerUUIDs.add(playerUUID);
            finishedSession.getExtraData(PlayerKills.class)
                    .map(PlayerKills::asList)
                    .ifPresent(kills -> {
                        for (PlayerKill kill : kills) {
                            playerUUIDs.add(kill.getKiller().getUuid());
                            playerUUIDs.add(kill.getVictim().getUuid());
                        }
                    });

            finishedSession.getExtraData(PlayerName.class)
                    .map(PlayerName::get)
                    .ifPresent(playerName -> playerNames.put(playerUUID, playerName));
            long start = finishedSession.getStart();
            Long previous = earliestDates.get(playerUUID);
            if (previous == null || start < previous) {
                earliestDates.put(playerUUID, start);
            }
        }

        Set<UUID> existingUUIDs = query(BaseUserQueries.fetchExistingUUIDs(playerUUIDs));

        for (UUID playerUUID : playerUUIDs) {
            if (!existingUUIDs.contains(playerUUID)) {
                LongSupplier registerDate = () -> Optional.ofNullable(earliestDates.get(playerUUID))
                        .orElseGet(System::currentTimeMillis);
                String playerName = Optional.ofNullable(playerNames.get(playerUUID))
                        .orElseGet(playerUUID::toString);
                executeOther(new PlayerRegisterTransaction(playerUUID, registerDate, playerName));
            }
        }
    }
}
