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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.datatransfer.preferences.Preferences;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.transactions.BackupCopyTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebUserPreferencesTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface DatabaseBackupTest extends DatabaseTestPreparer {

    default void saveDataForBackup() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        db().executeTransaction(
                new StoreNicknameTransaction(playerUUID, RandomData.randomNickname(serverUUID()), (uuid, name) -> false /* Not cached */)
        );
        db().executeTransaction(new StoreGeoInfoTransaction(playerUUID, new GeoInfo("TestLoc", RandomData.randomTime())));

        List<TPS> expected = RandomData.randomTPS();
        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID(), tps));
        }

        db().executeTransaction(new PingStoreTransaction(
                playerUUID, serverUUID(),
                Collections.singletonList(new DateObj<>(System.currentTimeMillis(), RandomData.randomInt(-1, 40))))
        );

        User user = new User("test", "console", null, PassEncryptUtil.createHash("testPass"), "admin", Collections.emptyList());
        db().executeTransaction(new StoreWebUserTransaction(user));

        Preferences defaultPreferences = config().getDefaultPreferences();
        String json = new Gson().toJson(defaultPreferences);
        db().executeTransaction(new StoreWebUserPreferencesTransaction(json, user.toWebUser()));
    }

    @Test
    default void testBackupAndRestoreSQLite() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = dbSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();

            saveDataForBackup();

            backup.executeTransaction(new BackupCopyTransaction(db(), backup));

            assertQueryResultIsEqual(db(), backup, BaseUserQueries.fetchAllBaseUsers());
            assertQueryResultIsEqual(db(), backup, UserInfoQueries.fetchAllUserInformation());
            assertQueryResultIsEqual(db(), backup, NicknameQueries.fetchAllNicknameData());
            assertQueryResultIsEqual(db(), backup, GeoInfoQueries.fetchAllGeoInformation());
            assertQueryResultIsEqual(db(), backup, SessionQueries.fetchAllSessions());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllWorldNames());
            assertQueryResultIsEqual(db(), backup, LargeFetchQueries.fetchAllTPSData());
            assertQueryResultIsEqual(db(), backup, ServerQueries.fetchPlanServerInformation());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchAllUsers());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchGroupNames());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchAvailablePermissions());
            assertQueryResultIsEqual(db(), backup, WebUserQueries.fetchAllPreferences());

        } finally {
            backup.close();
        }
    }

    default <T> void assertQueryResultIsEqual(Database one, Database two, Query<T> query) {
        assertEquals(one.query(query), two.query(query));
    }
}
