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
package com.djrapitops.plan.delivery.web.resolver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a response that will be sent over HTTP.
 *
 * @author AuroraLS3
 * @see MimeType for MIME types that are commonly used.
 */
public final class Response {

    final Map<String, String> headers;
    int code = 200;
    byte[] bytes;
    Charset charset; // can be null (raw bytes)

    Response() {
        headers = new HashMap<>();
    }

    public static ResponseBuilder builder() {
        return new ResponseBuilder();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getAsString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Optional<Charset> getCharset() {
        return Optional.ofNullable(charset);
    }

    public boolean isErrorResponse() {
        return code >= 400;
    }

}
