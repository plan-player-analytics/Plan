/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.processing.processors.Processor;
import com.djrapitops.plan.systems.info.InformationManager;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class NetworkPageUpdateProcessor extends Processor<InformationManager> {

    public NetworkPageUpdateProcessor(InformationManager object) {
        super(object);
    }

    @Override
    public void process() {
        object.updateNetworkPageContent();
    }
}