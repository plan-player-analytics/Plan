/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract InfoRequest that contains variables in request body.
 * <p>
 * Used to send request differently.
 *
 * @author Rsl1122
 */
public abstract class InfoRequestWithVariables implements InfoRequest {

    protected final Map<String, String> variables;

    public InfoRequestWithVariables() {
        this.variables = new HashMap<>();
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
