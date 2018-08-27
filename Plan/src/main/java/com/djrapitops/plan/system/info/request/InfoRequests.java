package com.djrapitops.plan.system.info.request;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * Map object that holds {@link InfoRequest} objects used for handling incoming requests.
 * <p>
 * Convenience class for Dagger injection.
 *
 * @author Rsl1122
 */
public class InfoRequests extends HashMap<String, InfoRequest> {

    @Inject
    public InfoRequests(InfoRequestHandlerFactory handlers) {
        putRequest(handlers.cacheAnalysisPageRequest());
        putRequest(handlers.cacheInspectPageRequest());
        putRequest(handlers.cacheInspectPluginsTabRequest());
        putRequest(handlers.cacheNetworkPageContentRequest());

        putRequest(handlers.generateAnalysisPageRequest());
        putRequest(handlers.generateInspectPageRequest());
        putRequest(handlers.generateInspectPluginsTabRequest());
        putRequest(handlers.generateNetworkPageContentRequest());

        putRequest(handlers.saveDBSettingsRequest());
        putRequest(handlers.sendDBSettingsRequest());
        putRequest(handlers.checkConnectionRequest());
    }

    private void putRequest(InfoRequest request) {
        put(request.getClass().getSimpleName().toLowerCase(), request);
    }
}