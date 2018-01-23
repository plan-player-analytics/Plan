/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of player's Plugins Tab to ResponseCache.
 *
 * @author Rsl1122
 */
public class CacheInspectPluginsTabRequest extends InfoRequestWithVariables implements CacheRequest {

    private static final String SPLIT = ";;SPLIT;;";
    private final UUID player;
    private final String navAndHtml;

    private CacheInspectPluginsTabRequest() {
        player = null;
        navAndHtml = null;
    }

    public CacheInspectPluginsTabRequest(UUID player, String nav, String html) {
        Verify.nullCheck(player, nav);
        variables.put("player", player.toString());
        this.player = player;
        this.navAndHtml = nav + SPLIT + html;
    }

    public static CacheInspectPluginsTabRequest createHandler() {
        return new CacheInspectPluginsTabRequest();
    }

    @Override
    public void placeDataToDatabase() throws WebException {
        Verify.nullCheck(player, navAndHtml);

        String encodedHtml = Base64Util.encode(navAndHtml);
        try {
            Database.getActive().transfer().storePlayerPluginsTab(player, ServerInfo.getServerUUID(), encodedHtml);
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, player

        String player = variables.get("player");
        NullCheck.check(player, new BadRequestException("Player UUID 'player' variable not supplied."));

        UUID uuid = UUID.fromString(player);

        try {
            Map<UUID, String> pages = Database.getActive().transfer().getEncodedPlayerPluginsTabs(uuid);

            InspectPagePluginsContent pluginsTab = (InspectPagePluginsContent)
                    ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid), InspectPagePluginsContent::new);

            for (Map.Entry<UUID, String> entry : pages.entrySet()) {
                UUID serverUUID = entry.getKey();
                String[] html = Base64Util.decode(entry.getValue()).split(SPLIT);

                pluginsTab.addTab(serverUUID, html[0], html[1]);
            }
        } catch (DBException e) {
            throw new TransferDatabaseException(e);
        }
        return DefaultResponses.SUCCESS.get();
    }
}