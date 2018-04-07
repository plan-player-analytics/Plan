/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan;

import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.settings.ColorScheme;

import java.io.File;
import java.io.InputStream;

/**
 * Abstraction interface for both Plan and PlanBungee.
 *
 * @author Rsl1122
 */
public interface PlanPlugin extends IPlugin {
    static PlanPlugin getInstance() {
        boolean bukkitAvailable = Check.isBukkitAvailable();
        boolean bungeeAvailable = Check.isBungeeAvailable();
        boolean spongeAvailable = Check.isSpongeAvailable();
        if (bukkitAvailable) {
            try {
                Plan instance = Plan.getInstance();
                if (instance != null) {
                    return instance;
                }
            } catch (IllegalStateException ignored) {
            }
        }
        if (bungeeAvailable) {
            try {
                PlanBungee instance = PlanBungee.getInstance();
                if (instance != null) {
                    return instance;
                }
            } catch (IllegalStateException ignored) {
            }
        }
        if (spongeAvailable) {
            try {
                PlanSponge instance = PlanSponge.getInstance();
                if (instance != null) {
                    return instance;
                }
            } catch (IllegalStateException ignored) {
            }
        }
        throw new IllegalAccessError("Plugin instance not available");
    }

    @Override
    File getDataFolder();

    InputStream getResource(String resource);

    ColorScheme getColorScheme();

    @Override
    boolean isReloading();
}