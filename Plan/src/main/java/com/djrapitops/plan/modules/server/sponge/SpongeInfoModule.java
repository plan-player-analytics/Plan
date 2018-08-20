package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.info.server.properties.SpongeServerProperties;
import dagger.Module;
import dagger.Provides;
import org.spongepowered.api.Sponge;

/**
 * Dagger module for Sponge ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class SpongeInfoModule {

    @Provides
    ServerProperties provideServerProperties() {
        return new SpongeServerProperties(Sponge.getGame());
    }
}