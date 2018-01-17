/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.List;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlayerPageHandler extends PageHandler {

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

        try {
            if (Database.getActive().check().isPlayerRegistered(uuid)) {
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
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return notFound("Player has not played on this server.");
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