/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.processing.player;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.database.tables.UsersTable;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Processor that registers a new User for all servers to use as UUID - ID reference.
 *
 * @author Rsl1122
 */
public class BungeePlayerRegisterProcessor extends PlayerProcessor {

    private final String name;
    private final long registered;

    public BungeePlayerRegisterProcessor(UUID uuid, String name, long registered) {
        super(uuid);
        this.name = name;
        this.registered = registered;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        UsersTable usersTable = PlanBungee.getInstance().getDB().getUsersTable();
        try {
            if (usersTable.isRegistered(uuid)) {
                return;
            }
            usersTable.registerUser(uuid, registered, name);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}