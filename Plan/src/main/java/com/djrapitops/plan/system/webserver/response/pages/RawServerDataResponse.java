package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.cache.CacheSystem;

import java.util.UUID;

/**
 * Raw Data JSON response for a Server.
 *
 * @author Rsl1122
 */
public class RawServerDataResponse extends RawDataResponse {

    public RawServerDataResponse(UUID serverUUID) {
        super(CacheSystem.getInstance().getDataContainerCache().getAnalysisContainer(serverUUID).getServerContainer());
    }
}