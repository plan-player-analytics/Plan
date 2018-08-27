package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.data.store.containers.ServerContainer;

/**
 * Raw Data JSON response for a Server.
 *
 * @author Rsl1122
 */
public class RawServerDataResponse extends RawDataResponse {

    public RawServerDataResponse(ServerContainer serverContainer) {
        super(serverContainer);
    }
}