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

import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.command.ColorScheme;

import java.io.File;
import java.io.InputStream;

/**
 * Abstraction interface for both Plan and PlanBungee.
 *
 * @author AuroraLS3
 */
public interface PlanPlugin extends IPlugin {

    @Override
    File getDataFolder();

    InputStream getResource(String resource);

    ColorScheme getColorScheme();

    @Override
    boolean isReloading();

    PlanSystem getSystem();

    default boolean isSystemEnabled() {
        return getSystem().isEnabled();
    }

    void registerCommand(Subcommand command);

    default void cancelAllTasks() {
        getRunnableFactory().cancelAllKnownTasks();
    }
}
