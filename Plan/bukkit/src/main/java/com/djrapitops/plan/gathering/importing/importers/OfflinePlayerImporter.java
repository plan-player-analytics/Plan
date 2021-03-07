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
package com.djrapitops.plan.gathering.importing.importers;

import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.gathering.importing.data.ServerImportData;
import com.djrapitops.plan.gathering.importing.data.UserImportData;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Fuzzlemann
 */
@Singleton
public class OfflinePlayerImporter extends BukkitImporter {

    @Inject
    public OfflinePlayerImporter(
            GeolocationCache geolocationCache,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        super(geolocationCache, dbSystem, serverInfo, "offline");
    }

    @Override
    public ServerImportData getServerImportData() {
        return null;
    }

    @Override
    public List<UserImportData> getUserImportData() {
        List<UserImportData> dataList = new ArrayList<>();

        Set<OfflinePlayer> operators = Bukkit.getOperators();
        Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();

        Arrays.stream(Bukkit.getOfflinePlayers()).parallel().forEach(player -> {
            UserImportData.UserImportDataBuilder builder = UserImportData.builder(serverUUID.get());
            builder.name(player.getName())
                    .uuid(player.getUniqueId())
                    .registered(player.getFirstPlayed());

            if (operators.contains(player)) {
                builder.op();
            }

            if (banned.contains(player)) {
                builder.banned();
            }

            dataList.add(builder.build());
        });

        return dataList;
    }
}
