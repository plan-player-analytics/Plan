/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.system.file.FileSystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * ConfigSystem for Bungee.
 * <p>
 * Bukkit and Bungee have different default config file inside the jar.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeConfigSystem extends ConfigSystem {

    @Inject
    public BungeeConfigSystem(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(fileSystem.readFromResource("bungeeconfig.yml"));
    }
}
