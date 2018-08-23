package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.LocaleSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger Module for LocaleSystem.
 *
 * @author Rsl1122
 */
@Module
public class LocaleModule {

    @Provides
    @Singleton
    Locale provideLocale(LocaleSystem localeSystem) {
        return localeSystem.getLocale();
    }
}