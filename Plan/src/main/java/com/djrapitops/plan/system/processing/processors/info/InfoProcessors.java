package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Factory for creating Runnables related to {@link InfoSystem} to run with {@link com.djrapitops.plan.system.processing.Processing}.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoProcessors {

    private final Lazy<InfoSystem> infoSystem;
    private final Lazy<WebExceptionLogger> webExceptionLogger;
    private final Lazy<RunnableFactory> runnableFactory;

    @Inject
    public InfoProcessors(
            Lazy<InfoSystem> infoSystem,
            Lazy<WebExceptionLogger> webExceptionLogger,
            Lazy<RunnableFactory> runnableFactory
    ) {
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
        this.runnableFactory = runnableFactory;
    }

    public InspectCacheRequestProcessor inspectCacheRequestProcessor(
            UUID uuid,
            Sender sender,
            String playerName,
            BiConsumer<Sender, String> msgSender
    ) {
        return new InspectCacheRequestProcessor(uuid, sender, playerName, msgSender,
                infoSystem.get(), webExceptionLogger.get()
        );
    }

    public NetworkPageUpdateProcessor networkPageUpdateProcessor() {
        return new NetworkPageUpdateProcessor(infoSystem.get(), webExceptionLogger.get());
    }

    public PlayerPageUpdateProcessor playerPageUpdateProcessor(UUID uuid) {
        return new PlayerPageUpdateProcessor(uuid,
                infoSystem.get(), webExceptionLogger.get(), runnableFactory.get()
        );
    }
}