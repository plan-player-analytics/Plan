package com.djrapitops.plan.utilities.html.graphs.bar;

import com.djrapitops.plan.data.store.mutators.PlayersMutator;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory class for Bar Graphs.
 *
 * @author Rsl1122
 */
@Singleton
public class BarGraphFactory {
    @Inject
    public BarGraphFactory() {
        // Inject Constructor.
    }

    public BarGraph geolocationBarGraph(PlayersMutator playersMutator) {
        return new GeolocationBarGraph(playersMutator);
    }
}