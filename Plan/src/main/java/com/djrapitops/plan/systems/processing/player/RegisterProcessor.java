/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.processing.player;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.tables.Actions;
import com.djrapitops.plan.system.database.tables.UserInfoTable;
import com.djrapitops.plan.system.database.tables.UsersTable;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Registers the user to the database and marks first session if the user has no actions.
 *
 * @author Rsl1122
 */
public class RegisterProcessor extends PlayerProcessor {

    private final long registered;
    private final long time;
    private final int playersOnline;
    private final String name;
    private final Processor[] afterProcess;

    public RegisterProcessor(UUID uuid, long registered, long time, String name, int playersOnline, Processor... afterProcess) {
        super(uuid);
        this.registered = registered;
        this.time = time;
        this.playersOnline = playersOnline;
        this.name = name;
        this.afterProcess = afterProcess;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Plan plugin = Plan.getInstance();
        Database db = plugin.getDB();
        UsersTable usersTable = db.getUsersTable();
        UserInfoTable userInfoTable = db.getUserInfoTable();
        try {
            if (!usersTable.isRegistered(uuid)) {
                usersTable.registerUser(uuid, registered, name);
            }
            if (!userInfoTable.isRegistered(uuid)) {
                userInfoTable.registerUserInfo(uuid, registered);
            }
            if (db.getActionsTable().getActions(uuid).size() > 0) {
                return;
            }
            plugin.getDataCache().markFirstSession(uuid);
            db.getActionsTable().insertAction(uuid, new Action(time, Actions.FIRST_SESSION, "Online: " + playersOnline + " Players"));
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            plugin.addToProcessQueue(afterProcess);
        }
    }
}