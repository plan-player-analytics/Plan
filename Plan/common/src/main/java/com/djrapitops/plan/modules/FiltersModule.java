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

import com.djrapitops.plan.storage.database.queries.filter.Filter;
import com.djrapitops.plan.storage.database.queries.filter.filters.*;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface FiltersModule {

    @Binds
    @IntoSet
    Filter filter1(PlayedBetweenDateRangeFilter filter);

    @Binds
    @IntoSet
    Filter filter2(RegisteredBetweenDateRangeFilter filter);

    @Binds
    @IntoSet
    Filter filter3(OperatorsFilter filter);

    @Binds
    @IntoSet
    Filter filter4(BannedFilter filter);

    @Binds
    @IntoSet
    Filter filter5(ActivityIndexNowFilter filter);

    @Binds
    @IntoSet
    Filter filter6(JoinAddressFilter filter);

    @Binds
    @IntoSet
    Filter filter7(GeolocationsFilter filter);

    @Binds
    @IntoSet
    Filter filter8(PluginBooleanGroupFilter filter);

    @Binds
    @IntoSet
    Filter filter9(PlayedOnServerFilter filter);

    @Binds
    @IntoSet
    Filter filter10(PlayedOnDateFilter filter);

    @Binds
    @IntoSet
    Filter filter11(ActivityIndexOnDateFilter filter);

    @Binds
    @IntoSet
    Filter filter12(LastSeenBetweenDateRangeFilter filter);

}
