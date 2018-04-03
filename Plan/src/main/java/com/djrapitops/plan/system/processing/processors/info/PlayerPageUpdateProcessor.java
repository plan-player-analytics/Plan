package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;

import java.util.UUID;

public class PlayerPageUpdateProcessor implements Runnable {

    private final UUID uuid;

    public PlayerPageUpdateProcessor(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> InfoSystem.getInstance().generateAndCachePlayerPage(uuid)
        );
    }
}
