package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.export.ExportSystem;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Export system.
 *
 * @author Rsl1122
 */
@Module
public class ExportModule {

    @Provides
    ExportSystem provideExportSystem(PlanPlugin plugin) {
        return new ExportSystem(plugin);
    }
}