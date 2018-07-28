/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.info.server.Server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Operation methods for saving data.
 * <p>
 * Note: Method names subject to change (TODO remove insert update and such)
 *
 * @author Rsl1122
 */
public interface SaveOperations {

    // Bulk save

    void insertTPS(Map<UUID, List<TPS>> ofServers);

    void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers);

    void insertUsers(Map<UUID, UserInfo> ofServers);

    void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData);

    void kickAmount(Map<UUID, Integer> ofUsers);

    void insertUserInfo(Map<UUID, List<UserInfo>> ofServers);

    void insertNicknames(Map<UUID, Map<UUID, List<Nickname>>> ofServers);

    void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers);

    // Single data point

    void banStatus(UUID uuid, boolean banned);

    void opStatus(UUID uuid, boolean op);

    void registerNewUser(UUID uuid, long registered, String name);

    void geoInfo(UUID uuid, GeoInfo geoInfo);

    void playerWasKicked(UUID uuid);

    void playerName(UUID uuid, String playerName);

    void playerDisplayName(UUID uuid, Nickname nickname);

    void registerNewUserOnThisServer(UUID uuid, long registered);

    void commandUsed(String commandName);

    void insertTPSforThisServer(TPS tps);

    void session(UUID uuid, Session session);

    void serverInfoForThisServer(Server server);

    void webUser(WebUser webUser);

    void ping(UUID uuid, Ping ping);
}