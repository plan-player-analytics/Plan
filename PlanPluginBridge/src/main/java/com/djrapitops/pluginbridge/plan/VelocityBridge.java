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

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for Velocity plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class VelocityBridge extends Bridge {

    private final BuyCraftHook buyCraftHook;

    @Inject
    public VelocityBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            BuyCraftHook buyCraftHook
    ) {
        super(config, errorHandler);
        this.buyCraftHook = buyCraftHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                buyCraftHook
        };
    }
}