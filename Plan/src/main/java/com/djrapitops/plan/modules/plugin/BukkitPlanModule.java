package com.djrapitops.plan.modules.plugin;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plugin.command.CommandNode;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for binding Plan instance.
 *
 * @author Rsl1122
 */
@Module
public class BukkitPlanModule {

    @Provides
    @Singleton
    PlanPlugin providePlanPlugin(Plan plugin) {
        return plugin;
    }

    @Provides
    @Singleton
    @Named("mainCommand")
    CommandNode provideMainCommand(PlanCommand command) {
        return command;
    }
}