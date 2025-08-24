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
import com.djrapitops.plan.delivery.web.AssetVersions;
import dagger.Lazy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.plugin.PluginContainer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;

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
            Lazy<AssetVersions> assetVersions
    ) {
        super(dataFolder, getResourceStream, assetVersions);
        this.plugin = plugin;
    }

    @Override
    public Resource getResourceFromJar(String resourceName) {
        try {
            if (!(plugin instanceof PlanSponge)) {
                throw new IllegalStateException("Not a Sponge plugin");
            }
            PluginContainer container = ((PlanSponge) plugin).getPlugin();
            ResourcePath resourcePath = ResourcePath.of(container, resourceName);
            org.spongepowered.api.resource.Resource resource = Sponge.server().resourceManager().load(resourcePath);

            return asStringResource(resource, resourceName);
        } catch (IllegalStateException | IOException ignored) {
            return super.getResourceFromJar(resourceName);
        }
    }

    private Resource asStringResource(org.spongepowered.api.resource.Resource resource, String resourceName) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.inputStream(), StandardCharsets.UTF_8));
                StringWriter writer = new StringWriter()
        ) {
            reader.transferTo(writer);
            return new StringResource(resourceName, writer.toString(), getLastModifiedForJarResource(resourceName));
        }
    }
}
