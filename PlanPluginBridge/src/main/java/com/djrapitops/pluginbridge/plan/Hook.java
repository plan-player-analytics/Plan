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
import com.djrapitops.plugin.api.Check;

/**
 * Abstract class for easy hooking of plugins.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
public abstract class Hook {

    /**
     * Is the plugin being hooked properly enabled?
     */
    protected boolean enabled;

    /**
     * Class constructor.
     * <p>
     * Checks if the given plugin (class path) is enabled.
     *
     * @param pluginClass Class path string of the plugin's main JavaPlugin class.
     */
    public Hook(String pluginClass) {
        enabled = Check.isAvailable(pluginClass);
    }

    public abstract void hook(HookHandler handler) throws NoClassDefFoundError;

    /**
     * Constructor to set enabled to false.
     */
    public Hook() {
        enabled = false;
    }
}
