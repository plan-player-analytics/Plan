/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.InternalErrorException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.calculation.AnalysisData;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.pages.parsing.AnalysisPage;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest to generate Analysis page HTML at the receiving end.
 *
 * @author Rsl1122
 */
public class GenerateAnalysisPageRequest extends InfoRequestWithVariables implements GenerateRequest {

    private final UUID serverUUID;

    public GenerateAnalysisPageRequest(UUID serverUUID) {
        Verify.nullCheck(serverUUID);
        this.serverUUID = serverUUID;
        variables.put("server", serverUUID.toString());
    }

    private GenerateAnalysisPageRequest() {
        serverUUID = null;
    }

    @Override
    public void placeDataToDatabase() {
        // No data required in a Generate request
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

        if (!Analysis.isAnalysisBeingRun()) {
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
        generateAndCache(serverUUID);
    }

    private String analyseAndGetHtml() throws InternalErrorException {
        try {
            UUID serverUUID = ServerInfo.getServerUUID();
            Database db = Database.getActive();
            DataCache dataCache = DataCache.getInstance();

            AnalysisData analysisData = Analysis.runAnalysisFor(serverUUID, db, dataCache);
            return new AnalysisPage(analysisData).toHtml();
        } catch (DBException e) {
            if (!e.getCause().getMessage().contains("Connection is closed")) {
                Log.toLog(this.getClass(), e);
            }
            throw new InternalErrorException("Analysis failed due to exception", e);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            throw new InternalErrorException("Analysis failed due to exception", e);
        }
    }

    public static GenerateAnalysisPageRequest createHandler() {
        return new GenerateAnalysisPageRequest();
    }

    public UUID getServerUUID() {
        return serverUUID;
    }
}