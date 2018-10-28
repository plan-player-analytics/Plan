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
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for BuyCraft plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class BuyCraftHook extends Hook {

    private final String secret;
    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public BuyCraftHook(
            PlanConfig config,
            Formatters formatters
    ) {
        super();
        this.config = config;
        this.formatters = formatters;

        secret = config.getString(Settings.PLUGIN_BUYCRAFT_SECRET);
        enabled = !secret.equals("-") && !secret.isEmpty();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new BuyCraftData(secret, config, formatters.yearLong(), formatters.decimals()));
        }
    }
}