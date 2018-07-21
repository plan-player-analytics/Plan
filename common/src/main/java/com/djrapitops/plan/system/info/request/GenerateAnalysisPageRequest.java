/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.InternalErrorException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.pages.parsing.AnalysisPage;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.api.utility.log.Log;
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

    private final UUID serverUUID;
    private boolean runningAnalysis = false;

    public GenerateAnalysisPageRequest(UUID serverUUID) {
        Verify.nullCheck(serverUUID);
        this.serverUUID = serverUUID;
        variables.put("server", serverUUID.toString());
    }

    private GenerateAnalysisPageRequest() {
        serverUUID = null;
    }

    public static GenerateAnalysisPageRequest createHandler() {
        return new GenerateAnalysisPageRequest();
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Variables available: sender, server

        String server = variables.get("server");
        Verify.nullCheck(server, () -> new BadRequestException("Server UUID 'server' variable not supplied in the request."));

        UUID serverUUID = UUID.fromString(server);
        if (!ServerInfo.getServerUUID().equals(serverUUID)) {
            throw new BadRequestException("Requested Analysis page from wrong server.");
        }

        if (!runningAnalysis) {
            generateAndCache(serverUUID);
        }

        return DefaultResponses.SUCCESS.get();
    }

    private void generateAndCache(UUID serverUUID) throws WebException {
        InfoSystem infoSystem = InfoSystem.getInstance();
        infoSystem.sendRequest(new CacheAnalysisPageRequest(serverUUID, analyseAndGetHtml()));
        infoSystem.updateNetworkPage();
    }

    @Override
    public void runLocally() throws WebException {
        // Get the handler from ConnectionSystem and run the request.
        InfoSystem.getInstance().getConnectionSystem()
                .getInfoRequest(this.getClass().getSimpleName())
                .handleRequest(Collections.singletonMap("server", serverUUID.toString()));
    }

    private String analyseAndGetHtml() throws InternalErrorException {
        try {
            runningAnalysis = true;
            UUID serverUUID = ServerInfo.getServerUUID();
            Database db = Database.getActive();

            AnalysisContainer analysisContainer = new AnalysisContainer(db.fetch().getServerContainer(serverUUID));
            return new AnalysisPage(analysisContainer).toHtml();
        } catch (DBOpException e) {
            if (!e.getCause().getMessage().contains("Connection is closed")) {
                Log.toLog(this.getClass(), e);
            }
            throw new InternalErrorException("Analysis failed due to exception", e);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            throw new InternalErrorException("Analysis failed due to exception", e);
        } finally {
            runningAnalysis = false;
        }
    }

    public UUID getServerUUID() {
        return serverUUID;
    }
}
