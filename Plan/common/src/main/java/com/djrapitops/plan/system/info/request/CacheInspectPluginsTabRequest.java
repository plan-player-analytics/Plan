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
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of player's Plugins Tab to ResponseCache.
 *
 * @author Rsl1122
 */
@Deprecated
public class CacheInspectPluginsTabRequest extends InfoRequestWithVariables implements CacheRequest {

    private final ServerInfo serverInfo;

    private UUID player;
    private String html;

    CacheInspectPluginsTabRequest(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    CacheInspectPluginsTabRequest(UUID player, String nav, String html, ServerInfo serverInfo) {
        this.serverInfo = serverInfo;

        Verify.nullCheck(player, nav);
        variables.put("player", player.toString());
        variables.put("nav", nav);
        variables.put("html", Base64Util.encode(html));
        this.player = player;
        this.html = html;
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, player, nav, html

        String player = variables.get("player");
        Verify.nullCheck(player, () -> new BadRequestException("Player UUID 'player' variable not supplied in the request."));
        UUID uuid = UUID.fromString(player);
        UUID serverUUID = UUID.fromString(variables.get("sender"));

        String nav = variables.get("nav");
        String html = variables.get("html");
        Verify.nullCheck(nav, () -> new BadRequestException("Nav HTML 'nav' variable not supplied in the request"));
        Verify.nullCheck(html, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        InspectPagePluginsContent pluginsTab = getPluginsTab(uuid);

        pluginsTab.addTab(nav, Base64Util.decode(html));
        return DefaultResponses.SUCCESS.get();
    }

    private InspectPagePluginsContent getPluginsTab(UUID uuid) {
        return (InspectPagePluginsContent) ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid), InspectPagePluginsContent::new);
    }

    @Override
    public void runLocally() {
        getPluginsTab(player).addTab(variables.get("nav"), html);
    }
}
