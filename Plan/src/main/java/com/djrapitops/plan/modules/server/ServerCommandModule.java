package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.PlanCommand;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Server Command /plan.
 *
 * @author Rsl1122
 */
@Module
public class ServerCommandModule {

    @Provides
    PlanCommand providePlanCommand(PlanPlugin plugin) {
        return new PlanCommand(plugin);
    }

}