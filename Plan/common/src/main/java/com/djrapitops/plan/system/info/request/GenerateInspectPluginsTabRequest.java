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

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page plugins tab on receiving WebServer.
 *
 * @deprecated Marked for removal when the connection system will be removed.
 * @author Rsl1122
 */
@Deprecated
public class GenerateInspectPluginsTabRequest extends InfoRequestWithVariables implements GenerateRequest, WideRequest {

    private final InfoSystem infoSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final PageFactory pageFactory;

    private UUID playerUUID;

    GenerateInspectPluginsTabRequest(
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory,
            PageFactory pageFactory
    ) {
        this.infoSystem = infoSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.pageFactory = pageFactory;
    }

    GenerateInspectPluginsTabRequest(
            UUID uuid,
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory,
            PageFactory pageFactory
    ) {
        this(infoSystem, infoRequestFactory, pageFactory);
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
        String[] navAndHtml = pageFactory.inspectPagePluginsContent(uuid).getContents();
        infoSystem.sendRequest(infoRequestFactory.cacheInspectPluginsTabRequest(uuid, navAndHtml[0], navAndHtml[1]));
    }

    @Override
    public void runLocally() throws WebException {
        generateAndCache(playerUUID);
    }
}
