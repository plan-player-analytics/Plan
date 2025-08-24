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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.Key;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.ServerAggregateQueries;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.queries.objects.playertable.NetworkTablePlayersQuery;
import com.djrapitops.plan.storage.database.queries.objects.playertable.ServerTablePlayersQuery;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.StoreConfigTransaction;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.storage.database.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.storage.database.transactions.patches.BadFabricJoinAddressValuePatch;
import com.djrapitops.plan.storage.database.transactions.patches.RegisterDateMinimizationPatch;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utilities.FieldFetcher;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestPluginLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains common Database Tests.
 *
 * @author AuroraLS3
 */
public interface DatabaseTest extends DatabaseTestPreparer {

    default void saveUserOne() {
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new KickStoreTransaction(playerUUID));
    }

    default void saveUserTwo() {
        db().executeTransaction(new PlayerRegisterTransaction(player2UUID, RandomData::randomTime, TestConstants.PLAYER_TWO_NAME));
    }

    default void saveWorld(String worldName) {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worldName));
    }

    default void saveWorlds(String... worldNames) {
        for (String worldName : worldNames) {
            saveWorld(worldName);
        }
    }

    default void saveTwoWorlds() {
        saveWorlds(worlds);
    }

    @Test
    default void testRemovalSingleUser() {
        saveUserTwo();

        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        saveTwoWorlds();

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);

        db().executeTransaction(new StoreSessionTransaction(session));
        db().executeTransaction(new StoreNicknameTransaction(playerUUID, new Nickname("TestNick", RandomData.randomTime(), serverUUID()), (uuid, name) -> false /* Not cached */));
        db().executeTransaction(new StoreGeoInfoTransaction(playerUUID, new GeoInfo("TestLoc", RandomData.randomTime())));

        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        db().executeTransaction(new RemovePlayerTransaction(playerUUID));

        assertFalse(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db().query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID())));
        assertTrue(db().query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID)).isEmpty());
        assertTrue(db().query(GeoInfoQueries.fetchPlayerGeoInformation(playerUUID)).isEmpty());
        assertQueryIsEmpty(db(), SessionQueries.fetchSessionsOfPlayer(playerUUID));
    }

    default <T extends Map<?, ?>> void assertQueryIsEmpty(Database database, Query<T> query) {
        assertTrue(database.query(query).isEmpty());
    }

    default void saveGeoInfo(UUID uuid, GeoInfo geoInfo) {
        db().executeTransaction(new StoreGeoInfoTransaction(uuid, geoInfo));
    }

    @Test
    default void cleanDoesNotCleanActivePlayers() {
        saveUserOne();
        saveTwoWorlds();

        long sessionStart = System.currentTimeMillis();
        ActiveSession session = new ActiveSession(playerUUID, serverUUID(), sessionStart, worlds[0], "SURVIVAL");
        db().executeTransaction(new StoreSessionTransaction(session.toFinishedSession(sessionStart + 22345L)));

        TestPluginLogger logger = new TestPluginLogger();
        new DBCleanTask(
                config(),
                new Locale(),
                dbSystem(),
                new QuerySvc(config(), dbSystem(), serverInfo(), null),
                serverInfo(),
                logger,
                null
        ).cleanOldPlayers(db());

        Collection<BaseUser> found = db().query(BaseUserQueries.fetchAllBaseUsers());
        assertFalse(found.isEmpty(), "All users were deleted!! D:");
    }

    @Test
    default void playerContainerSupportsAllPlayerKeys() throws IllegalAccessException {
        saveUserOne();
        saveUserTwo();
        saveTwoWorlds();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));
        db().executeTransaction(new StoreNicknameTransaction(playerUUID, RandomData.randomNickname(serverUUID()), (uuid, name) -> false /* Not cached */));
        saveGeoInfo(playerUUID, new GeoInfo("TestLoc", RandomData.randomTime()));
        assertTrue(db().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        db().executeTransaction(new PingStoreTransaction(playerUUID, serverUUID(), RandomData.randomIntDateObjects()));

        PlayerContainer playerContainer = db().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomUnfinishedSession(serverUUID(), worlds, playerUUID));

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(PlayerKeys.class, Key.class);
        for (Key<?> key : keys) {
            if (!playerContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue(unsupported.isEmpty(), () -> "Some keys are not supported by PlayerContainer: PlayerKeys." + unsupported.toString());
    }

    @Test
    default void configIsStoredInTheDatabase() {
        PlanConfig config = config();

        db().executeTransaction(new StoreConfigTransaction(serverUUID(), config, System.currentTimeMillis()));

        Optional<Config> foundConfig = db().query(new NewerConfigQuery(serverUUID(), 0));
        assertTrue(foundConfig.isPresent());
        assertEquals(config, foundConfig.get());
    }

    @Test
    default void unchangedConfigDoesNotUpdateInDatabase() {
        configIsStoredInTheDatabase();
        long savedMs = System.currentTimeMillis();

        PlanConfig config = config();

        db().executeTransaction(new StoreConfigTransaction(serverUUID(), config, System.currentTimeMillis()));

        assertFalse(db().query(new NewerConfigQuery(serverUUID(), savedMs)).isPresent());
    }

    @Test
    default void indexCreationWorksWithoutErrors() throws Exception {
        Transaction transaction = new CreateIndexTransaction();
        db().executeTransaction(transaction).get(); // get to ensure transaction is finished
        assertTrue(transaction.wasSuccessful());
    }

    @Test
    default void playerCountForServersIsCorrect() {
        Map<ServerUUID, Integer> expected = Collections.singletonMap(serverUUID(), 1);
        saveUserOne();

        Map<ServerUUID, Integer> result = db().query(ServerAggregateQueries.serverUserCounts());
        assertEquals(expected, result);
    }

    @Test
    default void sqlDateConversionSanityCheck() {
        Database db = db();

        long expected = System.currentTimeMillis() / 1000;

        Sql sql = db.getType().getSql();
        String testSQL = SELECT + sql.dateToEpochSecond(sql.epochSecondToDate(Long.toString(expected))) + " as ms";

        //noinspection Convert2Diamond Causes compiler issues without Generic type definition
        long result = db.query(new QueryAllStatement<Long>(testSQL) {
            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("ms") : -1L;
            }
        });
        assertEquals(expected, result);
    }

    @Test
    @Disabled("flaky") // TODO fix sql date parsing sanity check test
    default void sqlDateParsingSanityCheck() {
        Database db = db();

        long time = System.currentTimeMillis();
        int offset = TimeZone.getDefault().getOffset(time);

        Sql sql = db.getType().getSql();
        String testSQL = SELECT + sql.dateToDayStamp(sql.epochSecondToDate(Long.toString((time + offset) / 1000))) + " as date";

        String expected = deliveryUtilities().getFormatters().iso8601NoClockLong().apply(time);
        //noinspection Convert2Diamond Causes compiler issues without Generic type definition
        String result = db.query(new QueryAllStatement<String>(testSQL) {
            @Override
            public String processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getString("date") : null;
            }
        });
        assertEquals(expected, result, () -> "Expected <" + expected + "> but was: <" + result + "> for query <" + testSQL + ">");
    }

    @Test
    default void sqlDateParsingSanitySQLDoesNotApplyTimezone() {
        Database db = db();
        config().set(FormatSettings.TIMEZONE, "UTC");

        List<org.junit.jupiter.api.function.Executable> assertions = new ArrayList<>();
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long dayStartEpochSecond = now - (now % TimeUnit.DAYS.toSeconds(1));

        for (int i = 0; i < 24; i++) {
            long hourChange = TimeUnit.HOURS.toSeconds(i);
            assertions.add(() -> {
                long time = dayStartEpochSecond + hourChange;
                int offset = 0;

                Sql sql = db.getType().getSql();
                String testSQL = SELECT + sql.dateToDayStamp(sql.epochSecondToDate(Long.toString(time + offset))) + " as date";

                String expected = deliveryUtilities().getFormatters().iso8601NoClockLong().apply(TimeUnit.SECONDS.toMillis(time));
                //noinspection Convert2Diamond Causes compiler issues without Generic type definition
                String result = db.query(new QueryAllStatement<String>(testSQL) {
                    @Override
                    public String processResults(ResultSet set) throws SQLException {
                        return set.next() ? set.getString("date") : null;
                    }
                });
                assertEquals(expected, result, () -> "Expected <" + expected + "> but was: <" + result + "> for query <" + testSQL + ">");
            });
        }
        assertAll(assertions);
    }

    @Test
    default void registerDateIsMinimized() {
        executeTransactions(
                new StoreServerPlayerTransaction(playerUUID, () -> 1000,
                        TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME)
                , new Transaction() {
                    @Override
                    protected void performOperations() {
                        execute("UPDATE " + UserInfoTable.TABLE_NAME + " SET " + UserInfoTable.REGISTERED + "=1" +
                                WHERE + UserInfoTable.USER_ID + "=(" + SELECT + "MAX(" + UsersTable.ID + ")" + FROM + UsersTable.TABLE_NAME + ")");
                    }
                }
        );

        // Check test assumptions
        Map<UUID, Long> registerDates = db().query(UserInfoQueries.fetchRegisterDates(0L, System.currentTimeMillis(), serverUUID()));
        assertEquals(1L, registerDates.get(playerUUID));
        Optional<BaseUser> baseUser = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(1000L, baseUser.isPresent() ? baseUser.get().getRegistered() : null);

        RegisterDateMinimizationPatch testedPatch = new RegisterDateMinimizationPatch();
        executeTransactions(testedPatch);

        // Test expected result
        Optional<BaseUser> updatedBaseUser = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(1L, updatedBaseUser.isPresent() ? updatedBaseUser.get().getRegistered() : null);
        assertTrue(testedPatch.isApplied());
    }

    @Test
    default void serverTablePlayersQueryQueriesAtLeastOnePlayer() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID)));

        List<TablePlayer> result = db().query(new ServerTablePlayersQuery(serverUUID(), System.currentTimeMillis(), 10L, 1));
        assertEquals(1, result.size(), () -> "Incorrect query result: " + result);
        assertNotEquals(Collections.emptyList(), result);
    }

    @Test
    default void networkTablePlayersQueryQueriesAtLeastOnePlayer() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID)));

        List<TablePlayer> result = db().query(new NetworkTablePlayersQuery(System.currentTimeMillis(), 10L, 1));
        assertEquals(1, result.size(), () -> "Incorrect query result: " + result);
    }

    @Test
    @DisplayName("BadFabricJoinAddressValuePatch removes join addresses of one server from sessions")
    default void badFabricJoinAddressPatchRemovesJoinAddressesOfOneServer() throws ExecutionException, InterruptedException {
        ServerUUID randomSecondServer = ServerUUID.randomUUID();
        db().executeTransaction(new StoreServerInformationTransaction(new Server(randomSecondServer, "", "", ""))).get();
        db().executeTransaction(new StoreWorldNameTransaction(randomSecondServer, "World"));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), "World"));

        DataMap extraData1 = new DataMap();
        extraData1.put(JoinAddress.class, new JoinAddress("test1"));
        extraData1.put(WorldTimes.class, new WorldTimes("World", GMTimes.magicNumberToGMName(0), System.currentTimeMillis()));
        FinishedSession session1 = new FinishedSession(playerUUID, serverUUID(), System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData1);
        db().executeTransaction(new StoreSessionTransaction(session1)).get();

        DataMap extraData2 = new DataMap();
        extraData2.put(JoinAddress.class, new JoinAddress("test2"));
        extraData2.put(WorldTimes.class, new WorldTimes("World", GMTimes.magicNumberToGMName(0), System.currentTimeMillis()));
        FinishedSession session2 = new FinishedSession(playerUUID, randomSecondServer, System.currentTimeMillis(), System.currentTimeMillis(), 0L, extraData2);
        db().executeTransaction(new StoreSessionTransaction(session2)).get();

        assertEquals(2, db().query(SessionQueries.fetchAllSessions()).size());


        db().executeTransaction(new BadFabricJoinAddressValuePatch(randomSecondServer)).get();


        List<String> expected = new ArrayList<>(List.of("test1", JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP));
        List<FinishedSession> sessions = db().query(SessionQueries.fetchAllSessions());
        System.out.println(sessions);
        List<String> result = sessions.stream()
                .map(session -> session.getExtraData(JoinAddress.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(JoinAddress::getAddress)
                .collect(Collectors.toList());
        Collections.sort(expected);
        Collections.sort(result);
        assertEquals(expected, result);

        result = db().query(JoinAddressQueries.allJoinAddresses());
        Collections.sort(result);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("BadFabricJoinAddressValuePatch removes join addresses of one server from plan_user_info")
    default void badFabricJoinAddressPatchRemovesJoinAddressesOfOneServerUserInfo() {
        ServerUUID randomSecondServer = ServerUUID.randomUUID();
        db().executeTransaction(new StoreServerInformationTransaction(new Server(randomSecondServer, "", "", "")));

        long time = System.currentTimeMillis();
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> time, "", serverUUID(), () -> "test1"));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> time, "", randomSecondServer, () -> "test2"));

        db().executeTransaction(new BadFabricJoinAddressValuePatch(randomSecondServer));

        Set<UserInfo> expected = Set.of(
                new UserInfo(playerUUID, serverUUID(), time, false, "test1", false),
                new UserInfo(playerUUID, randomSecondServer, time, false, null, false)
        );
        Set<UserInfo> result = db().query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        assertEquals(expected, result);
    }
}
