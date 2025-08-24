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

import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResponseBuilder {

    private final Response response;

    ResponseBuilder() {
        this.response = new Response();
    }

    /**
     * Set MIME Type of the Response.
     *
     * @param mimeType MIME type of the Response <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types">Documentation</a>
     * @return this builder.
     * @see MimeType for common MIME types.
     */
    public ResponseBuilder setMimeType(String mimeType) {
        return setHeader("Content-Type", mimeType);
    }

    /**
     * Set HTTP Status code.
     * <p>
     * Default status code is 200 (OK) if not set.
     *
     * @param code 1xx, 2xx, 3xx, 4xx, 5xx, <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">Documentation</a>
     * @return this builder.
     */
    public ResponseBuilder setStatus(int code) {
        response.code = code;
        return this;
    }

    /**
     * Set HTTP Response header.
     *
     * @param header Key of the header. <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers">Documentation</a>
     * @param value  Value for the header.
     * @return this builder.
     */
    public ResponseBuilder setHeader(String header, Object value) {
        response.headers.put(header, value.toString());
        return this;
    }

    protected ResponseBuilder removeHeader(String header) {
        response.headers.remove(header);
        return this;
    }

    /**
     * Utility method for building redirects.
     *
     * @param url URL to redirect the client to with 302 Found.
     * @return <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Location">Documentation</a>
     */
    public ResponseBuilder redirectTo(String url) {
        return setStatus(302).setHeader("Location", url).setContent(new byte[0]);
    }

    public ResponseBuilder setContent(WebResource resource) {
        return setContent(resource.asBytes());
    }

    public ResponseBuilder setContent(byte[] bytes) {
        response.bytes = bytes;
        return setHeader("Content-Length", bytes.length)
                .setHeader("Accept-Ranges", "bytes"); // Does not compress
    }

    public ResponseBuilder setContent(String utf8String) {
        return setContent(utf8String, StandardCharsets.UTF_8);
    }

    public ResponseBuilder setContent(String content, Charset charset) {
        if (content == null) return setContent(new byte[0]);
        if (charset == null) return setContent(content); // UTF-8 used
        String mimeType = getMimeType();
        response.charset = charset;

        if (mimeType != null) {
            String[] parts = mimeType.split(";");
            if (parts.length == 1) {
                setMimeType(parts[0] + "; charset=" + charset.name().toLowerCase());
            }
        }

        return setContent(content.getBytes(charset))
                .removeHeader("Accept-Ranges"); // Can compress
    }

    /**
     * Set content as serialized JSON object.
     *
     * @param objectToSerialize Object to serialize into JSON with Gson. If the object is a String it is assumed to be valid JSON.
     * @return this builder.
     */
    public ResponseBuilder setJSONContent(Object objectToSerialize) {
        if (objectToSerialize instanceof String) return setJSONContent((String) objectToSerialize);
        return setJSONContent(new Gson().toJson(objectToSerialize));
    }

    public ResponseBuilder setJSONContent(String json) {
        return setMimeType(MimeType.JSON).setContent(json);
    }

    /**
     * Finish building.
     *
     * @return Response.
     * @throws InvalidResponseException if content was not defined (not even empty byte array).
     * @throws InvalidResponseException if content has bytes, but MIME-type is not defined.
     * @throws InvalidResponseException if status code is outside range 100-599.
     * @see #setMimeType(String) to set MIME-type.
     */
    public Response build() {
        byte[] content = response.bytes;
        if(content == null && response.code == 204) {
            // HTTP Code 204 requires no response, so there is no need to validate it.
            return response;
        }
        exceptionIf(content == null, "Content not defined for Response");
        String mimeType = getMimeType();
        exceptionIf(content.length > 0 && mimeType == null, "MIME Type not defined for Response");
        exceptionIf(content.length > 0 && mimeType.isEmpty(), "MIME Type empty for Response");
        exceptionIf(response.code < 100 || response.code >= 600, "HTTP Status code out of bounds (" + response.code + ")");
        return response;
    }

    private String getMimeType() {
        return response.headers.get("Content-Type");
    }

    private void exceptionIf(boolean value, String errorMsg) {
        if (value) throw new InvalidResponseException(errorMsg);
    }

    /**
     * Thrown when {@link ResponseBuilder} is missing some parameters.
     *
     * @author AuroraLS3
     */
    public static class InvalidResponseException extends IllegalStateException {
        public InvalidResponseException(String s) {
            super(s);
        }
    }
}
