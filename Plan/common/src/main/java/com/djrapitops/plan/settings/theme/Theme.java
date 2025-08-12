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
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author AuroraLS3
 */
@Singleton
public class Theme implements SubSystem {

    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;

    @Inject
    public Theme(PlanFiles files, PlanConfig config, PluginLogger logger) {
        this.files = files;
        this.config = config;
        this.logger = logger;
    }

    public String[] getPieColors(ThemeVal variable) {
        return Arrays.stream(StringUtils.split(variable.getDefaultValue()/*TODO handle new config path*/, ','))
                .map(color -> StringUtils.remove(StringUtils.trim(color), '"'))
                .toArray(String[]::new);
    }

    @Override
    public void enable() {
        // TODO write migration logic for moving specific colors to config
        ThemeConfig themeConfig = new ThemeConfig(files, config, logger);
    }

    @Override
    public void disable() {
        // No need to save theme on disable
    }
}
