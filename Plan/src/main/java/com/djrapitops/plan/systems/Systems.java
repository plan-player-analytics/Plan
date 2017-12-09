/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Layer for reducing
 *
 * @author Rsl1122
 */
public class Systems {

    FileSystem fileSystem;
    DatabaseSystem databaseSystem;

    public Systems(IPlan plugin) {
        databaseSystem = new DatabaseSystem();
        fileSystem = new FileSystem(plugin);
    }

    private SubSystem[] getSubSystems() {
        return new SubSystem[]{
                databaseSystem,
                fileSystem
        };
    }

    public void close() {
        SubSystem[] subSystems = getSubSystems();
        ArrayUtils.reverse(subSystems);
        for (SubSystem subSystem : subSystems) {
            subSystem.close();
        }
    }

    public static Systems getInstance() {
        return MiscUtils.getIPlan().getSystems();
    }
}