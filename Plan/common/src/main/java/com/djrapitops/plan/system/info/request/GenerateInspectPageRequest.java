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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page on receiving WebServer.
 *
 * @author Rsl1122
 */
public class GenerateInspectPageRequest extends InfoRequestWithVariables implements GenerateRequest {

    private final InfoRequestFactory infoRequestFactory;
    private final ResponseFactory responseFactory;
    private final PageFactory pageFactory;
    private final InfoSystem infoSystem;

    private UUID playerUUID;

    GenerateInspectPageRequest(
            InfoRequestFactory infoRequestFactory,
            ResponseFactory responseFactory, PageFactory pageFactory,
            InfoSystem infoSystem
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.responseFactory = responseFactory;
        this.pageFactory = pageFactory;
        this.infoSystem = infoSystem;
    }

    GenerateInspectPageRequest(
            UUID uuid,
            InfoRequestFactory infoRequestFactory,
            ResponseFactory responseFactory, PageFactory pageFactory,
            InfoSystem infoSystem
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.responseFactory = responseFactory;
        this.pageFactory = pageFactory;
        this.infoSystem = infoSystem;

        Verify.nullCheck(uuid);
        playerUUID = uuid;
        variables.put("player", uuid.toString());
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, player

        String player = variables.get("player");
        Verify.nullCheck(player, () -> new BadRequestException("Player UUID 'player' variable not supplied in the request."));

        UUID uuid = UUID.fromString(player);

        generateAndCache(uuid);

        return DefaultResponses.SUCCESS.get();
    }

    private void generateAndCache(UUID uuid) throws WebException {
        String html;
        try {
            html = getHtml(uuid);
            infoSystem.getConnectionSystem().sendWideInfoRequest(infoRequestFactory.generateInspectPluginsTabRequest(uuid));
        } catch (NotFoundException e) {
            html = responseFactory.notFound404(e.getMessage()).getContent();
        }
        infoSystem.sendRequest(infoRequestFactory.cacheInspectPageRequest(uuid, html));
    }

    @Override
    public void runLocally() throws WebException {
        generateAndCache(playerUUID);
    }

    private String getHtml(UUID uuid) throws WebException {
        try {

            return pageFactory.inspectPage(uuid).toHtml();

        } catch (ParseException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DBOpException) {
                throw new TransferDatabaseException((DBOpException) cause);
            } else if (cause instanceof IllegalStateException && "Player profile was null!".equals(cause.getMessage())) {
                throw new NotFoundException("Player has not played on this server.");
            } else {
                throw new WebFailException("Exception during HTML Parsing", cause);
            }
        }
    }
}
