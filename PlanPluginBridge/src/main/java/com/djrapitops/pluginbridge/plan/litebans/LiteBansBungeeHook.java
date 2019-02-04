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
package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import litebans.api.Database;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A Class responsible for hooking to LiteBans and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class LiteBansBungeeHook extends Hook {

    private Formatter<Long> timestampFormatter;

    @Inject
    public LiteBansBungeeHook(
            Formatters formatters
    ) {
        super();
        try {
            enabled = Database.get() != null;
            timestampFormatter = formatters.secondLong();
        } catch (NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError | Exception e) {
            enabled = false;
        }
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            LiteBansDatabaseQueries db = new LiteBansDatabaseQueries();
            handler.addPluginDataSource(new LiteBansData(db, timestampFormatter));
        }
    }
}
