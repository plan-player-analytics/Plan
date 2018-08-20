/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.system.file.FileSystem;

import javax.inject.Inject;
import java.io.IOException;

/**
 * ConfigSystem for Bukkit.
 * <p>
 * Bukkit and Bungee have different default config file inside the jar.
 *
 * @author Rsl1122
 */
public class ServerConfigSystem extends ConfigSystem {

    protected final FileSystem fileSystem;

    @Inject
    public ServerConfigSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(fileSystem.readFromResource("config.yml"));
    }
}
