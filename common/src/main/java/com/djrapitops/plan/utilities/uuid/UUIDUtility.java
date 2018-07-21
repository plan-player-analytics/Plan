/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.uuid;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.api.utility.log.Log;

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
            return getUUIDOf(playerName, Database.getActive());
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
    private static UUID getUUIDOf(String playerName, Database db) {
        UUID uuid = null;
        if (Check.isBukkitAvailable()) {
            UUID uuidOf = DataCache.getInstance().getUUIDof(playerName);
            if (uuidOf != null) {
                return uuidOf;
            }
        }
        try {
            uuid = db.fetch().getUuidOf(playerName);
        } catch (DBOpException e) {
            if (e.isFatal()) {
                Log.toLog(UUIDUtility.class, e);
            }
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
