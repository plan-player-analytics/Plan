/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.modules;

import com.djrapitops.plan.placeholder.*;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/**
 * Module for the Placeholder API related objects.
 *
 * @author AuroraLS3
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
