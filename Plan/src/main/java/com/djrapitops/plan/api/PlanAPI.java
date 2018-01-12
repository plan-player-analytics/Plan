/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.PluginData;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public interface PlanAPI {

    static PlanAPI getInstance() {
        throw new IllegalAccessError("Not yet implemented"); // TODO
    }

    void registerPluginData(PluginData pluginData);
}