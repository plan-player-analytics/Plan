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
package com.djrapitops.pluginbridge.plan.essentials;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import com.earth2me.essentials.Essentials;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to Essentials.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class EssentialsHook extends Hook {

    @Inject
    public EssentialsHook() {
        super("com.earth2me.essentials.Essentials");
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            Essentials ess = getPlugin(Essentials.class);
            handler.addPluginDataSource(new EssentialsData(ess));
        }
    }
}
