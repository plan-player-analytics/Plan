/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * ConfigSystem for Bukkit.
 * <p>
 * Bukkit and Bungee have different default config file inside the jar.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitConfigSystem extends ConfigSystem {

    @Inject
    public BukkitConfigSystem(
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            ErrorHandler errorHandler
    ) {
        super(files, config, theme, errorHandler);
    }

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(files.readFromResource("config.yml"));
    }

    @Override
    public void enable() throws EnableException {
        super.enable();
        config.getNetworkSettings().loadSettingsFromDB();
    }
}
