package com.djrapitops.plan.system.info.server;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.WebServer;
import dagger.Lazy;

import javax.inject.Inject;

public class SpongeServerInfo extends BukkitServerInfo {

    @Inject
    public SpongeServerInfo(
            ServerProperties serverProperties,
            ServerInfoFile serverInfoFile,
            DBSystem dbSystem,
            Lazy<WebServer> webServer,
            PlanConfig config
    ) {
        super(serverProperties, serverInfoFile, dbSystem, webServer, config);
    }
}
