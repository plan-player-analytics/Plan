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
package com.djrapitops.plan.utilities.html.graphs.special;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.html.graphs.HighChart;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

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

    public PunchCard punchCard(List<Session> sessions) {
        return punchCard(new SessionsMutator(sessions));
    }

    public PunchCard punchCard(SessionsMutator sessions) {
        return new PunchCard(sessions);
    }

    public WorldMap worldMap(Map<String, Integer> geolocationCounts) {
        return new WorldMap(geolocationCounts);
    }

    public HighChart worldMap(PlayersMutator mutator) {
        return new WorldMap(mutator);
    }
}