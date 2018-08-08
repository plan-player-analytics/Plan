package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.system.info.server.properties.SpongeServerProperties;
import org.spongepowered.api.Sponge;

public class SpongeServerInfo extends BukkitServerInfo {

    public SpongeServerInfo() {
        super(new SpongeServerProperties(Sponge.getGame()));
    }
}
