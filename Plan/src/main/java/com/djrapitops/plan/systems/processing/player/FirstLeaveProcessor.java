/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.database.tables.Actions;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Processor for inserting a FIRST_LOGOUT Action.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class FirstLeaveProcessor extends PlayerProcessor {

    private final Action leaveAction;

    public FirstLeaveProcessor(UUID uuid, long time, int messagesSent) {
        super(uuid);
        leaveAction = new Action(time, Actions.FIRST_LOGOUT, "Messages sent: " + messagesSent);
    }

    @Override
    public void process() {
        try {
            Plan.getInstance().getDB().getActionsTable().insertAction(getUUID(), leaveAction);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}