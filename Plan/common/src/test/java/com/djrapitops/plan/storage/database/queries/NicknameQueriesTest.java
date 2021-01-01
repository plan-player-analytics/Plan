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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.NicknameQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.NicknameStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerServerRegisterTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface NicknameQueriesTest extends DatabaseTestPreparer {

    @Test
    default void allNicknamesAreSaved() {
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), "play.example.com"));

        List<Nickname> saved = RandomData.randomNicknames(serverUUID());
        for (Nickname nickname : saved) {
            db().executeTransaction(new NicknameStoreTransaction(playerUUID, nickname, (uuid, name) -> false /* Not cached */));
            db().executeTransaction(new NicknameStoreTransaction(playerUUID, nickname, (uuid, name) -> true /* Cached */));
        }

        forcePersistenceCheck();

        List<Nickname> fetched = db().query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID));
        assertEquals(saved, fetched);
    }

    @Test
    default void nicknameMatchingFindsNicknames() {
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);

        db().executeTransaction(new PlayerRegisterTransaction(uuid, () -> 0L, userName));
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1L, "Not random"));

        String nickname = "2" + RandomData.randomString(10);
        db().executeTransaction(new NicknameStoreTransaction(uuid, new Nickname(nickname, System.currentTimeMillis(), serverUUID()), (u, name) -> false /* Not cached */));
        db().executeTransaction(new NicknameStoreTransaction(playerUUID, new Nickname("No nick", System.currentTimeMillis(), serverUUID()), (u, name) -> true /* Cached */));

        String searchFor = "2";

        List<String> result = db().query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }

    @Test
    default void removeEverythingRemovesNicknames() {
        allNicknamesAreSaved();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(NicknameQueries.fetchAllNicknameData()).isEmpty());
    }
}