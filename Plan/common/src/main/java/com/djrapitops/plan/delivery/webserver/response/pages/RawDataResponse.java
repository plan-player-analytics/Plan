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
package com.djrapitops.plan.delivery.webserver.response.pages;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response for sending raw data as JSON when it is inside a DataContainer.
 * <p>
 * This transform class is required to remove Key-Supplier object pollution in the resulting JSON, as well as to remove
 * the effects of the caching layer.
 *
 * @author Rsl1122
 */
public class RawDataResponse extends JSONResponse {

    public RawDataResponse(DataContainer dataContainer) {
        super(mapToNormalMap(dataContainer));
    }

    public static Map<String, Object> mapToNormalMap(DataContainer player) {
        Map<String, Object> values = new HashMap<>();
        player.getMap().forEach((key, value) ->
                {
                    if (value instanceof DataContainer) {
                        value = mapToNormalMap((DataContainer) value);
                    }
                    if (value instanceof Map) {
                        value = handleMap((Map<?, ?>) value);
                    }
                    if (value instanceof List) {
                        value = handleList((List<?>) value);
                    }
                    values.put(key.getKeyName(), value);
                }
        );
        return values;
    }

    private static List<?> handleList(List<?> list) {
        if (list.stream().findAny().orElse(null) instanceof DataContainer) {
            return Lists.map(list, obj -> mapToNormalMap((DataContainer) obj));
        }
        return list;
    }

    private static Map<?, ?> handleMap(Map<?, ?> map) {
        if (map.values().stream().findAny().orElse(null) instanceof DataContainer) {
            Map<Object, Object> newMap = new HashMap<>();
            map.forEach((key, value) -> newMap.put(key, mapToNormalMap((DataContainer) value)));
            return newMap;
        }
        return map;
    }
}