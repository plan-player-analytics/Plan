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
package com.djrapitops.plan.system.webserver;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents URI of a requested resource.
 *
 * @author Rsl1122
 */
public class RequestTarget {

    private final String resourceString;
    private final List<String> resource;
    private final Map<String, String> parameters;

    public RequestTarget(URI targetURI) {
        resourceString = targetURI.getPath();
        resource = Arrays.stream(resourceString.split("/")).filter(part -> !part.isEmpty()).collect(Collectors.toList());

        String parameterString = targetURI.getQuery();
        parameters = parseParameters(parameterString);
    }

    private Map<String, String> parseParameters(String parameterString) {
        if (parameterString == null || parameterString.isEmpty()) {
            return Collections.emptyMap();
        }

        TreeMap<String, String> parameters = new TreeMap<>();

        String[] keysAndValues = parameterString.split("&");
        for (String kv : keysAndValues) {
            if (kv.isEmpty()) {
                continue;
            }
            String[] keyAndValue = kv.split("=", 2);
            if (keyAndValue.length >= 2) {
                parameters.put(keyAndValue[0], keyAndValue[1]);
            }
        }

        return parameters;
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

    public Optional<String> getParameter(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    public String getResourceString() {
        return resourceString;
    }
}