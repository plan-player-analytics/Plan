/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.processing;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.database.tables.Actions;
import com.djrapitops.plan.systems.processing.player.PlayerProcessor;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.api.utility.log.Log;

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

        String info = HtmlUtils.removeXSS(displayName);

        Action action = new Action(MiscUtils.getTime(), Actions.NEW_NICKNAME, info);

        try {
            Plan.getInstance().getDB().getActionsTable().insertAction(uuid, action);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}