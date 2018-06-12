/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
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
public class CacheInspectPluginsTabRequest extends InfoRequestWithVariables implements CacheRequest {

    private final UUID player;
    private final String html;

    private CacheInspectPluginsTabRequest() {
        player = null;
        html = null;
    }

    public CacheInspectPluginsTabRequest(UUID player, String nav, String html) {
        Verify.nullCheck(player, nav);
        variables.put("player", player.toString());
        variables.put("nav", nav);
        variables.put("html", Base64Util.encode(html));
        this.player = player;
        this.html = html;
    }

    public static CacheInspectPluginsTabRequest createHandler() {
        return new CacheInspectPluginsTabRequest();
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

        pluginsTab.addTab(serverUUID, nav, Base64Util.decode(html));
        return DefaultResponses.SUCCESS.get();
    }

    private InspectPagePluginsContent getPluginsTab(UUID uuid) {
        return (InspectPagePluginsContent) ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid), InspectPagePluginsContent::new);
    }

    @Override
    public void runLocally() {
        getPluginsTab(player).addTab(ServerInfo.getServerUUID(), variables.get("nav"), html);
    }
}
