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

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
public interface CombineUserTransactionTest extends DatabaseTestPreparer {

    @Test
    default void uuidChangeCombinesData() {
        UUID oldUUID = UUID.randomUUID();
        UUID newUUID = UUID.randomUUID();
        UUID player2UUID = TestConstants.PLAYER_TWO_UUID;
        ServerUUID server2UUID = TestConstants.SERVER_TWO_UUID;

        DataMap extraData1 = new DataMap();
        extraData1.put(WorldTimes.class, new WorldTimes(Map.of(TestConstants.WORLD_ONE_NAME, new GMTimes(Map.of(GMTimes.SURVIVAL, 200L)))));
        extraData1.put(PlayerKills.class, new PlayerKills(new ArrayList<>(List.of(new PlayerKill(new PlayerKill.Killer(oldUUID, TestConstants.PLAYER_ONE_NAME), new PlayerKill.Victim(player2UUID, TestConstants.PLAYER_TWO_NAME), new ServerIdentifier(serverUUID(), TestConstants.SERVER_NAME), TestConstants.WEAPON_SWORD, System.currentTimeMillis())))));
        FinishedSession sessionOnOldUUID = new FinishedSession(oldUUID, serverUUID(), System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData1);

        DataMap extraData2 = new DataMap();
        extraData2.put(WorldTimes.class, new WorldTimes(Map.of(TestConstants.WORLD_ONE_NAME, new GMTimes(Map.of(GMTimes.SURVIVAL, 200L)))));
        extraData2.put(PlayerKills.class, new PlayerKills(new ArrayList<>(List.of(new PlayerKill(new PlayerKill.Killer(newUUID, TestConstants.PLAYER_ONE_NAME), new PlayerKill.Victim(player2UUID, TestConstants.PLAYER_TWO_NAME), new ServerIdentifier(server2UUID, TestConstants.SERVER_TWO_NAME), TestConstants.WEAPON_SWORD, System.currentTimeMillis())))));
        FinishedSession sessionOnNewUUID = new FinishedSession(newUUID, server2UUID, System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData2);

        Database db = db();
        db.executeTransaction(new StoreServerInformationTransaction(new Server(server2UUID, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));
        db.executeTransaction(new StoreWorldNameTransaction(serverUUID(), TestConstants.WORLD_ONE_NAME));
        db.executeTransaction(new StoreWorldNameTransaction(server2UUID, TestConstants.WORLD_ONE_NAME));
        db.executeTransaction(new StoreServerPlayerTransaction(player2UUID, System::currentTimeMillis, TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreServerPlayerTransaction(player2UUID, System::currentTimeMillis, TestConstants.PLAYER_TWO_NAME, server2UUID, TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreServerPlayerTransaction(oldUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreServerPlayerTransaction(newUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME, server2UUID, TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreSessionTransaction(sessionOnOldUUID));
        db.executeTransaction(new StoreSessionTransaction(sessionOnNewUUID));


        db.executeTransaction(new CombineUserTransaction(oldUUID, newUUID));


        // Assert data exists with the new UUID
        assertTrue(db.query(BaseUserQueries.fetchUserId(newUUID)).isPresent());
        assertEquals(2, db.query(SessionQueries.fetchSessionsOfPlayer(newUUID)).values().stream().mapToInt(List::size).sum());
        assertEquals(2, db.query(KillQueries.fetchPlayerKillsOfPlayer(newUUID)).size());
        assertEquals(2, db.query(UserInfoQueries.fetchUserInformationOfUser(newUUID)).size());

        // Assert data doesn't exist with the old UUID
        assertTrue(db.query(BaseUserQueries.fetchUserId(oldUUID)).isEmpty());
        assertTrue(db.query(UserInfoQueries.fetchUserInformationOfUser(oldUUID)).isEmpty());
        assertTrue(db.query(KillQueries.fetchPlayerKillsOfPlayer(oldUUID)).isEmpty());
    }

}