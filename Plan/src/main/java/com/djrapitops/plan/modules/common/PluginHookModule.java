package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.data.plugin.HookHandler;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Hooking to other plugins.
 *
 * @author Rsl1122
 */
@Module
public class PluginHookModule {

    @Provides
    HookHandler provideHookHandler() {
        return new HookHandler();
    }
}