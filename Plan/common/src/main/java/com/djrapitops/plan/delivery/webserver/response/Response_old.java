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
package com.djrapitops.plan.delivery.webserver.response;

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rsl1122
 */
@Deprecated
public abstract class Response_old {

    private String type;
    private String header;
    private String content;

    protected Headers responseHeaders;

    public Response_old(ResponseType type) {
        this.type = type.get();
    }

    /**
     * Default Response constructor that defaults ResponseType to HTML.
     */
    public Response_old() {
        this(ResponseType.HTML);
    }

    protected String getHeader() {
        return header;
    }

    public Optional<String> getHeader(String called) {
        if (header != null) {
            for (String header : StringUtils.split(header, "\r\n")) {
                if (called == null) {
                    return Optional.of(header);
                }
                if (StringUtils.startsWith(header, called)) {
                    return Optional.of(StringUtils.split(header, ':')[1].trim());
                }
            }
        }
        return Optional.empty();
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getResponse() {
        return header + "\r\n"
                + "Content-Type: " + type + ";\r\n"
                + "Content-Length: " + content.length() + "\r\n"
                + "\r\n"
                + content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCode() {
        return getHeader(null).map(h -> Integer.parseInt(StringUtils.split(h, ' ')[1])).orElse(500);
    }

    @Deprecated
    public static Response_old from(com.djrapitops.plan.delivery.web.resolver.Response apiResponse) {
        Response_old response = new Response_old() {};
        response.setContent(apiResponse.getCharset().map(charset -> new String(apiResponse.getBytes(), charset))
                .orElse(new String(apiResponse.getBytes())));
        response.setHeader("HTTP/1.1 " + apiResponse.getCode() + " ");
        for (Map.Entry<String, String> header : apiResponse.getHeaders().entrySet()) {
            response.header += header.getKey() + ": " + header.getValue() + ";\r\n";
        }
        return response;
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, content);
    }

    protected void setType(ResponseType type) {
        this.type = type.get();
    }

    public void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    protected void translate(Function<String, String> translator) {
        content = translator.apply(content);
    }

    protected void fixThemeColors(Theme theme) {
        content = theme.replaceThemeColors(content);
    }

    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        responseHeaders.set("Content-Type", type);
        responseHeaders.set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(getCode(), 0);

        try (
                GZIPOutputStream out = new GZIPOutputStream(exchange.getResponseBody());
                ByteArrayInputStream bis = new ByteArrayInputStream((content != null ? content : "").getBytes(StandardCharsets.UTF_8))
        ) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }

    @Override
    public String toString() {
        return header + " | " + getResponse();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response_old response = (Response_old) o;
        return Objects.equals(header, response.header) &&
                Objects.equals(content, response.content);
    }
}
