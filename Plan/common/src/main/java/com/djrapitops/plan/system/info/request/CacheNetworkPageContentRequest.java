/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
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
import com.djrapitops.plan.system.webserver.response.pages.parts.NetworkPageContent;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for caching Network page parts to ResponseCache of receiving server.
 * <p>
 * SHOULD NOT BE SENT TO BUKKIT.
 *
 * @author Rsl1122
 */
public class CacheNetworkPageContentRequest extends InfoRequestWithVariables implements CacheRequest {

    private final ServerInfo serverInfo;

    private String html;

    CacheNetworkPageContentRequest(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    CacheNetworkPageContentRequest(UUID serverUUID, String html, ServerInfo serverInfo) {
        this.serverInfo = serverInfo;

        Verify.nullCheck(serverUUID, html);
        variables.put("serverName", serverInfo.getServer().getName());
        variables.put("html", Base64Util.encode(html));
        this.html = html;
    }



    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, serverName, html (Base64)

        String serverName = variables.get("serverName");
        Verify.nullCheck(serverName, () -> new BadRequestException("Server name 'serverName' variable not supplied in the request"));
        String html = variables.get("html");
        Verify.nullCheck(html, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        NetworkPageContent serversTab = getNetworkPageContent();
        serversTab.addElement(serverName, Base64Util.decode(html));

        ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));

        return DefaultResponses.SUCCESS.get();
    }

    private NetworkPageContent getNetworkPageContent() {
        return (NetworkPageContent) ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
    }

    @Override
    public void runLocally() {
        getNetworkPageContent().addElement(serverInfo.getServer().getName(), html);
    }

}
