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
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author AuroraLS3
 */
@Singleton
@SuppressWarnings("java:S1452")
public class DatapointStore {

    private final Map<DatapointType, Datapoint<?>> dataPointsByType;

    private final Map<DatapointCacheKey, Long> lastModified = new ConcurrentHashMap<>();

    @Inject
    public DatapointStore(Set<Datapoint<?>> datapoints) {
        dataPointsByType = datapoints.stream().collect(Collectors.toMap(Datapoint::getType, Function.identity()));
    }

    public Optional<DatapointValue> getValue(DatapointType datapointType, GenericFilter filter) {
        return Optional.ofNullable(dataPointsByType.get(datapointType))
                .flatMap(datapoint ->
                        datapoint.getValue(filter)
                                .map(value -> new DatapointValue(
                                        datapointType,
                                        value,
                                        datapoint.getFormatType()
                                ))
                );
    }

    public long getLastModified(@Nullable Long etag, DatapointType datapointType, GenericFilter filter) {
        long now = System.currentTimeMillis();
        if (etag == null) return now;

        Set<DatapointCacheKey> cacheKeys = datapointType.getCacheKeys();
        if (cacheKeys.contains(DatapointCacheKey.TPS)) {
            long diff = TimeUnit.SECONDS.toMillis(30);
            boolean hasNewData = now - etag >= diff;
            boolean nearLiveEdge = filter.getBefore() >= now - diff;
            return nearLiveEdge && hasNewData ? now : etag;
        } else if (cacheKeys.contains(DatapointCacheKey.SESSION)) {
            Long lastChange = lastModified.computeIfAbsent(DatapointCacheKey.SESSION, key -> now);
            boolean hasNewData = lastChange > etag;
            boolean nearLiveEdge = filter.getBefore() >= lastChange;
            return nearLiveEdge && hasNewData ? lastChange : etag;
        }
        return now;
    }

    public Optional<WebPermission> getPermission(DatapointType datapointType, GenericFilter filter) {
        return Optional.ofNullable(dataPointsByType.get(datapointType))
                .map(datapoint -> datapoint.getPermission(filter));
    }

    public void clearLastModified(DatapointCacheKey key) {
        lastModified.remove(key);
    }
}
