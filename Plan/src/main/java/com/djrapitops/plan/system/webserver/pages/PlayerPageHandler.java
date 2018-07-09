/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.util.List;
import java.util.UUID;

/**
 * PageHandler for /player/PlayerName pages.
 *
 * @author Rsl1122
 */
public class PlayerPageHandler extends PageHandler {

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        if (target.isEmpty()) {
            return DefaultResponses.NOT_FOUND.get();
        }

        String playerName = target.get(0);
        UUID uuid = UUIDUtility.getUUIDOf(playerName);

        if (uuid == null) {
            return notFound("Player UUID was not found in the database.");
        }
        try {
            if (Database.getActive().check().isPlayerRegistered(uuid)) {
                Response response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
                if (!(response instanceof InspectPageResponse)) {
                    InfoSystem.getInstance().generateAndCachePlayerPage(uuid);
                    response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
                }
                return response != null ? response : notFound("No Bukkit servers online to perform the request.");
            } else {
                return notFound("Player has not played on this server.");
            }
        } catch (NoServersException e) {
            ResponseCache.loadResponse(PageId.PLAYER.of(uuid), () -> new NotFoundResponse(e.getMessage()));
        }
        return InspectPageResponse.getRefreshing();
    }

    private Response notFound(String error) {
        return ResponseCache.loadResponse(PageId.NOT_FOUND.of(error), () -> new NotFoundResponse(error));
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
        WebUser webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 1 || webUser.getName().equalsIgnoreCase(target.get(target.size() - 1));
    }
}
