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
package com.djrapitops.plan.storage.file;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.settings.config.PlanConfig;
import dagger.Lazy;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * Implements jar resource fetching with Sponge Asset API.
 *
 * @author AuroraLS3
 */
@Singleton
public class SpongePlanFiles extends PlanFiles {

    private final PlanPlugin plugin;

    @Inject
    public SpongePlanFiles(
            @Named("dataFolder") File dataFolder,
            JarResource.StreamFunction getResourceStream,
            PlanPlugin plugin,
            Lazy<PlanConfig> config
    ) {
        super(dataFolder, getResourceStream, config);
        this.plugin = plugin;
    }

    @Override
    public Resource getResourceFromJar(String resourceName) {
        try {
            PluginContainer container = plugin instanceof PlanSponge ? ((PlanSponge) plugin).getPlugin() : null;
            return new SpongeAssetResource(resourceName, Sponge.assetManager().asset(container, resourceName).orElse(null));
        } catch (IllegalStateException spongeNotEnabled) {
            return super.getResourceFromJar(resourceName);
        }
    }
}
