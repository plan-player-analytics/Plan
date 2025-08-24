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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link BanStatusUpkeepTask}.
 *
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class BanStatusUpkeepTaskTest {

    private static final int PLAYER_COUNT = 50;

    private BanStatusUpkeepTask underTest;

    @BeforeEach
    void setUp(PlanSystem system, TaskSystem taskSystem) {
        system.enable();

        underTest = taskSystem.getTask(BanStatusUpkeepTask.class).orElseThrow(AssertionError::new);
    }

    @AfterEach
    void tearDown(PlanSystem system) {
        if (system != null) {system.disable();}
    }

    @Test
    @DisplayName("BanStatusUpkeepTask updates ban status of players")
    void banStatusOfPlayersIsUpdated(Database database, ServerUUID serverUUID, ServerSensor<?> serverSensor) {
        when(serverSensor.supportsBans()).thenReturn(true);
        when(serverSensor.isBanned(any())).thenReturn(true);

        storePlayers(database, serverUUID);
        Set<Integer> expected = database.query(UserInfoQueries.userIdsOfNotBanned());
        assertEquals(PLAYER_COUNT, expected.size());

        underTest.updateBanStatus().join();
        underTest.updateBanStatus().join(); // Run twice to cover all players since first run only gets to MAX.

        Set<Integer> result = database.query(UserInfoQueries.userIdsOfBanned());
        assertEquals(expected, result);
    }

    private void storePlayers(Database database, ServerUUID serverUUID) {
        for (UUID playerUUID : RandomData.randomUUIDs(PLAYER_COUNT)) {
            String name = RandomData.randomString(25);
            database.executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> TestConstants.REGISTER_TIME,
                    name, serverUUID, TestConstants.GET_PLAYER_HOSTNAME)).join();
        }
    }
}