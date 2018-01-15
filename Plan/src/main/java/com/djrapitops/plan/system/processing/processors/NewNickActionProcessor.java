/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.processors.player.PlayerProcessor;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.api.utility.log.Log;

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
            Database.getActive().save().action(uuid, action);
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}