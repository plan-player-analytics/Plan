/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plugin.api.Check;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PageHandler for /server and /network pages.
 *
 * @author Rsl1122
 */
public class ServerPageHandler extends PageHandler {

    @Override
    public Response getResponse(Request request, List<String> target) {
        UUID serverUUID = getServerUUID(target);
        Response response = ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));

        if (response != null) {
            return response;
        } else {
            if (Check.isBungeeAvailable() && ServerInfo.getServerUUID().equals(serverUUID)) {
                try {
                    InfoSystem.getInstance().updateNetworkPage();
                } catch (WebException e) {
                    /*Ignore, should not occur*/
                }
                return ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));
            }
            return AnalysisPageResponse.refreshNow(serverUUID);
        }
    }

    private UUID getServerUUID(List<String> target) {
        UUID serverUUID = ServerInfo.getServerUUID();
        if (!target.isEmpty()) {
            try {
                String serverName = target.get(0).replace("%20", " ");
                Optional<UUID> serverUUIDOptional = Database.getActive().fetch().getServerUUID(serverName);
                if (serverUUIDOptional.isPresent()) {
                    serverUUID = serverUUIDOptional.get();
                }
            } catch (IllegalArgumentException ignore) {
                /*ignored*/
            }
        }
        return serverUUID;
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}
