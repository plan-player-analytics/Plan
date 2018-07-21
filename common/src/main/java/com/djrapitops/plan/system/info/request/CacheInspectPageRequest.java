/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.text.StringSubstitutor;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of a player to ResponseCache.
 *
 * @author Rsl1122
 */
public class CacheInspectPageRequest extends InfoRequestWithVariables implements CacheRequest {

    private final UUID player;
    private final String html;

    private CacheInspectPageRequest() {
        player = null;
        html = null;
    }

    public CacheInspectPageRequest(UUID player, String html) {
        Verify.nullCheck(player, html);
        variables.put("player", player.toString());
        variables.put("html", Base64Util.encode(html));
        this.player = player;
        this.html = html;
    }

    public static CacheInspectPageRequest createHandler() {
        return new CacheInspectPageRequest();
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, player, html (Base64)

        String player = variables.get("player");
        Verify.nullCheck(player, () -> new BadRequestException("Player UUID 'player' variable not supplied in the request."));
        UUID uuid = UUID.fromString(player);

        String html = variables.get("html");
        Verify.nullCheck(html, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        Map<String, String> replace = Collections.singletonMap("networkName", ServerInfo.getServerName());
        boolean export = Settings.ANALYSIS_EXPORT.isTrue();
        cache(export, uuid, StringSubstitutor.replace(Base64Util.decode(html), replace));

        return DefaultResponses.SUCCESS.get();
    }

    private void cache(boolean export, UUID uuid, String html) {
        ResponseCache.cacheResponse(PageId.PLAYER.of(uuid), () -> new InspectPageResponse(uuid, html));
        if (export) {
            Processing.submitNonCritical(() -> HtmlExport.exportPlayer(uuid));
        }
    }

    @Override
    public void runLocally() {
        cache(Settings.ANALYSIS_EXPORT.isTrue(), player, html);
    }
}
