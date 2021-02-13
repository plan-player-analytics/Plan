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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Mutator for lists of GeoInfo objects.
 *
 * @author AuroraLS3
 * @see GeoInfo for the object.
 */
public class GeoInfoMutator {

    private final List<GeoInfo> geoInfo;

    public static GeoInfoMutator forContainer(DataContainer container) {
        return new GeoInfoMutator(container.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>()));
    }

    public GeoInfoMutator(List<GeoInfo> geoInfo) {
        this.geoInfo = geoInfo;
    }

    public GeoInfoMutator forCollection(Collection<GeoInfo> collection) {
        return new GeoInfoMutator(new ArrayList<>(collection));
    }

    public Optional<GeoInfo> mostRecent() {
        if (geoInfo.isEmpty()) {
            return Optional.empty();
        }
        return geoInfo.stream().min(new GeoInfoComparator());
    }
}