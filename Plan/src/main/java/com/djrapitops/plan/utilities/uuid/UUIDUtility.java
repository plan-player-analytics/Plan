/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.uuid;

import com.djrapitops.plugin.utilities.player.UUIDFetcher;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class UUIDUtility {

    /**
     * @param playername
     * @return
     */
    public static UUID getUUIDOf(String playername) {
        try {
            return getUUIDOf(playername, Plan.getInstance().getDB());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param playername
     * @param db
     * @return
     */
    public static UUID getUUIDOf(String playername, Database db) {
        UUID uuid = null;
        try {
            uuid = db.getUsersTable().getUuidOf(playername);
        } catch (SQLException e) {
            Log.toLog("UUIDUtility", e);
        }
        try {
            if (uuid == null) {
                uuid = UUIDFetcher.getUUIDOf(playername);
            }
        } catch (Exception e) {
        }
        return uuid;
    }
}
