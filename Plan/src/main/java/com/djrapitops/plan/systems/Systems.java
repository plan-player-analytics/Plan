/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.config.BukkitConfigSystem;
import com.djrapitops.plan.systems.file.database.DBSystem;
import com.djrapitops.plan.systems.file.database.PlanBungeeDBSystem;
import com.djrapitops.plan.systems.file.database.PlanDBSystem;
import com.djrapitops.plan.systems.tasks.PlanBungeeTaskSystem;
import com.djrapitops.plan.systems.tasks.PlanTaskSystem;
import com.djrapitops.plan.systems.tasks.TaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.systems.webserver.WebServerSystem;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
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
        configSystem = new BukkitConfigSystem();
        databaseSystem = new PlanDBSystem();
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());

        taskSystem = new PlanTaskSystem();

        webServerSystem = new WebServerSystem();
        themeSystem = new Theme();
    }

    /**
     * Constructor for Bungee version.
     *
     * @param plugin PlanBungee instance
     */
    public Systems(PlanBungee plugin) {
        fileSystem = new FileSystem(plugin);
        configSystem = new BungeeConfigSystem();
        databaseSystem = new PlanBungeeDBSystem();
        versionCheckSystem = new VersionCheckSystem(plugin.getVersion());

        taskSystem = new PlanBungeeTaskSystem();

        webServerSystem = new WebServerSystem();
        themeSystem = new Theme();
    }

    private SubSystem[] getSubSystems() {
        return new SubSystem[]{
                fileSystem,
                configSystem,
                versionCheckSystem,
                databaseSystem,
                taskSystem,
                webServerSystem,
                themeSystem
        };
    }

    public void close() {
        SubSystem[] subSystems = getSubSystems();
        ArrayUtils.reverse(subSystems);
        for (SubSystem subSystem : subSystems) {
            try {
                subSystem.disable();
            } catch (Exception e) {
                Log.toLog(Systems.class, e);
            }
        }
    }

    public static Systems getInstance() {
        return PlanPlugin.getInstance().getSystems();
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