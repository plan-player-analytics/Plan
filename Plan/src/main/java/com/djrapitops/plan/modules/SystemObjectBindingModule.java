package com.djrapitops.plan.modules;

import com.djrapitops.plan.data.plugin.PluginsConfigSection;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.LocaleSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding object instances found inside other systems.
 *
 * @author Rsl1122
 */
@Module
public class SystemObjectBindingModule {

    @Provides
    @Singleton
    Locale provideLocale(LocaleSystem localeSystem) {
        return localeSystem.getLocale();
    }

    @Provides
    @Singleton
    PluginsConfigSection providePluginsConfigSection(PlanConfig config) {
        return config.getPluginsConfigSection();
    }

}