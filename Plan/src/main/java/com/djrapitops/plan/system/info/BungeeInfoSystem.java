/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.CacheRequest;
import com.djrapitops.plan.system.info.request.GenerateInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.pages.NetworkPageResponse;

import javax.inject.Inject;

/**
 * InfoSystem for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeInfoSystem extends InfoSystem {

    private final ServerInfo serverInfo;

    @Inject
    public BungeeInfoSystem(ConnectionSystem connectionSystem, ServerInfo serverInfo) {
        super(connectionSystem);

        this.serverInfo = serverInfo;
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        if (infoRequest instanceof CacheRequest ||
                infoRequest instanceof GenerateInspectPageRequest) {
            infoRequest.runLocally();
        } else {
            // runLocally is called when ConnectionSystem has no servers.
            throw new NoServersException("No servers were available to process this request (Local attempt): " + infoRequest.getClass().getSimpleName());
        }
    }

    @Override
    public void updateNetworkPage() {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverInfo.getServerUUID()), () -> {
            try {
                return new NetworkPageResponse();
            } catch (ParseException e) {
                return new InternalErrorResponse("Network page parsing failed.", e);
            }
        });
    }
}
