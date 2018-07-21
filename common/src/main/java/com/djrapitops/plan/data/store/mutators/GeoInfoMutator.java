package com.djrapitops.plan.data.store.mutators;


import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Mutator for lists of GeoInfo objects.
 *
 * @author Rsl1122
 * @see GeoInfo for the object.
 */
public class GeoInfoMutator {

    private final List<GeoInfo> geoInfo;

    public GeoInfoMutator(List<GeoInfo> geoInfo) {
        this.geoInfo = geoInfo;
    }

    public static GeoInfoMutator forContainer(DataContainer container) {
        return new GeoInfoMutator(container.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>()));
    }

    public GeoInfoMutator forCollection(Collection<GeoInfo> collection) {
        return new GeoInfoMutator(new ArrayList<>(collection));
    }

    public Optional<GeoInfo> mostRecent() {
        if (geoInfo.isEmpty()) {
            return Optional.empty();
        }
        geoInfo.sort(new GeoInfoComparator());
        return Optional.of(geoInfo.get(0));
    }
}