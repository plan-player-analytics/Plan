/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.utilities.NullCheck;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class InfoSystem implements SubSystem {

    protected final ConnectionSystem connectionSystem;

    public InfoSystem(ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
    }

    public static InfoSystem getInstance() {
        InfoSystem infoSystem = PlanSystem.getInstance().getInfoSystem();
        NullCheck.check(infoSystem, new IllegalStateException("Info System was not initialized."));
        return infoSystem;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    public ConnectionSystem getConnectionSystem() {
        return connectionSystem;
    }
}