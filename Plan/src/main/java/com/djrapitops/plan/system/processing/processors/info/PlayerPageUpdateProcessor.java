package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.UUID;

public class PlayerPageUpdateProcessor implements Runnable {

    private final UUID uuid;

    private final InfoSystem infoSystem;
    private final WebExceptionLogger webExceptionLogger;
    private final RunnableFactory runnableFactory;

    PlayerPageUpdateProcessor(
            UUID uuid,
            InfoSystem infoSystem,
            WebExceptionLogger webExceptionLogger,
            RunnableFactory runnableFactory
    ) {
        this.uuid = uuid;
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
        this.runnableFactory = runnableFactory;
    }

    @Override
    public void run() {
        if (!infoSystem.getConnectionSystem().isServerAvailable() || Check.isBungeeAvailable()) {
            runnableFactory.create("Generate Inspect page: " + uuid, new AbsRunnable() {
                @Override
                public void run() {
                    try {

                        webExceptionLogger.logIfOccurs(PlayerPageUpdateProcessor.class,
                                () -> infoSystem.generateAndCachePlayerPage(uuid)
                        );
                    } finally {
                        cancel();
                    }
                }
            }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 20);
        }
    }
}
