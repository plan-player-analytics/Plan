/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.CacheNetworkPageContentRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.SetupRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plugin.api.utility.log.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * InfoSystem for Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerInfoSystem extends InfoSystem {

    private final ServerInfo serverInfo;

    @Inject
    public ServerInfoSystem(ConnectionSystem connectionSystem, ServerInfo serverInfo) {
        super(connectionSystem);
        this.serverInfo = serverInfo;
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        if (infoRequest instanceof SetupRequest) {
            throw new NoServersException("Set-up requests can not be run locally.");
        }
        Log.debug("LocalRun: " + infoRequest.getClass().getSimpleName());
        infoRequest.runLocally();
    }

    @Override
    public void updateNetworkPage() throws WebException {
        String html = HtmlStructure.createServerContainer();
        sendRequest(new CacheNetworkPageContentRequest(serverInfo.getServerUUID(), html));
    }
}
