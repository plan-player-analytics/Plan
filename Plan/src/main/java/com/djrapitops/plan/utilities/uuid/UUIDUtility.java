/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.uuid;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class UUIDUtility {

    private final DataCache dataCache;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public UUIDUtility(DataCache dataCache, Database database, ErrorHandler errorHandler) {
        this.dataCache = dataCache;
        this.database = database;
        this.errorHandler = errorHandler;
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @return UUID of the player.
     */
    @Deprecated
    public static UUID getUUIDOf_Old(String playerName) {
        try {
            return Database.getActive().fetch().getUuidOf(playerName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @return UUID of the player
     */
    public UUID getUUIDOf(String playerName) {
        UUID uuid = null;
        UUID uuidOf = dataCache.getUUIDof(playerName);
        if (uuidOf != null) {
            return uuidOf;
        }
        try {
            uuid = database.fetch().getUuidOf(playerName);
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, UUIDUtility.class, e);
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
