/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.system.file.FileSystem;

import java.io.IOException;

/**
 * ConfigSystem for Bukkit.
 * <p>
 * Bukkit and Bungee have different default config file inside the jar.
 *
 * @author Rsl1122
 */
public class BukkitConfigSystem extends ConfigSystem {

    @Override
    protected void copyDefaults() throws IOException {
        config.copyDefaults(FileSystem.readFromResource("config.yml"));
    }
}