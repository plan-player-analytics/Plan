package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.IPlugin;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for VersionCheckSystem.
 *
 * @author Rsl1122
 */
@Module
public class VersionCheckModule {

    @Provides
    VersionCheckSystem provideVersionCheckSystem(IPlugin plugin, Locale locale) {
        // TODO Remove supplier
        return new VersionCheckSystem(plugin.getVersion(), () -> locale);
    }

}