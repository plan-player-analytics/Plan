package com.djrapitops.plan.modules;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.PlanBungeeCommand;
import com.djrapitops.plugin.command.CommandNode;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for binding PlanBungee instance.
 *
 * @author Rsl1122
 */
@Module
public class BungeePlanModule {

    @Provides
    @Singleton
    PlanPlugin providePlanPlugin(PlanBungee plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanBungeeCommand command) {
        return command;
    }
}