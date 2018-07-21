package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.UUID;

public class PlayerPageUpdateProcessor implements Runnable {

    private final UUID uuid;

    public PlayerPageUpdateProcessor(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        RunnableFactory.createNew("Generate Inspect page: " + uuid, new AbsRunnable() {
            @Override
            public void run() {
                try {
                    WebExceptionLogger.logIfOccurs(PlayerPageUpdateProcessor.class,
                            () -> InfoSystem.getInstance().generateAndCachePlayerPage(uuid)
                    );
                } finally {
                    cancel();
                }
            }
        }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 20);
    }
}
