/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.webserver.response.errors.BadRequestResponse;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * PageHandler for /info/requestclassname pages.
 * <p>
 * Used for answering info requests by other servers.
 * <p>
 * requestclassname should be replaced with lowercase version of {@code Class.getSimpleName()}
 *
 * @author Rsl1122
 */
@Singleton
public class InfoRequestPageHandler implements PageHandler {

    private final DBSystem dbSystem;
    private final ConnectionSystem connectionSystem;
    private final ResponseFactory responseFactory;
    private final PluginLogger logger;

    @Inject
    public InfoRequestPageHandler(
            DBSystem dbSystem,
            ConnectionSystem connectionSystem,
            ResponseFactory responseFactory, PluginLogger logger
    ) {
        this.dbSystem = dbSystem;
        this.connectionSystem = connectionSystem;
        this.responseFactory = responseFactory;
        this.logger = logger;
    }

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        int responseCode = 200;

        try {
            if (target.isEmpty()) {
                return responseFactory.pageNotFound404();
            }

            if (!request.getRequestMethod().equals("POST")) {
                return new BadRequestResponse("POST should be used for Info calls.");
            }

            String requestName = target.get(0);
            InfoRequest infoRequest = connectionSystem.getInfoRequest(requestName);

            Verify.nullCheck(infoRequest, () -> new NotFoundException("Info Request has not been registered."));

            logger.debug("ConnectionIn: " + infoRequest.getClass().getSimpleName());
            return new ConnectionIn(request, infoRequest, dbSystem.getDatabase(), connectionSystem).handleRequest();
        } catch (WebException e) {
            responseCode = getResponseCodeFor(e);
            throw e;
        } finally {
            connectionSystem.getConnectionLog().logConnectionFrom(request.getRemoteAddress(), request.getTarget(), responseCode);
        }
    }

    private int getResponseCodeFor(WebException e) {
        return e.getResponseCode().getCode();
    }
}
