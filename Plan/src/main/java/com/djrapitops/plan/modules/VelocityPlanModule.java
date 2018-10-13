package com.djrapitops.plan.modules;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.command.PlanVelocityCommand;
import com.djrapitops.plugin.command.CommandNode;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for binding PlanVelocity instance.
 *
 * @author Rsl1122
 */
@Module
public class VelocityPlanModule {

    @Provides
    @Singleton
    PlanPlugin providePlanPlugin(PlanVelocity plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanVelocityCommand command) {
        return command;
    }
}