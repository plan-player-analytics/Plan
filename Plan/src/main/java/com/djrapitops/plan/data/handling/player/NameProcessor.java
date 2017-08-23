/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.handling.player;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;

import java.sql.SQLException;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class NameProcessor extends PlayerProcessor {

    private final String playerName;
    private final String displayName;

    public NameProcessor(UUID uuid, String playerName, String displayName) {
        super(uuid);
        this.playerName = playerName;
        this.displayName = displayName;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Database db = Plan.getInstance().getDB();
        try {
            db.getUsersTable().updateName(uuid, playerName);
            db.getNicknamesTable().saveUserName(uuid, displayName);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}