/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.processing.Processor;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Processor for updating the network page.
 *
 * @author Rsl1122
 */
public class NetworkPageUpdateProcessor implements Processor {

    @Override
    public void process() {
        try {
            InfoSystem.getInstance().updateNetworkPage();
        } catch (WebException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}