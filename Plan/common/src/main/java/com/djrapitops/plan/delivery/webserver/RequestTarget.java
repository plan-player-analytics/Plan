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
package com.djrapitops.plan.delivery.webserver;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents URI of a requested resource.
 *
 * @author Rsl1122
 */
@Deprecated
public class RequestTarget {

    private final String resourceString;
    private final List<String> resource;
    private final Map<String, String> parameters;

    public RequestTarget(URI targetURI) {
        resourceString = targetURI.getPath();
        resource = Arrays.stream(StringUtils.split(resourceString, '/'))
                .filter(part -> !part.isEmpty()).collect(Collectors.toList());

        parameters = new TreeMap<>();
        parseParameters(targetURI.getQuery());
    }

    private void parseParameters(String parameterString) {
        if (parameterString == null || parameterString.isEmpty()) {
            return;
        }

        String[] keysAndValues = StringUtils.split(parameterString, '&');
        for (String kv : keysAndValues) {
            if (kv.isEmpty()) {
                continue;
            }
            String[] keyAndValue = StringUtils.split(kv, "=", 2);
            if (keyAndValue.length >= 2) {
                parameters.put(keyAndValue[0], keyAndValue[1]);
            }
        }
    }

    public boolean isEmpty() {
        return resource.isEmpty();
    }

    public int size() {
        return resource.size();
    }

    public String get(int index) {
        return resource.get(index);
    }

    public void removeFirst() {
        if (!isEmpty()) {
            resource.remove(0);
        }
    }

    public boolean endsWith(String suffix) {
        return resourceString.endsWith(suffix);
    }

    public boolean endsWithAny(String... suffixes) {
        return StringUtils.endsWithAny(resourceString, suffixes);
    }

    public Optional<String> getParameter(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    public String getResourceString() {
        return resourceString;
    }
}