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
package com.djrapitops.pluginbridge.plan.sponge;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * A Class responsible for hooking to Sponge and registering 1 data sources
 *
 * @author BrainStone
 * @since 4.4.6
 */
@Singleton
public class SpongeEconomyHook extends Hook {

    @Inject
    public SpongeEconomyHook(
            ErrorHandler errorHandler
    ) {
        super("org.spongepowered.api.Sponge");

        try {
            Optional<EconomyService> serviceOpt = Sponge.getServiceManager().provide(EconomyService.class);
            enabled = serviceOpt.isPresent();
        } catch(NoClassDefFoundError e) {
            enabled = false;
        } catch (IllegalStateException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @Override
    public void hook(HookHandler handler) {
        if (enabled) {
            handler.addPluginDataSource(new SpongeEconomyData(Sponge.getServiceManager().provide(EconomyService.class).get()));
        }
    }
}
