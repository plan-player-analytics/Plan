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
package com.djrapitops.plan.delivery.web.resolver.request;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents URI parameters described with {@code ?param=value&param2=value2} in the URL.
 *
 * @author Rsl1122
 */
public final class URIQuery {

    private final Map<String, String> byKey;

    public URIQuery(Map<String, String> byKey) {
        this.byKey = byKey;
    }

    public URIQuery(String fromURI) {
        this.byKey = parseParameters(fromURI);
    }

    private Map<String, String> parseParameters(String fromURI) {
        if (fromURI == null || fromURI.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> parameters = new HashMap<>();
        String[] keysAndValues = StringUtils.split(fromURI, '&');
        for (String kv : keysAndValues) {
            if (kv.isEmpty()) {
                continue;
            }
            String[] keyAndValue = StringUtils.split(kv, "=", 2);
            if (keyAndValue.length >= 2) {
                try {
                    parameters.put(
                            URLDecoder.decode(keyAndValue[0], StandardCharsets.UTF_8.name()),
                            URLDecoder.decode(keyAndValue[1], StandardCharsets.UTF_8.name())
                    );
                } catch (UnsupportedEncodingException e) {
                    // If UTF-8 is unsupported, we have bigger problems
                }
            }
        }
        return parameters;
    }

    /**
     * Obtain an URI parameter by key.
     *
     * @param key Case-sensitive key, eg. 'param' in {@code ?param=value&param2=value2}
     * @return The value in the URL or empty if key is not specified in the URL.
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(byKey.get(key));
    }

    public String asString() {
        StringBuilder builder = new StringBuilder("?");
        int i = 0;
        int max = byKey.size();
        for (Map.Entry<String, String> entry : byKey.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key).append('=').append(value);
            if (i < max - 1) {
                builder.append('&');
            }
            i++;
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "URIQuery{" +
                "byKey=" + byKey +
                '}';
    }
}
