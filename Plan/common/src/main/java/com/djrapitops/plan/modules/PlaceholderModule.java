package com.djrapitops.plan.modules;

import com.djrapitops.plan.placeholder.*;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface PlaceholderModule {

    @Binds
    @IntoSet
    PlaceholderRegistry bindOperatorPlaceholders(OperatorPlaceholders placeholders);

    @Binds
    @IntoSet
    PlaceholderRegistry bindPlayerPlaceHolders(PlayerPlaceHolders placeholders);

    @Binds
    @IntoSet
    PlaceholderRegistry bindServerPlaceHolders(ServerPlaceHolders placeholders);

    @Binds
    @IntoSet
    PlaceholderRegistry bindSessionPlaceHolders(SessionPlaceHolders placeholders);

    @Binds
    @IntoSet
    PlaceholderRegistry bindWorldTimePlaceHolders(WorldTimePlaceHolders placeholders);

}
