/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;

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
        return ResponseCache.loadResponse(PageId.SERVER.of(serverUUID),
                () -> new AnalysisPageResponse(PlanPlugin.getInstance().getInfoManager())
        );
    }

    private UUID getServerUUID(List<String> target) {
        UUID serverUUID = PlanPlugin.getInstance().getServerUuid();
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