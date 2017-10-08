/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.database.tables.Actions;
import main.java.com.djrapitops.plan.systems.processing.player.PlayerProcessor;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Processor for inserting a Name Change action to the Actions table.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class NewNickActionProcessor extends PlayerProcessor {

    private final String displayName;

    public NewNickActionProcessor(UUID uuid, String displayName) {
        super(uuid);
        this.displayName = displayName;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();

        String n = HtmlUtils.swapColorsToSpan(displayName);

        String info = HtmlUtils.removeXSS(n);

        Action action = new Action(MiscUtils.getTime(), Actions.NEW_NICKNAME, info);

        try {
            Plan.getInstance().getDB().getActionsTable().insertAction(uuid, action);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}