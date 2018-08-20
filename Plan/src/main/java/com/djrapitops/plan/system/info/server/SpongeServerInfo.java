package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.config.PlanConfig;

import javax.inject.Inject;

public class SpongeServerInfo extends BukkitServerInfo {

    @Inject
    public SpongeServerInfo(ServerProperties serverProperties, ServerInfoFile serverInfoFile, Database database, PlanConfig config) {
        super(serverProperties, serverInfoFile, database, config);
    }
}
