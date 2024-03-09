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
package com.djrapitops.plan.data.plugin;

import java.util.Collection;
import java.util.UUID;

/**
 * Interface for PluginData objects that affect Ban state of players.
 *
 * @author AuroraLS3
 * @deprecated PluginData API has been deprecated - see <a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5---DataExtension-API">wiki</a> for new API.
 */
@Deprecated(since = "5.0")
public interface BanData {

    boolean isBanned(UUID uuid);

    /**
     * Method that should return only banned players of the given UUIDs.
     *
     * @param uuids UUIDs to filter.
     * @return UUIDs from the collection uuids that are banned.
     */
    Collection<UUID> filterBanned(Collection<UUID> uuids);

}
