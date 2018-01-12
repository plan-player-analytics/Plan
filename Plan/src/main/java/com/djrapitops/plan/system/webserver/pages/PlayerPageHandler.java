/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.util.List;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlayerPageHandler extends PageHandler {

    public PlayerPageHandler() {
        permission = "special_player";
    }

    @Override
    public Response getResponse(Request request, List<String> target) {
        if (target.isEmpty()) {
            return DefaultResponses.NOT_FOUND.get();
        }

        String playerName = target.get(0);
        UUID uuid = UUIDUtility.getUUIDOf(playerName);

        if (uuid == null) {
            return notFound("Player has no UUID");
        }

        if (PlanPlugin.getInstance().getDB().wasSeenBefore(uuid)) {
            PlanPlugin.getInstance().getInfoManager().cachePlayer(uuid);
            Response response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
            // TODO Create a new method that places NotFoundResponse to ResponseCache instead.
            if (response == null || response.getContent().contains("No Bukkit Servers were online to process this request")) {
                ResponseCache.cacheResponse(PageId.PLAYER.of(uuid), () -> {
                    try {
                        return new InspectPageResponse(PlanPlugin.getInstance().getInfoManager(), uuid);
                    } catch (ParseException e) {
                        return new InternalErrorResponse(e, this.getClass().getName());
                    }
                });
                response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
            }
            return response;
        }
        return notFound("Player has not played on this server.");
    }

    private Response notFound(String error) {
        return ResponseCache.loadResponse(PageId.NOT_FOUND.of(error), () -> new NotFoundResponse(error));
    }
}