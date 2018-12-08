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
package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

/**
 * Manages connection to other plugins.
 *
 * @author Rsl1122
 */
public abstract class AbstractBridge implements Bridge {

    private final PlanConfig config;
    private final ErrorHandler errorHandler;

    AbstractBridge(
            PlanConfig config,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.errorHandler = errorHandler;
    }

    public void hook(HookHandler handler) {
        Hook[] hooks = getHooks();
        hookInto(handler, hooks);
    }

    private void hookInto(HookHandler handler, Hook[] hooks) {
        boolean devMode = config.isTrue(PluginSettings.DEV_MODE);
        for (Hook hook : hooks) {
            try {
                hook.hook(handler);
            } catch (Exception | NoClassDefFoundError e) {
                if (devMode) {
                    errorHandler.log(L.WARN, this.getClass(), e);
                }
            }
        }
    }

    abstract Hook[] getHooks();

}
