/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan;

import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import net.playeranalytics.plugin.PluginInformation;

import java.io.File;
import java.io.InputStream;

/**
 * Abstraction interface for both Plan and PlanBungee.
 *
 * @author AuroraLS3
 */
public interface PlanPlugin {

    InputStream getResource(String resource);

    ColorScheme getColorScheme();

    PlanSystem getSystem();

    default boolean isSystemEnabled() {
        return getSystem().isEnabled();
    }

    void registerCommand(Subcommand command);

    void onEnable();

    void onDisable();

    /**
     * @deprecated Use {@code @Named("dataFolder") File}, or {@link PluginInformation#getDataFolder()}
     */
    @Deprecated(since = "2021-03-09")
    File getDataFolder();

}
