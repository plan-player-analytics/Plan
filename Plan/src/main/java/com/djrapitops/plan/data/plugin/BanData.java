/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.plugin;

import java.util.Collection;
import java.util.UUID;

/**
 * Interface for PluginData objects that affect Ban state of players.
 *
 * @author Rsl1122
 * @deprecated As of 4.3.2+ BanData has been deprecated as a way to display bans. Use Key integration instead.
 */
@Deprecated
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
