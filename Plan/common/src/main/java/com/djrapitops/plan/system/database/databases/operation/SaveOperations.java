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
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.Config;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Operation methods for saving data.
 * <p>
 * Note: Method names subject to change
 *
 * @author Rsl1122
 */
@Deprecated
public interface SaveOperations {

    // Bulk save

    @Deprecated
    void insertTPS(Map<UUID, List<TPS>> ofServers);

    @Deprecated
    void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers);

    @Deprecated
    void insertUsers(Map<UUID, UserInfo> ofServers);

    @Deprecated
    void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData);

    @Deprecated
    void kickAmount(Map<UUID, Integer> ofUsers);

    @Deprecated
    void insertUserInfo(Map<UUID, List<UserInfo>> ofServers);

    @Deprecated
    void insertNicknames(Map<UUID, Map<UUID, List<Nickname>>> ofServers);

    @Deprecated
    void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers);

    // Single data point

    @Deprecated
    void banStatus(UUID uuid, boolean banned);

    @Deprecated
    void opStatus(UUID uuid, boolean op);

    @Deprecated
    void playerWasKicked(UUID uuid);

    @Deprecated
    void playerDisplayName(UUID uuid, Nickname nickname);

    @Deprecated
    void insertTPSforThisServer(TPS tps);

    @Deprecated
    void session(UUID uuid, Session session);

    @Deprecated
    void serverInfoForThisServer(Server server);

    @Deprecated
    void webUser(WebUser webUser);

    @Deprecated
    void setAsUninstalled(UUID serverUUID);

    @Deprecated
    void saveConfig(UUID serverUUID, Config config, long lastModified);
}