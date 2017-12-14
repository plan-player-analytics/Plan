/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.systems.file.FileSystem;
import main.java.com.djrapitops.plan.systems.file.config.ConfigSystem;
import main.java.com.djrapitops.plan.systems.file.config.PlanBungeeConfigSystem;
import main.java.com.djrapitops.plan.systems.file.config.PlanConfigSystem;
import main.java.com.djrapitops.plan.systems.file.database.DBSystem;
import main.java.com.djrapitops.plan.systems.file.database.PlanBungeeDBSystem;
import main.java.com.djrapitops.plan.systems.file.database.PlanDBSystem;
import main.java.com.djrapitops.plan.systems.tasks.PlanBungeeTaskSystem;
import main.java.com.djrapitops.plan.systems.tasks.PlanTaskSystem;
import main.java.com.djrapitops.plan.systems.tasks.TaskSystem;
import main.java.com.djrapitops.plan.systems.update.VersionCheckSystem;
import main.java.com.djrapitops.plan.systems.webserver.WebServerSystem;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Layer for reducing
 *
 * @author Rsl1122
 */
public class Systems {

    private FileSystem fileSystem;
    private ConfigSystem configSystem;
    private DBSystem databaseSystem;
    private Theme themeSystem;

    private WebServerSystem webServerSystem;

    private TaskSystem taskSystem;

    private VersionCheckSystem versionCheckSystem;

    /**
     * Constructor for Bukkit version.
     *
     * @param plugin Plan instance
     */
    public Systems(Plan plugin) {
        fileSystem = new FileSystem(plugin);
        configSystem = new PlanConfigSystem();
        databaseSystem = new PlanDBSystem();
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());

        taskSystem = new PlanTaskSystem();

        webServerSystem = new WebServerSystem(plugin);
        themeSystem = new Theme();
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
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());

        taskSystem = new PlanBungeeTaskSystem();

        webServerSystem = new WebServerSystem(plugin);
        themeSystem = new Theme();
    }

    private SubSystem[] getSubSystems() {
        return new SubSystem[]{
                fileSystem,
                configSystem,
                databaseSystem,
                webServerSystem
        };
    }

    public void close() {
        SubSystem[] subSystems = getSubSystems();
        ArrayUtils.reverse(subSystems);
        for (SubSystem subSystem : subSystems) {
            try {
                subSystem.close();
            } catch (Exception e) {
                Log.toLog(Systems.class, e);
            }
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

    public WebServerSystem getWebServerSystem() {
        return webServerSystem;
    }

    public VersionCheckSystem getVersionCheckSystem() {
        return versionCheckSystem;
    }

    public Theme getThemeSystem() {
        return themeSystem;
    }

    public TaskSystem getTaskSystem() {
        return taskSystem;
    }
}