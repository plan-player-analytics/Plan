/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.uuid;

import com.djrapitops.javaplugin.utilities.UUIDFetcher;
import java.sql.SQLException;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

/**
 *
 * @author Risto
 */
public class UUIDUtility {

    public static UUID getUUIDOf(String playername) throws Exception {
        return getUUIDOf(playername, Plan.getInstance().getDB());
    }

    public static UUID getUUIDOf(String playername, Database db) throws Exception {
        UUID uuid = null;
        try {
            uuid = db.getUsersTable().getUuidOf(playername);
        } catch (SQLException e) {
            Log.toLog("UUIDUtility", e);
        }
        if (uuid == null) {
            uuid = UUIDFetcher.getUUIDOf(playername);
        }
        return uuid;
    }
}
