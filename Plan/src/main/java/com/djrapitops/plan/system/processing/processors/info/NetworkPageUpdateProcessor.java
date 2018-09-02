/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;

/**
 * Processor for updating the network page.
 *
 * @author Rsl1122
 */
public class NetworkPageUpdateProcessor implements Runnable {

    private final InfoSystem infoSystem;
    private final WebExceptionLogger webExceptionLogger;

    NetworkPageUpdateProcessor(
            InfoSystem infoSystem,
            WebExceptionLogger webExceptionLogger
    ) {
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        webExceptionLogger.logIfOccurs(this.getClass(), infoSystem::updateNetworkPage);
    }
}
