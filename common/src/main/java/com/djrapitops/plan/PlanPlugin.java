/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.settings.ColorScheme;

import java.io.File;
import java.io.InputStream;

/**
 * Abstraction interface for both Plan and PlanBungee.
 *
 * @author Rsl1122
 */
public interface PlanPlugin extends IPlugin {
    @Override
    File getDataFolder();

    InputStream getResource(String resource);

    ColorScheme getColorScheme();

    @Override
    boolean isReloading();
}
