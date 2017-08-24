/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;

import java.sql.SQLException;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BanAndOpProcessor extends PlayerProcessor {

    private final boolean banned;
    private final boolean opped;

    public BanAndOpProcessor(UUID uuid, boolean banned, boolean op) {
        super(uuid);
        this.banned = banned;
        opped = op;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        try {
            Plan.getInstance().getDB().getUserInfoTable().updateOpAndBanStatus(uuid, opped, banned);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}