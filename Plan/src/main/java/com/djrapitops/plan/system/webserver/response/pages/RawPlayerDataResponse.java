package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.cache.CacheSystem;

import java.util.UUID;

/**
 * Raw Data JSON response for a Player.
 *
 * @author Rsl1122
 */
public class RawPlayerDataResponse extends RawDataResponse {

    public RawPlayerDataResponse(UUID uuid) {
        super(CacheSystem.getInstance().getDataContainerCache().getPlayerContainer(uuid));
    }
}