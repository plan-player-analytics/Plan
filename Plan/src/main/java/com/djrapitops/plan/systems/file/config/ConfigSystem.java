/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.file.config;

import com.djrapitops.plan.api.exceptions.PlanEnableException;
import com.djrapitops.plan.systems.SubSystem;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plan.systems.file.FileSystem;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class ConfigSystem implements SubSystem {

    protected Config config;

    public static ConfigSystem getInstance() {
        return Systems.getInstance().getConfigSystem();
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public void init() throws PlanEnableException {
        try {
            config = new Config(FileSystem.getConfigFile());
            copyDefaults();
            config.save();
        } catch (IOException e) {
            throw new PlanEnableException("Config Subsystem failed to initialize", e);
        }
    }

    protected abstract void copyDefaults() throws IOException;

    @Override
    public void close() {

    }

    public static void reload() {
        try {
            getInstance().config.read();
        } catch (IOException e) {
            Log.toLog(ConfigSystem.class, e);
        }
    }
}