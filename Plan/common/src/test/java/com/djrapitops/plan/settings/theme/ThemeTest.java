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

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class ThemeTest {

    @AfterAll
    static void afterAll(PlanSystem system) {
        if (system != null) system.disable();
    }

    @BeforeEach
    void setUp(PlanSystem system) {
        system.enable();
    }

    @Test
    void freshThemeFileContainsOnlyDefaultValues(Theme theme, PlanFiles files, PlanConfig config, PluginLogger logger) {
        assertFalse(theme.containsNonDefaultValues(new ThemeConfig(files, config, logger)));
    }

    @Test
    void freshThemeFileContainsNonDefaultValues(Theme theme, PlanFiles files, PlanConfig config, PluginLogger logger) {
        ThemeConfig modifiedConfig = new ThemeConfig(files, config, logger);
        modifiedConfig.set("Colors.black", "#000");
        assertTrue(theme.containsNonDefaultValues(modifiedConfig));
    }

}