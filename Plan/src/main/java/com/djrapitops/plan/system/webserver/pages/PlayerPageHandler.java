/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

/**
 * PageHandler for /player/PlayerName pages.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerPageHandler implements PageHandler {

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final InfoSystem infoSystem;
    private final UUIDUtility uuidUtility;

    @Inject
    public PlayerPageHandler(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            InfoSystem infoSystem,
            UUIDUtility uuidUtility
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.infoSystem = infoSystem;
        this.uuidUtility = uuidUtility;
    }

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        if (target.isEmpty()) {
            return responseFactory.pageNotFound404();
        }

        String playerName = target.get(0);
        UUID uuid = uuidUtility.getUUIDOf(playerName);

        boolean raw = target.size() >= 2 && target.get(1).equalsIgnoreCase("raw");

        if (uuid == null) {
            return responseFactory.uuidNotFound404();
        }
        try {
            // TODO Move this Database dependency to PlayerPage generation in PageFactory instead.
            if (dbSystem.getDatabase().check().isPlayerRegistered(uuid)) {
                if (raw) {
                    return ResponseCache.loadResponse(PageId.RAW_PLAYER.of(uuid), () -> responseFactory.rawPlayerPageResponse(uuid));
                }
                return playerResponseOrNotFound(uuid);
            } else {
                return responseFactory.playerNotFound404();
            }
        } catch (NoServersException e) {
            ResponseCache.loadResponse(PageId.PLAYER.of(uuid), () -> responseFactory.notFound404(e.getMessage()));
        }
        return responseFactory.serverNotFound404();
    }

    private Response playerResponseOrNotFound(UUID uuid) throws WebException {
        Response response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
        if (!(response instanceof InspectPageResponse)) {
            infoSystem.generateAndCachePlayerPage(uuid);
            response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
        }
        return response != null ? response : responseFactory.serverNotFound404();
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) throws WebUserAuthException {
        WebUser webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 1 || webUser.getName().equalsIgnoreCase(target.get(target.size() - 1));
    }
}
