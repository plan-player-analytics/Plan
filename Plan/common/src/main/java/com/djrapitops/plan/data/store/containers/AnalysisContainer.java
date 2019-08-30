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
package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.system.delivery.domain.container.DynamicDataContainer;
import com.djrapitops.plan.system.delivery.domain.keys.PlaceholderKey;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.delivery.domain.keys.AnalysisKeys for Key objects
 * @see PlaceholderKey for placeholder information
 * @deprecated AnalysisContainer is no longer used.
 */
@Deprecated
public class AnalysisContainer extends DynamicDataContainer {
    public AnalysisContainer() {
    }
}