package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.SpongeServerInfo;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.info.server.properties.SpongeServerProperties;
import dagger.Module;
import dagger.Provides;
import org.spongepowered.api.Sponge;

import javax.inject.Singleton;

/**
 * Dagger module for Sponge ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class SpongeInfoModule {

    @Provides
    @Singleton
    ServerInfo provideSpongeServerInfo(SpongeServerInfo spongeServerInfo) {
        return spongeServerInfo;
    }

    @Provides
    @Singleton
    ServerProperties provideServerProperties() {
        return new SpongeServerProperties(Sponge.getGame());
    }
}