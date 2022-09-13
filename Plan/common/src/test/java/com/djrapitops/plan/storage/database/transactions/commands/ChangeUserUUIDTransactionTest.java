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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.NicknameQueries;
import com.djrapitops.plan.storage.database.transactions.events.StoreNicknameTransaction;
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
public interface ChangeUserUUIDTransactionTest extends DatabaseTestPreparer {

    @Test
    default void uuidChangeMovesUserData() {
        UUID oldUUID = UUID.randomUUID();
        UUID newUUID = UUID.randomUUID();
        UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

        Nickname nickname = new Nickname(TestConstants.PLAYER_ONE_NAME, System.currentTimeMillis(), serverUUID());

        DataMap extraData1 = new DataMap();
        extraData1.put(WorldTimes.class, new WorldTimes(Map.of(TestConstants.WORLD_ONE_NAME, new GMTimes(Map.of(GMTimes.SURVIVAL, 200L)))));
        extraData1.put(PlayerKills.class, new PlayerKills(new ArrayList<>(List.of(new PlayerKill(new PlayerKill.Killer(oldUUID, TestConstants.PLAYER_ONE_NAME), new PlayerKill.Victim(player2UUID, TestConstants.PLAYER_TWO_NAME), new ServerIdentifier(serverUUID(), TestConstants.SERVER_NAME), TestConstants.WEAPON_SWORD, System.currentTimeMillis())))));
        FinishedSession player1Session = new FinishedSession(oldUUID, serverUUID(), System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData1);

        DataMap extraData2 = new DataMap();
        extraData2.put(WorldTimes.class, new WorldTimes(Map.of(TestConstants.WORLD_ONE_NAME, new GMTimes(Map.of(GMTimes.SURVIVAL, 200L)))));
        extraData2.put(PlayerKills.class, new PlayerKills(new ArrayList<>(List.of(new PlayerKill(new PlayerKill.Killer(player2UUID, TestConstants.PLAYER_ONE_NAME), new PlayerKill.Victim(oldUUID, TestConstants.PLAYER_TWO_NAME), new ServerIdentifier(serverUUID(), TestConstants.SERVER_NAME), TestConstants.WEAPON_SWORD, System.currentTimeMillis())))));
        FinishedSession player2Session = new FinishedSession(player2UUID, serverUUID(), System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData2);

        Database db = db();
        db.executeTransaction(new StoreWorldNameTransaction(serverUUID(), TestConstants.WORLD_ONE_NAME));
        db.executeTransaction(new StoreServerPlayerTransaction(player2UUID, System::currentTimeMillis, TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreServerPlayerTransaction(oldUUID, System::currentTimeMillis, TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreNicknameTransaction(oldUUID, nickname, (uuid, name) -> false));
        db.executeTransaction(new StoreSessionTransaction(player1Session));
        db.executeTransaction(new StoreSessionTransaction(player2Session));

        // Assert data was stored with the old UUID
        List<Nickname> expectedNicknames = List.of(nickname);
        List<Nickname> nicknames = db.query(NicknameQueries.fetchNicknameDataOfPlayer(oldUUID));
        assertEquals(expectedNicknames, nicknames);
        assertEquals(1, db.query(KillQueries.fetchPlayerKillsOfPlayer(oldUUID)).size());
        assertEquals(1, db.query(KillQueries.fetchPlayerDeathsOfPlayer(oldUUID)).size());
        assertTrue(db.query(BaseUserQueries.fetchUserId(oldUUID)).isPresent());


        db.executeTransaction(new ChangeUserUUIDTransaction(oldUUID, newUUID));


        // Assert data exists with the new UUID
        expectedNicknames = List.of(nickname);
        nicknames = db.query(NicknameQueries.fetchNicknameDataOfPlayer(newUUID));
        assertEquals(expectedNicknames, nicknames);
        assertEquals(1, db.query(KillQueries.fetchPlayerKillsOfPlayer(newUUID)).size());
        assertEquals(1, db.query(KillQueries.fetchPlayerDeathsOfPlayer(newUUID)).size());
        assertTrue(db.query(BaseUserQueries.fetchUserId(newUUID)).isPresent());

        // Assert data doesn't exist with the old UUID
        assertTrue(db.query(NicknameQueries.fetchNicknameDataOfPlayer(oldUUID)).isEmpty());
        assertTrue(db.query(KillQueries.fetchPlayerKillsOfPlayer(oldUUID)).isEmpty());
        assertTrue(db.query(KillQueries.fetchPlayerDeathsOfPlayer(oldUUID)).isEmpty());
        assertTrue(db.query(BaseUserQueries.fetchUserId(oldUUID)).isEmpty());
    }

}