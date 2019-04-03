package com.djrapitops.plan.system.file;

import com.djrapitops.plan.PlanPlugin;
import org.spongepowered.api.Sponge;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements jar resource fetching with Sponge Asset API.
 *
 * @author Rsl1122
 */
@Singleton
public class SpongePlanFiles extends PlanFiles {

    @Inject
    public SpongePlanFiles(PlanPlugin plugin) {
        super(plugin);
    }

    @Override
    public Resource getResourceFromJar(String resourceName) {
        try {
            return new SpongeAssetResource(resourceName, Sponge.getAssetManager().getAsset(plugin, resourceName).orElse(null));
        } catch (IllegalStateException spongeNotEnabled) {
            return super.getResourceFromJar(resourceName);
        }
    }
}