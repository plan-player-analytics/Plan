package com.djrapitops.plan.modules;

import com.djrapitops.plan.placeholder.*;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/**
 * Module for the Placeholder API related objects.
 *
 * @author Rsl1122
 */
@Module
public interface PlaceholderModule {

    @Binds
    @IntoSet
    Placeholders bindOperatorPlaceholders(OperatorPlaceholders placeholders);

    @Binds
    @IntoSet
    Placeholders bindPlayerPlaceHolders(PlayerPlaceHolders placeholders);

    @Binds
    @IntoSet
    Placeholders bindServerPlaceHolders(ServerPlaceHolders placeholders);

    @Binds
    @IntoSet
    Placeholders bindSessionPlaceHolders(SessionPlaceHolders placeholders);

    @Binds
    @IntoSet
    Placeholders bindWorldTimePlaceHolders(WorldTimePlaceHolders placeholders);

}
