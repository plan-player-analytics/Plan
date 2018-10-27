package com.djrapitops.plan.utilities.html.graphs.special;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.utilities.html.graphs.HighChart;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * Factory class for different objects representing special HTML graphs.
 *
 * @author Rsl1122
 */
@Singleton
public class SpecialGraphFactory {

    @Inject
    public SpecialGraphFactory() {
        // Inject Constructor.
    }

    public HighChart punchCard(Collection<Session> sessions) {
        return new PunchCard(sessions);
    }

    public HighChart worldMap(PlayersMutator mutator) {
        return new WorldMap(mutator);
    }
}