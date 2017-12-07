/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;

/**
 * Layer for reducing
 *
 * @author Rsl1122
 */
public class Systems implements SubSystem {

    DatabaseSystem databaseSystem;

    public Systems() {
        this.databaseSystem = new DatabaseSystem();
    }

    private SubSystem[] getSubSystems() {
        return new SubSystem[]{
                databaseSystem
        };
    }

    @Override
    public void init() throws PlanEnableException {
        for (SubSystem subSystem : getSubSystems()) {
            subSystem.init();
        }
    }

    @Override
    public void close() {
        for (SubSystem subSystem : getSubSystems()) {
            subSystem.close();
        }
    }

    public static Systems getInstance() {
        return Plan.getInstance().getSystems();
    }
}