/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.GenerateInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

interface ExceptionLoggingAction {

    void performAction() throws WebException;

}

/**
 * Information management system.
 * <p>
 * Subclasses should decide how InfoRequests are run locally if necessary.
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
        NullCheck.check(infoSystem, new IllegalStateException("Info System was not initialized."));
        return infoSystem;
    }

    public void generateAndCachePlayerPage(UUID player) throws WebException {
        sendRequest(new GenerateInspectPageRequest(player));
    }

    public void generateAnalysisPageOfThisServer() throws WebException {
        generateAnalysisPage(Plan.getServerUUID());
    }

    public void generateAnalysisPage(UUID serverUUID) throws WebException {
        GenerateAnalysisPageRequest request = new GenerateAnalysisPageRequest(serverUUID);
        if (Plan.getServerUUID().equals(serverUUID)) {
            runLocally(request);
        } else {
            sendRequest(request);
        }
    }

    public void sendRequest(InfoRequest infoRequest) throws WebException {
        if (!connectionSystem.isServerAvailable()) {
            runLocally(infoRequest);
        }
        connectionSystem.sendInfoRequest(infoRequest);
    }

    protected abstract void runLocally(InfoRequest infoRequest);

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

    public abstract void updateNetworkPage();

    public void handlePossibleException(ExceptionLoggingAction action) {
        try {
            action.performAction();
        } catch (ConnectionFailException | UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            Log.warn(e.getMessage());
        } catch (WebException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}