package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;

import java.util.UUID;

public class PlayerPageUpdateProcessor implements Runnable {

    private final UUID uuid;

    // TODO Factory method fix
    PlayerPageUpdateProcessor(
            UUID uuid
    ) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        ResponseCache.clearResponse(PageId.PLAYER.of(uuid));
    }
}
