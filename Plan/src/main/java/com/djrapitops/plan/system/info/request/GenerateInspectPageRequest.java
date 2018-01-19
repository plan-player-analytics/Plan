/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.TransferDatabaseException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.connection.WebFailException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.pages.parsing.InspectPage;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest for Generating Inspect page on receiving WebServer.
 *
 * @author Rsl1122
 */
public class GenerateInspectPageRequest extends InfoRequestWithVariables {

    private GenerateInspectPageRequest() {
    }

    public GenerateInspectPageRequest(UUID uuid) {
        Verify.nullCheck(uuid);
        variables.put("player", uuid.toString());
    }

    public static GenerateInspectPageRequest createHandler() {
        return new GenerateInspectPageRequest();
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
        String html = getHtml(uuid);

        InfoSystem.getInstance().sendRequest(new CacheInspectPageRequest(uuid, html));

        return DefaultResponses.SUCCESS.get();
    }

    private String getHtml(UUID uuid) throws WebException {
        try {

            return new InspectPage(uuid).toHtml();

        } catch (ParseException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DBException) {
                throw new TransferDatabaseException((DBException) cause);
            } else {
                throw new WebFailException("Exception during HTML Parsing", cause);
            }
        }
    }
}