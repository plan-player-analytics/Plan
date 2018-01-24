/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.BukkitSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BukkitAPI implements PlanAPI {

    private final BukkitSystem bukkitSystem;

    public BukkitAPI(BukkitSystem bukkitSystem) {
        this.bukkitSystem = bukkitSystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        bukkitSystem.getHookHandler().addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerInspectPageLink(UUID uuid) {
        return getPlayerInspectPageLink(getPlayerName(uuid));
    }

    @Override
    public String getPlayerInspectPageLink(String playerName) {
        return "../player/" + playerName;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return bukkitSystem.getCacheSystem().getDataCache().getName(uuid);
    }

    @Override
    public UUID playerNameToUUID(String playerName) {
        return bukkitSystem.getCacheSystem().getDataCache().getUUIDof(playerName);
    }

    @Override
    public Map<UUID, String> getKnownPlayerNames() {
        try {
            return bukkitSystem.getDatabaseSystem().getActiveDatabase().fetch().getPlayerNames();
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
            return new HashMap<>();
        }
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return bukkitSystem.getDatabaseSystem().getActiveDatabase().fetch();
    }
}