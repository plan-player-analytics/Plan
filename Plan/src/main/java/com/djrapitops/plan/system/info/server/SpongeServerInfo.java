package com.djrapitops.plan.system.info.server;

import org.spongepowered.api.Sponge;

public class SpongeServerInfo extends BukkitServerInfo {

    public SpongeServerInfo() {
        super(new ServerProperties(Sponge.getGame()));
    }
}
