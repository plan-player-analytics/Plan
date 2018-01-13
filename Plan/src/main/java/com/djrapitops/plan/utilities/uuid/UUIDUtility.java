/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.uuid;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class UUIDUtility {

    /**
     * Constructor used to hide the public constructor
     */
    private UUIDUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @return UUID of the player.
     */
    public static UUID getUUIDOf(String playerName) {
        try {
            return getUUIDOf(playerName, PlanPlugin.getInstance().getDB());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @param db         Database to check from.
     * @return UUID of the player
     */
    public static UUID getUUIDOf(String playerName, Database db) {
        UUID uuid = null;
        if (Check.isBukkitAvailable()) {
            UUID uuidOf = Plan.getInstance().getDataCache().getUUIDof(playerName);
            if (uuidOf != null) {
                return uuidOf;
            }
        }
        try {
            uuid = db.getUsersTable().getUuidOf(playerName);
        } catch (SQLException e) {
            Log.toLog("UUIDUtility", e);
        }
        try {
            if (uuid == null) {
                uuid = UUIDFetcher.getUUIDOf(playerName);
            }
        } catch (Exception | NoClassDefFoundError ignored) {
            /* Ignored */
        }
        return uuid;
    }
}
