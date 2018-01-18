/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.api.API;

/**
 * Older package version of Plan.
 *
 * @author Rsl1122
 * @deprecated Use com.djrapitops.plan.Plan instead.
 */
@Deprecated
public class Plan {

    /**
     * Old method for getting old API.
     *
     * @return new instance of the old API object to not break old API.
     * @deprecated Use PlanAPI.getInstance() instead as new API.
     */
    @Deprecated
    public static API getPlanAPI() {
        return new API(com.djrapitops.plan.Plan.getInstance());
    }

}