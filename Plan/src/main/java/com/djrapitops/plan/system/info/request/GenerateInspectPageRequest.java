/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.pages.parsing.InspectPage;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page on receiving WebServer.
 *
 * @author Rsl1122
 */
public class GenerateInspectPageRequest extends InfoRequestWithVariables implements GenerateRequest {

    private final UUID playerUUID;

    private GenerateInspectPageRequest() {
        playerUUID = null;
    }

    public GenerateInspectPageRequest(UUID uuid) {
        Verify.nullCheck(uuid);
        playerUUID = uuid;
        variables.put("player", uuid.toString());
    }

    public static GenerateInspectPageRequest createHandler() {
        return new GenerateInspectPageRequest();
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
            InfoSystem.getInstance().getConnectionSystem().sendWideInfoRequest(new GenerateInspectPluginsTabRequest(uuid));
        } catch (NotFoundException e) {
            html = new NotFoundResponse(e.getMessage()).getContent();
        }
        InfoSystem.getInstance().sendRequest(new CacheInspectPageRequest(uuid, html));
    }

    @Override
    public void runLocally() throws WebException {
        generateAndCache(playerUUID);
    }

    private String getHtml(UUID uuid) throws WebException {
        try {

            return new InspectPage(uuid).toHtml();

        } catch (ParseException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DBException) {
                throw new TransferDatabaseException((DBException) cause);
            } else if (cause instanceof IllegalStateException && "Player profile was null!".equals(cause.getMessage())) {
                throw new NotFoundException("Player has not played on this server.");
            } else {
                throw new WebFailException("Exception during HTML Parsing", cause);
            }
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
