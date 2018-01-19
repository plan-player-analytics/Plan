/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.pages.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.NullCheck;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest to generate Analysis page HTML at the receiving end.
 *
 * @author Rsl1122
 */
public class GenerateAnalysisPageRequest extends InfoRequestWithVariables {

    public GenerateAnalysisPageRequest(UUID serverUUID) {
        variables.put("server", serverUUID.toString());
    }

    @Override
    public void placeDataToDatabase() {
        // No data required in a Generate request
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Variables available: sender, server

        String server = variables.get("server");
        NullCheck.check(server, new BadRequestException("Server UUID 'server' variable not supplied."));

        UUID serverUUID = UUID.fromString(server);
        if (!ServerInfo.getServerUUID().equals(serverUUID)) {
            throw new BadRequestException("Requested Analysis page from wrong server.");
        }
        String html = getHtml();

        InfoSystem.getInstance().sendRequest(new CacheAnalysisPageRequest(serverUUID, html));

        return DefaultResponses.SUCCESS.get();
    }

    public String getHtml() {
        // TODO Perform Analysis & get HTML
        return null;
    }
}