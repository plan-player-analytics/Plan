/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.InternalErrorException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest to generate Analysis page HTML at the receiving end.
 *
 * @author Rsl1122
 */
public class GenerateAnalysisPageRequest extends InfoRequestWithVariables implements GenerateRequest {

    private final InfoRequestFactory infoRequestFactory;
    private final ServerInfo serverInfo;
    private final InfoSystem infoSystem;
    private final PageFactory pageFactory;

    private boolean runningAnalysis = false;
    private UUID serverUUID;

    GenerateAnalysisPageRequest(
            InfoRequestFactory infoRequestFactory,
            ServerInfo serverInfo,
            InfoSystem infoSystem,
            PageFactory pageFactory
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
        this.infoSystem = infoSystem;
        this.pageFactory = pageFactory;
    }

    GenerateAnalysisPageRequest(
            UUID serverUUID,
            InfoRequestFactory infoRequestFactory,
            ServerInfo serverInfo,
            InfoSystem infoSystem,
            PageFactory pageFactory
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
        this.infoSystem = infoSystem;
        this.pageFactory = pageFactory;

        Verify.nullCheck(serverUUID);
        this.serverUUID = serverUUID;
        variables.put("server", serverUUID.toString());
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Variables available: sender, server

        String server = variables.get("server");
        Verify.nullCheck(server, () -> new BadRequestException("Server UUID 'server' variable not supplied in the request."));

        UUID serverUUID = UUID.fromString(server);
        if (!serverInfo.getServerUUID().equals(serverUUID)) {
            throw new BadRequestException("Requested Analysis page from wrong server.");
        }

        if (!runningAnalysis) {
            generateAndCache(serverUUID);
        }

        return DefaultResponses.SUCCESS.get();
    }

    private void generateAndCache(UUID serverUUID) throws WebException {
        infoSystem.sendRequest(infoRequestFactory.cacheAnalysisPageRequest(serverUUID, analyseAndGetHtml()));
        infoSystem.updateNetworkPage();
    }

    @Override
    public void runLocally() throws WebException {
        // Get the handler from ConnectionSystem and run the request.
        // This is done to keep the concurrent analysis in check with runningAnalysis variable.
        infoSystem.getConnectionSystem()
                .getInfoRequest(this.getClass().getSimpleName())
                .handleRequest(Collections.singletonMap("server", serverUUID.toString()));
    }

    private String analyseAndGetHtml() throws InternalErrorException {
        try {
            runningAnalysis = true;
            UUID serverUUID = serverInfo.getServerUUID();
            return pageFactory.analysisPage(serverUUID).toHtml();
        } catch (Exception e) {
            throw new InternalErrorException("Analysis failed due to exception", e);
        } finally {
            runningAnalysis = false;
        }
    }

    public UUID getServerUUID() {
        return serverUUID;
    }
}
