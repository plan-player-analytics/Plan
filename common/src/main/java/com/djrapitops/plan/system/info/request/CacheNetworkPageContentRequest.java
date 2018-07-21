/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
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

    private final String html;

    public CacheNetworkPageContentRequest(UUID serverUUID, String html) {
        Verify.nullCheck(serverUUID, html);
        variables.put("serverName", ServerInfo.getServerName());
        variables.put("html", Base64Util.encode(html));
        this.html = html;
    }

    private CacheNetworkPageContentRequest() {
        html = null;
    }

    public static CacheNetworkPageContentRequest createHandler() {
        return new CacheNetworkPageContentRequest();
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

        InfoSystem.getInstance().updateNetworkPage();

        return DefaultResponses.SUCCESS.get();
    }

    private NetworkPageContent getNetworkPageContent() {
        return (NetworkPageContent) ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
    }

    @Override
    public void runLocally() {
        getNetworkPageContent().addElement(ServerInfo.getServerName(), html);
    }
}
