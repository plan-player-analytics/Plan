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
package com.djrapitops.plan.data.element;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Container used to hold data for Server page.
 *
 * @author AuroraLS3
 * @see TableContainer
 * @see InspectContainer
 * @deprecated PluginData API has been deprecated - see <a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5---DataExtension-API">wiki</a> for new API.
 */
@Deprecated(since = "5.0")
public final class AnalysisContainer extends InspectContainer {

    private final Map<String, Map<UUID, ? extends Serializable>> playerTableValues;

    public AnalysisContainer() {
        playerTableValues = new TreeMap<>();
    }

    public Map<String, Map<UUID, ? extends Serializable>> getPlayerTableValues() {
        return playerTableValues;
    }

    public void addPlayerTableValues(String columnName, Map<UUID, ? extends Serializable> values) {
        playerTableValues.put(columnName, values);
    }

    public boolean hasPlayerTableValues() {
        return !playerTableValues.isEmpty();
    }
}
