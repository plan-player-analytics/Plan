/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.processing.processors.Processor;

/**
 * Processor for updating the network page.
 *
 * @author Rsl1122
 */
public class NetworkPageUpdateProcessor extends Processor<InfoSystem> {

    public NetworkPageUpdateProcessor() {
        super(null);
    }

    @Override
    public void process() {
        InfoSystem.getInstance().updateNetworkPage();
    }
}