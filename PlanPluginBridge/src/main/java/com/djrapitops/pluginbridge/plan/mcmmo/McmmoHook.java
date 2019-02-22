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
package com.djrapitops.pluginbridge.plan.mcmmo;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to MCMMO and registering data sources.
 *
 * @author Rsl1122

 */
@Singleton
public class McmmoHook extends Hook {

    private final Formatter<Double> decimalFormatter;

    @Inject
    public McmmoHook(
            Formatters formatters
    ) {
        super("com.gmail.nossr50.mcMMO");
        decimalFormatter = formatters.decimals();
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            handler.addPluginDataSource(new McMmoData(decimalFormatter));
        }
    }
}
