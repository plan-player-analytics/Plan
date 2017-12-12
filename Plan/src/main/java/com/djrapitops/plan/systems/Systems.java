/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.systems.store.FileSystem;
import main.java.com.djrapitops.plan.systems.store.config.ConfigSystem;
import main.java.com.djrapitops.plan.systems.store.config.PlanBungeeConfigSystem;
import main.java.com.djrapitops.plan.systems.store.config.PlanConfigSystem;
import main.java.com.djrapitops.plan.systems.store.database.DBSystem;
import main.java.com.djrapitops.plan.systems.store.database.PlanBungeeDBSystem;
import main.java.com.djrapitops.plan.systems.store.database.PlanDBSystem;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Layer for reducing
 *
 * @author Rsl1122
 */
public class Systems {

    private FileSystem fileSystem;
    private DBSystem databaseSystem;
    private ConfigSystem configSystem;

    /**
     * Constructor for Bukkit version.
     *
     * @param plugin Plan instance
     */
    public Systems(Plan plugin) {
        fileSystem = new FileSystem(plugin);
        configSystem = new PlanConfigSystem();
        databaseSystem = new PlanDBSystem();

    }

    /**
     * Constructor for Bungee version.
     *
     * @param plugin PlanBungee instance
     */
    public Systems(PlanBungee plugin) {
        fileSystem = new FileSystem(plugin);
        configSystem = new PlanBungeeConfigSystem();
        databaseSystem = new PlanBungeeDBSystem();
    }

    private SubSystem[] getSubSystems() {
        return new SubSystem[]{
                fileSystem,
                configSystem,
                databaseSystem
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

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public DBSystem getDatabaseSystem() {
        return databaseSystem;
    }

    public ConfigSystem getConfigSystem() {
        return configSystem;
    }
}