package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.processors.info.InfoProcessors;
import com.djrapitops.plan.system.processing.processors.player.PlayerProcessors;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Factory for creating Runnables to run with {@link com.djrapitops.plan.system.processing.Processing}.
 *
 * @author Rsl1122
 */
@Singleton
public class Processors {

    private final PlayerProcessors playerProcessors;
    private final InfoProcessors infoProcessors;

    private final Lazy<Database> database;

    @Inject
    public Processors(
            PlayerProcessors playerProcessors,
            InfoProcessors infoProcessors,

            Lazy<Database> database
    ) {
        this.playerProcessors = playerProcessors;
        this.infoProcessors = infoProcessors;
        this.database = database;
    }

    public TPSInsertProcessor tpsInsertProcessor(List<TPS> tpsList) {
        return new TPSInsertProcessor(tpsList, database.get());
    }

    public CommandProcessor commandProcessor(String command) {
        return new CommandProcessor(command, database.get());
    }

    public PlayerProcessors player() {
        return playerProcessors;
    }

    public InfoProcessors info() {
        return infoProcessors;
    }
}