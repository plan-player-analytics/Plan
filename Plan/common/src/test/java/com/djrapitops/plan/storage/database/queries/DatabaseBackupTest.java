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
import com.djrapitops.plan.delivery.domain.datatransfer.AllowlistBounce;
import com.djrapitops.plan.delivery.domain.datatransfer.preferences.Preferences;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.processors.move.DatabaseCopyProcessor;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.queries.objects.lookup.LookupTableQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.sql.tables.webuser.*;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.patches.WebGroupDefaultGroupsPatch;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebUserPreferencesTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestErrorLogger;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface DatabaseBackupTest extends DatabaseTestPreparer {

    default void saveDataForBackup(Database db, ServerUUID serverUUID) {
        db.executeTransaction(new RemoveEverythingTransaction()).join();
        db.executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID, TestConstants.SERVER_NAME, "", ""))).join();
        db.executeTransaction(new StoreWorldNameTransaction(serverUUID, worlds[0]));
        db.executeTransaction(new StoreWorldNameTransaction(serverUUID, worlds[1]));
        db.executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID, TestConstants.GET_PLAYER_HOSTNAME));
        db.executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID, TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID, worlds, playerUUID, player2UUID);
        db.executeTransaction(new StoreSessionTransaction(session));

        db.executeTransaction(
                new StoreNicknameTransaction(playerUUID, RandomData.randomNickname(serverUUID), (uuid, name) -> false /* Not cached */)
        );
        db.executeTransaction(new StoreGeoInfoTransaction(playerUUID, new GeoInfo("TestLoc", RandomData.randomTime())));

        List<TPS> expected = RandomData.randomTPS();
        for (TPS tps : expected) {
            db.executeInTransaction(DataStoreQueries.storeTPS(serverUUID, tps));
        }

        db.executeTransaction(new PingStoreTransaction(
                playerUUID, serverUUID,
                Collections.singletonList(new DateObj<>(System.currentTimeMillis(), RandomData.randomInt(-1, 40))))
        );

        User user = new User("test", "console", null, PassEncryptUtil.createHash("testPass"), "admin", Collections.emptyList());
        db.executeTransaction(new StoreWebUserTransaction(user));

        Preferences defaultPreferences = config().getDefaultPreferences();
        String json = new Gson().toJson(defaultPreferences);
        db.executeTransaction(new StoreWebUserPreferencesTransaction(json, user.toWebUser()));

        List<PluginMetadata> changeSet = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2"),
                new PluginMetadata("LittleFX", null)
        );
        db.executeTransaction(new StorePluginVersionsTransaction(System.currentTimeMillis(), serverUUID, changeSet));

        AllowlistBounce bounce = new AllowlistBounce(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, 1, System.currentTimeMillis());
        db.executeTransaction(new StoreAllowlistBounceTransaction(bounce.getPlayerUUID(), bounce.getPlayerName(), serverUUID, bounce.getLastTime())).join();

        db.executeTransaction(new WebGroupDefaultGroupsPatch());
        db.executeTransaction(new StoreWebGroupTransaction("admin", List.of("page", "access", "manage.groups", "manage.users")));
    }

    @Test
    default void backupToSQLite() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = dbSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> db().getState() == Database.State.OPEN);
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> backup.getState() == Database.State.OPEN);

            saveDataForBackup(db(), serverUUID());

            List<String> feedback = new ArrayList<>();

            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), db(), backup, feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();

            for (String s : feedback) {
                System.out.println(s);
            }

            assertSame(db(), backup);
        } finally {
            backup.close();
        }
    }

    @Test
    default void backupToSQLiteAndRestore() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = dbSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> db().getState() == Database.State.OPEN);
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> backup.getState() == Database.State.OPEN);

            saveDataForBackup(db(), serverUUID());

            List<String> feedback = new ArrayList<>();

            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), db(), backup, feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();
            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), backup, db(), feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();

            for (String s : feedback) {
                System.out.println(s);
            }

            assertSame(db(), backup);
        } finally {
            backup.close();
        }
    }

    @Test
    default void backupToSQLiteAndRestoreTwice() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = dbSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> db().getState() == Database.State.OPEN);
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> backup.getState() == Database.State.OPEN);

            saveDataForBackup(db(), serverUUID());

            List<String> feedback = new ArrayList<>();

            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), db(), backup, feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();
            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), backup, db(), feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();
            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), backup, db(), feedback::add, DatabaseCopyProcessor.Strategy.CLEAR_DESTINATION_DATABASE)
                    .run();

            for (String s : feedback) {
                System.out.println(s);
            }

            assertSame(db(), backup);
        } finally {
            backup.close();
        }
    }

    @Test
    default void databaseMerge() throws Exception {
        File tempFile = Files.createTempFile(system().getPlanFiles().getDataFolder().toPath(), "backup-", ".db").toFile();
        tempFile.deleteOnExit();
        SQLiteDB backup = dbSystem().getSqLiteFactory().usingFile(tempFile);
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        try {
            backup.init();
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> db().getState() == Database.State.OPEN);
            Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> backup.getState() == Database.State.OPEN);

            saveDataForBackup(db(), serverUUID());
            saveDataForBackup(backup, TestConstants.SERVER_TWO_UUID);

            Map<String, Integer> beforeBackupFrom = db().query(LookupTableQueries.tableCounts());
            Map<String, Integer> beforeBackupTo = backup.query(LookupTableQueries.tableCounts());
            Set<String> joinAddresses = new HashSet<>(backup.query(JoinAddressQueries.allJoinAddresses()));
            joinAddresses.addAll(db().query(JoinAddressQueries.allJoinAddresses()));
            List<String> feedback = new ArrayList<>();

            Map<String, Object> beforeBackupDataFrom = getDataCorrectnessSet(db());
            Map<String, Object> beforeBackupDataTo = getDataCorrectnessSet(backup);

            new DatabaseCopyProcessor(new Locale(), new TestErrorLogger(), db(), backup, feedback::add)
                    .run();

            for (String s : feedback) {
                System.out.println(s);
            }

            Map<String, Integer> expected = new TreeMap<>();
            // These tables have merging rules based on conditions
            expected.put(UsersTable.TABLE_NAME, 2);
            expected.put(NicknamesTable.TABLE_NAME, 2);
            expected.put(GeoInfoTable.TABLE_NAME, 1);
            // These tables don't insert duplicates
            expected.put(JoinAddressTable.TABLE_NAME, joinAddresses.size());
            expected.put(SecurityTable.TABLE_NAME, 1);
            expected.put(WebUserPreferencesTable.TABLE_NAME, 1);
            // These tables insert if two servers are different
            expected.put(AccessLogTable.TABLE_NAME, beforeBackupTo.get(AccessLogTable.TABLE_NAME) + beforeBackupFrom.get(AccessLogTable.TABLE_NAME));
            expected.put(ServerTable.TABLE_NAME, beforeBackupTo.get(ServerTable.TABLE_NAME) + beforeBackupFrom.get(ServerTable.TABLE_NAME));
            expected.put(WorldTable.TABLE_NAME, beforeBackupTo.get(WorldTable.TABLE_NAME) + beforeBackupFrom.get(WorldTable.TABLE_NAME));
            expected.put(UserInfoTable.TABLE_NAME, beforeBackupTo.get(UserInfoTable.TABLE_NAME) + beforeBackupFrom.get(UserInfoTable.TABLE_NAME));
            expected.put(SessionsTable.TABLE_NAME, beforeBackupTo.get(SessionsTable.TABLE_NAME) + beforeBackupFrom.get(SessionsTable.TABLE_NAME));
            expected.put(KillsTable.TABLE_NAME, beforeBackupTo.get(KillsTable.TABLE_NAME) + beforeBackupFrom.get(KillsTable.TABLE_NAME));
            expected.put(WorldTimesTable.TABLE_NAME, beforeBackupTo.get(WorldTimesTable.TABLE_NAME) + beforeBackupFrom.get(WorldTimesTable.TABLE_NAME));
            expected.put(TPSTable.TABLE_NAME, beforeBackupTo.get(TPSTable.TABLE_NAME) + beforeBackupFrom.get(TPSTable.TABLE_NAME));
            expected.put(PingTable.TABLE_NAME, beforeBackupTo.get(PingTable.TABLE_NAME) + beforeBackupFrom.get(PingTable.TABLE_NAME));
            expected.put(PluginVersionTable.TABLE_NAME, beforeBackupTo.get(PluginVersionTable.TABLE_NAME) + beforeBackupFrom.get(PluginVersionTable.TABLE_NAME));
            expected.put(AllowlistBounceTable.TABLE_NAME, beforeBackupTo.get(AllowlistBounceTable.TABLE_NAME) + beforeBackupFrom.get(AllowlistBounceTable.TABLE_NAME));
            expected.put(WebGroupTable.TABLE_NAME, beforeBackupTo.get(WebGroupTable.TABLE_NAME));
            expected.put(WebPermissionTable.TABLE_NAME, beforeBackupTo.get(WebPermissionTable.TABLE_NAME));
            expected.put(WebGroupToPermissionTable.TABLE_NAME, beforeBackupTo.get(WebGroupToPermissionTable.TABLE_NAME));
            Map<String, Integer> result = backup.query(LookupTableQueries.tableCounts());
            assertEquals(expected, result);

            Map<String, Object> expectedData = mergeCorrectnessSet(beforeBackupDataFrom, beforeBackupDataTo);
            Map<String, Object> resultData = getDataCorrectnessSet(backup);
            assertEquals(expectedData, resultData);
        } finally {
            backup.close();
        }
    }

    private @NonNull Map<String, Object> getDataCorrectnessSet(Database db) {
        return Map.of(
                "playtime", db.query(SessionQueries.playtime(0, Long.MAX_VALUE)),
                "playtime_uuid1_1", db.query(SessionQueries.playtimeOfPlayer(0, Long.MAX_VALUE, TestConstants.PLAYER_ONE_UUID)).getOrDefault(serverUUID(), 0L),
                "playtime_uuid1_2", db.query(SessionQueries.playtimeOfPlayer(0, Long.MAX_VALUE, TestConstants.PLAYER_ONE_UUID)).getOrDefault(TestConstants.SERVER_TWO_UUID, 0L),
                "playtime_uuid2_1", db.query(SessionQueries.playtimeOfPlayer(0, Long.MAX_VALUE, TestConstants.PLAYER_TWO_UUID)).getOrDefault(serverUUID(), 0L),
                "playtime_uuid2_2", db.query(SessionQueries.playtimeOfPlayer(0, Long.MAX_VALUE, TestConstants.PLAYER_TWO_UUID)).getOrDefault(TestConstants.SERVER_TWO_UUID, 0L),
                "seen_uuid1", db.query(SessionQueries.lastSeen(TestConstants.PLAYER_ONE_UUID)),
                "seen_uuid2", db.query(SessionQueries.lastSeen(TestConstants.PLAYER_TWO_UUID)),
                "registered1", db.query(BaseUserQueries.fetchBaseUserOfPlayer(TestConstants.PLAYER_ONE_UUID)).get().getRegistered()
        );
    }

    private Map<String, Object> mergeCorrectnessSet(Map<String, Object> one, Map<String, Object> two) {
        return Map.of(
                "playtime", (long) one.get("playtime") + (long) two.get("playtime"),
                "playtime_uuid1_1", (long) one.get("playtime_uuid1_1") + (long) two.get("playtime_uuid1_1"),
                "playtime_uuid1_2", (long) one.get("playtime_uuid1_2") + (long) two.get("playtime_uuid1_2"),
                "playtime_uuid2_1", (long) one.get("playtime_uuid2_1") + (long) two.get("playtime_uuid2_1"),
                "playtime_uuid2_2", (long) one.get("playtime_uuid2_2") + (long) two.get("playtime_uuid2_2"),
                "seen_uuid1", Math.max((long) one.get("seen_uuid1"), (long) two.get("seen_uuid1")),
                "seen_uuid2", Math.max((long) one.get("seen_uuid2"), (long) two.get("seen_uuid2")),
                "registered1", Math.min((long) one.get("registered1"), (long) two.get("registered1"))
        );
    }

    private void assertSame(Database from, Database to) {
        assertAll(
                assertQueryResultIsEqual(from, to, BaseUserQueries.fetchAllBaseUsers()),
                assertQueryResultIsEqual(from, to, UserInfoQueries.fetchAllUserInformation()),
                assertQueryResultIsEqual(from, to, NicknameQueries.fetchAllNicknameData()),
                assertQueryResultIsEqual(from, to, GeoInfoQueries.fetchAllGeoInformation()),
                assertQueryResultIsEqual(from, to, SessionQueries.fetchAllSessions()),
                assertQueryResultIsEqual(from, to, LargeFetchQueries.fetchAllWorldNames()),
                assertQueryResultIsEqual(from, to, LargeFetchQueries.fetchAllTPSData()),
                assertQueryResultIsEqual(from, to, ServerQueries.fetchPlanServerInformation()),
                assertQueryResultIsEqual(from, to, WebUserQueries.fetchAllUsers()),
                assertQueryResultIsEqual(from, to, WebUserQueries.fetchGroupNames()),
                assertQueryResultIsEqual(from, to, WebUserQueries.fetchAvailablePermissions()),
                assertQueryResultIsEqual(from, to, WebUserQueries.fetchAllPreferences()),
                assertQueryResultIsEqual(from, to, PluginMetadataQueries.getPluginHistory()),
                assertQueryResultIsEqual(from, to, AllowlistQueries.getBounces()),
                assertQueryResultIsEqual(from, to, LookupTableQueries.tableCounts())
        );
    }

    default <T> Executable assertQueryResultIsEqual(Database one, Database two, Query<T> query) {
        return () -> assertEquals(one.query(query), two.query(query));
    }

    @Test
    default void removeEverythingRemovesEverything() {
        saveDataForBackup(db(), serverUUID());

        db().executeTransaction(new RemoveEverythingTransaction()).join();

        Map<String, Integer> tableCounts = db().query(LookupTableQueries.tableCounts());
        assertAll(tableCounts.entrySet().stream()
                .filter(entry -> !JoinAddressTable.TABLE_NAME.equals(entry.getKey()))
                .map(
                        entry -> () -> assertEquals(0, entry.getValue(), () -> entry.getKey() + " was not empty: " + entry.getValue() + " rows.")
                ));
    }
}
