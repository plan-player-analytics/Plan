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
package utilities;

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for saving test data to a database.
 *
 * @author AuroraLS3
 */
public class TestData {

    private TestData() {
        /* Utility class */
    }

    private static final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private static final UUID player2UUID = TestConstants.PLAYER_TWO_UUID;
    private static final ServerUUID serverUUID = TestConstants.SERVER_UUID;
    private static final ServerUUID server2UUID = TestConstants.SERVER_TWO_UUID;
    private static final String playerName = TestConstants.PLAYER_ONE_NAME;
    private static final String player2Name = TestConstants.PLAYER_TWO_NAME;

    private static final String[] serverWorldNames = new String[]{
            TestConstants.WORLD_ONE_NAME, "World Two", "world"
    };
    private static final String[] server2WorldNames = new String[]{
            "Foo", "Bar", "Z"
    };

    private static final long playerFirstJoin = 1234500L;
    private static final long playerSecondJoin = 234000L;

    private static final List<FinishedSession> playerSessions = createSessionsForPlayer(playerUUID);
    private static final List<FinishedSession> player2Sessions = createSessionsForPlayer(player2UUID);

    private static final List<GeoInfo> playerGeoInfo = createGeoInfoForPlayer();

    private static List<GeoInfo> createGeoInfoForPlayer() {
        List<GeoInfo> geoInfos = new ArrayList<>();

        geoInfos.add(new GeoInfo("Not Known", playerFirstJoin));
        geoInfos.add(new GeoInfo("Not Known", playerFirstJoin));
        geoInfos.add(new GeoInfo("Local Machine", playerFirstJoin));
        geoInfos.add(new GeoInfo("Argentina", playerFirstJoin));

        return geoInfos;
    }

    private static List<FinishedSession> createSessionsForPlayer(UUID uuid) {
        List<FinishedSession> sessions = new ArrayList<>();

        String[] gms = GMTimes.getGMKeyArray();

        ActiveSession sessionOne = new ActiveSession(uuid, serverUUID, playerFirstJoin, serverWorldNames[0], gms[0]);

        UUID otherUUID = uuid.equals(playerUUID) ? player2UUID : playerUUID;
        sessionOne.addPlayerKill(TestData.getPlayerKill(uuid, otherUUID, serverUUID, "Iron Sword", 1234750L));
        sessionOne.addPlayerKill(TestData.getPlayerKill(uuid, otherUUID, serverUUID, "Gold Sword", 1234800L));

        // Length 500ms
        sessions.add(sessionOne.toFinishedSession(1235000L));

        ActiveSession sessionTwo = new ActiveSession(uuid, server2UUID, playerSecondJoin, server2WorldNames[0], gms[1]);
        sessionTwo.changeState(server2WorldNames[1], gms[0], 334000L); // Length 100s
        // Length 200s
        sessions.add(sessionTwo.toFinishedSession(434000L));

        return sessions;
    }

    public static FinishedSession createSession(UUID uuid, ServerUUID serverUUID, long start) {
        String[] gms = GMTimes.getGMKeyArray();

        ActiveSession sessionOne = new ActiveSession(uuid, serverUUID, start, serverWorldNames[0], gms[0]);

        UUID otherUUID = uuid.equals(playerUUID) ? player2UUID : playerUUID;
        sessionOne.addPlayerKill(TestData.getPlayerKill(uuid, otherUUID, serverUUID, "Iron Sword", 1234750L));
        sessionOne.addPlayerKill(TestData.getPlayerKill(uuid, otherUUID, serverUUID, "Gold Sword", 1234800L));

        return sessionOne.toFinishedSession(start + 500L);
    }

    public static Transaction storeServers() {
        return new Transaction() {
            @Override
            protected void performOperations() {
                executeOther(new StoreServerInformationTransaction(new Server(serverUUID, "Server 1", "", TestConstants.VERSION)));
                executeOther(new StoreServerInformationTransaction(new Server(server2UUID, "Server 2", "", TestConstants.VERSION)));

                for (String worldName : serverWorldNames) {
                    executeOther(new StoreWorldNameTransaction(serverUUID, worldName));
                }
                for (String worldName : server2WorldNames) {
                    executeOther(new StoreWorldNameTransaction(server2UUID, worldName));
                }
            }
        };
    }

    public static Transaction[] storePlayerOneData() {
        return new Transaction[]{
                new PlayerRegisterTransaction(playerUUID, () -> playerFirstJoin, playerName),
                new Transaction() {
                    @Override
                    protected void performOperations() {
                        executeOther(new StoreServerPlayerTransaction(playerUUID, () -> playerFirstJoin,
                                playerName, serverUUID, TestConstants.GET_PLAYER_HOSTNAME));
                        executeOther(new StoreServerPlayerTransaction(playerUUID, () -> playerSecondJoin,
                                playerName, server2UUID, TestConstants.GET_PLAYER_HOSTNAME));

                        for (GeoInfo geoInfo : playerGeoInfo) {
                            executeOther(new StoreGeoInfoTransaction(playerUUID, geoInfo));
                        }

                        for (FinishedSession session : playerSessions) {
                            executeOther(new StoreSessionTransaction(session));
                        }
                    }
                }
        };
    }

    public static Transaction[] storePlayerTwoData() {
        return new Transaction[]{
                new PlayerRegisterTransaction(player2UUID, () -> playerFirstJoin, player2Name),
                new Transaction() {
                    @Override
                    protected void performOperations() {
                        executeOther(new StoreServerPlayerTransaction(player2UUID, () -> playerFirstJoin,
                                player2Name, serverUUID, TestConstants.GET_PLAYER_HOSTNAME));
                        executeOther(new StoreServerPlayerTransaction(player2UUID, () -> playerSecondJoin,
                                player2Name, server2UUID, TestConstants.GET_PLAYER_HOSTNAME));

                        for (GeoInfo geoInfo : playerGeoInfo) {
                            executeOther(new StoreGeoInfoTransaction(player2UUID, geoInfo));
                        }

                        for (FinishedSession session : player2Sessions) {
                            executeOther(new StoreSessionTransaction(session));
                        }
                    }
                }
        };
    }

    public static String[] getServerWorldNames() {
        return serverWorldNames;
    }

    public static String[] getServer2WorldNames() {
        return server2WorldNames;
    }

    public static List<FinishedSession> getPlayerSessions() {
        return playerSessions;
    }

    public static List<FinishedSession> getPlayer2Sessions() {
        return player2Sessions;
    }

    public static List<GeoInfo> getPlayerGeoInfo() {
        return playerGeoInfo;
    }

    public static BaseUser getPlayerBaseUser() {
        return new BaseUser(playerUUID, playerName, playerFirstJoin, 0);
    }

    public static BaseUser getPlayer2BaseUser() {
        return new BaseUser(player2UUID, player2Name, playerFirstJoin, 0);
    }

    public static PlayerKill getPlayerKill(UUID killerUUID, UUID victimUUID, ServerUUID serverUUID, String weapon, long time) {
        return new PlayerKill(
                new PlayerKill.Killer(killerUUID, getPlayerName(killerUUID)),
                new PlayerKill.Victim(victimUUID, getPlayerName(victimUUID)),
                new ServerIdentifier(serverUUID, TestConstants.SERVER_NAME),
                weapon,
                time
        );
    }

    private static String getPlayerName(UUID uuid) {
        if (playerUUID.equals(uuid)) return TestConstants.PLAYER_ONE_NAME;
        if (player2UUID.equals(uuid)) return TestConstants.PLAYER_TWO_NAME;
        return "player_name";
    }
}