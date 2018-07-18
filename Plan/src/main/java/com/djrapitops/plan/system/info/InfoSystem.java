/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.GenerateInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.SendDBSettingsRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;

/**
 * Information management system.
 * <p>
 * Subclasses should decide how InfoRequests are run locally if necessary.
 * <p>
 * Everything should be called from an Async thread.
 *
 * @author Rsl1122
 */
public abstract class InfoSystem implements SubSystem {

    protected final ConnectionSystem connectionSystem;

    protected InfoSystem(ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
    }

    public static InfoSystem getInstance() {
        InfoSystem infoSystem = PlanSystem.getInstance().getInfoSystem();
        Verify.nullCheck(infoSystem, () -> new IllegalStateException("Info System was not initialized."));
        return infoSystem;
    }

    /**
     * Refreshes Player page.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param player UUID of the player.
     * @throws WebException If fails.
     */
    public void generateAndCachePlayerPage(UUID player) throws WebException {
        GenerateInspectPageRequest infoRequest = new GenerateInspectPageRequest(player);
        try {
            sendRequest(infoRequest);
        } catch (ConnectionFailException e) {
            runLocally(infoRequest);
        }
    }

    /**
     * Refreshes Analysis page.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param serverUUID UUID of the server to analyze
     * @throws WebException If fails.
     */
    public void generateAnalysisPage(UUID serverUUID) throws WebException {
        GenerateAnalysisPageRequest request = new GenerateAnalysisPageRequest(serverUUID);
        if (ServerInfo.getServerUUID().equals(serverUUID)) {
            runLocally(request);
        } else {
            sendRequest(request);
        }
    }

    /**
     * Send an InfoRequest to another server or run locally if necessary.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param infoRequest InfoRequest to send or run.
     * @throws WebException If fails.
     */
    public void sendRequest(InfoRequest infoRequest) throws WebException {
        try {
            if (!connectionSystem.isServerAvailable()) {
                Log.debug("Main server unavailable, running locally.");
                runLocally(infoRequest);
                return;
            }
            connectionSystem.sendInfoRequest(infoRequest);
        } catch (WebException original) {
            try {
                Log.debug("Exception during request: " + original.toString() + ", running locally.");
                runLocally(infoRequest);
            } catch (NoServersException e2) {
                throw original;
            }
        }
    }

    /**
     * Run the InfoRequest locally.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param infoRequest InfoRequest to run.
     * @throws WebException If fails.
     */
    public abstract void runLocally(InfoRequest infoRequest) throws WebException;

    @Override
    public void enable() throws EnableException {
        connectionSystem.enable();
    }

    @Override
    public void disable() {
        connectionSystem.disable();
    }

    public ConnectionSystem getConnectionSystem() {
        return connectionSystem;
    }

    /**
     * Updates Network page.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @throws WebException If fails.
     */
    public abstract void updateNetworkPage() throws WebException;

    /**
     * Requests Set up from Bungee.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param addressToRequestServer Address of Bungee server.
     * @throws WebException If fails.
     */
    public void requestSetUp(String addressToRequestServer) throws WebException {
        if (Check.isBungeeAvailable()) {
            throw new BadRequestException("Method not available on Bungee.");
        }
        Server bungee = new Server(-1, null, "Bungee", addressToRequestServer, -1);
        String addressOfThisServer = WebServerSystem.getInstance().getWebServer().getAccessAddress();

        ConnectionSystem connectionSystem = ConnectionSystem.getInstance();
        connectionSystem.setSetupAllowed(true);
        connectionSystem.sendInfoRequest(new SendDBSettingsRequest(addressOfThisServer), bungee);
    }
}
