/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plugin.api.Check;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PageHandler for /server and /network pages.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerPageHandler implements PageHandler {

    private final Processing processing;
    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final InfoSystem infoSystem;

    @Inject
    public ServerPageHandler(
            Processing processing,
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            InfoSystem infoSystem
    ) {
        this.processing = processing;
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.infoSystem = infoSystem;
    }

    @Override
    public Response getResponse(Request request, List<String> target) {
        UUID serverUUID = getServerUUID(target);

        boolean raw = target.size() >= 2 && target.get(1).equalsIgnoreCase("raw");
        if (raw) {
            return ResponseCache.loadResponse(PageId.RAW_SERVER.of(serverUUID), () -> responseFactory.rawServerPageResponse(serverUUID));
        }

        Response response = ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));

        if (response != null) {
            return response;
        } else {
            if ((Check.isBungeeAvailable() || Check.isVelocityAvailable()) && serverInfo.getServerUUID().equals(serverUUID)) {
                return ResponseCache.loadResponse(PageId.SERVER.of(serverUUID), responseFactory::networkPageResponse);
            }
            return refreshNow(serverUUID);
        }
    }

    // TODO Split responsibility so that this method does not call system to refresh and also render a refresh page.
    private Response refreshNow(UUID serverUUID) {
        processing.submitNonCritical(() -> {
            try {
                infoSystem.generateAnalysisPage(serverUUID);
            } catch (NoServersException | ConnectionFailException e) {
                ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> responseFactory.notFound404(e.getMessage()));
            } catch (WebException e) {
                ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> responseFactory.internalErrorResponse(e, "Failed to generate Analysis Page"));
            }
        });
        return responseFactory.refreshingAnalysisResponse();
    }

    private UUID getServerUUID(List<String> target) {
        // Default to current server's page
        UUID serverUUID = serverInfo.getServerUUID();

        if (!target.isEmpty()) {
            try {
                String serverName = target.get(0);
                Optional<UUID> serverUUIDOptional = dbSystem.getDatabase().fetch().getServerUUID(serverName);
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
