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
package com.djrapitops.pluginbridge.plan.banmanager;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for BanManager plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class BanManagerHook extends Hook {

    private final Formatter<Long> timestampFormatter;

    @Inject
    public BanManagerHook(Formatters formatters) {
        super("me.confuser.banmanager.BanManager");
        timestampFormatter = formatters.yearLong();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }

        handler.addPluginDataSource(new BanManagerData(timestampFormatter));
    }
}