/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.NullCheck;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page on receiving WebServer.
 *
 * @author Rsl1122
 */
public class GenerateInspectPageRequest extends InfoRequestWithVariables {

    public GenerateInspectPageRequest(UUID uuid) {
        variables.put("player", uuid.toString());
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

        // TODO Generate HTML

        // TODO InfoSystem.getInstance().sendRequest(new CacheInspectPageRequest(uuid, html));

        return DefaultResponses.SUCCESS.get();
    }
}