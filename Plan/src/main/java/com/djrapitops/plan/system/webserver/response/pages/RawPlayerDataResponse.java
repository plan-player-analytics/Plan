package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.data.store.containers.PlayerContainer;

/**
 * Raw Data JSON response for a Player.
 *
 * @author Rsl1122
 */
public class RawPlayerDataResponse extends RawDataResponse {

    public RawPlayerDataResponse(PlayerContainer playerContainer) {
        super(playerContainer);
    }
}