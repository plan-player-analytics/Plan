/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page plugins tab on receiving WebServer.
 *
 * @author Rsl1122
 */
public class GenerateInspectPluginsTabRequest extends InfoRequestWithVariables implements GenerateRequest {

    private final UUID playerUUID;

    private GenerateInspectPluginsTabRequest() {
        playerUUID = null;
    }

    public GenerateInspectPluginsTabRequest(UUID uuid) {
        Verify.nullCheck(uuid);
        playerUUID = uuid;
        variables.put("player", uuid.toString());
    }

    public static GenerateInspectPluginsTabRequest createHandler() {
        return new GenerateInspectPluginsTabRequest();
    }

    @Override
    public void placeDataToDatabase() {
        // No data required in a Generate request
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, player

        String player = variables.get("player");
        NullCheck.check(player, new BadRequestException("Player UUID 'player' variable not supplied."));

        UUID uuid = UUID.fromString(player);
        String[] navAndhtml = getNavAndHtml(uuid);

        InfoSystem.getInstance().sendRequest(new CacheInspectPluginsTabRequest(uuid, navAndhtml[0], navAndhtml[1]));

        return DefaultResponses.SUCCESS.get();
    }

    private String[] getNavAndHtml(UUID uuid) {
        return ((InspectPagePluginsContent) ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid),
                InspectPagePluginsContent::new)).getContents();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}