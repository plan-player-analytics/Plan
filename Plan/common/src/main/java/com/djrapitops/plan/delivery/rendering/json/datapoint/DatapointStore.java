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
package com.djrapitops.plan.delivery.rendering.json.datapoint;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
@Singleton
public class DatapointStore {

    private final Map<DatapointType, Datapoint<?>> dataPointsByType;

    @Inject
    public DatapointStore(Set<Datapoint<?>> datapoints) {
        dataPointsByType = datapoints.stream().collect(Collectors.toMap(Datapoint::getType, Function.identity()));
    }

    public Optional<Object> getValue(DatapointType datapointType, GenericFilter filter) {
        return Optional.ofNullable(dataPointsByType.get(datapointType))
                .flatMap(datapoint -> datapoint.getValue(filter));
    }

    public Optional<WebPermission> getPermission(DatapointType datapointType, GenericFilter filter) {
        return Optional.ofNullable(dataPointsByType.get(datapointType))
                .map(datapoint -> datapoint.getPermission(filter));
    }
}
