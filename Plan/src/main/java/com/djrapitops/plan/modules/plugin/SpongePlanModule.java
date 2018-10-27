package com.djrapitops.plan.modules.plugin;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plugin.command.CommandNode;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for binding PlanSponge instance.
 *
 * @author Rsl1122
 */
@Module
public class SpongePlanModule {

    @Provides
    @Singleton
    PlanPlugin providePlanPlugin(PlanSponge plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanCommand command) {
        return command;
    }
}