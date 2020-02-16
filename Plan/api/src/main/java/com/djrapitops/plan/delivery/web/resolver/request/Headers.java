package com.djrapitops.plan.delivery.web.resolver.request;

import java.util.Map;
import java.util.Optional;

/**
 * Request headers, read only.
 *
 * @author Rsl1122
 */
public class Headers {

    private final Map<String, String> headers;

    public Headers(Map<String, String> headers) {
        this.headers = headers;
    }

    private Optional<String> get(String key) {
        return Optional.ofNullable(headers.get(key));
    }
}