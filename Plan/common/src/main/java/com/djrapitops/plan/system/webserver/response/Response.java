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
package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rsl1122
 */
public abstract class Response {

    private String type;
    private String header;
    private String content;

    protected Headers responseHeaders;

    public Response(ResponseType type) {
        this.type = type.get();
    }

    /**
     * Default Response constructor that defaults ResponseType to HTML.
     */
    public Response() {
        this(ResponseType.HTML);
    }

    protected String getHeader() {
        return header;
    }

    public Optional<String> getHeader(String called) {
        for (String header : header.split("\r\n")) {
            if (header.startsWith(called)) {
                return Optional.of(header.split(": ")[1]);
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
        return header == null ? 500 : Integer.parseInt(header.split(" ")[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equals(header, response.header) &&
                Objects.equals(content, response.content);
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

    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        responseHeaders.set("Content-Type", type);
        responseHeaders.set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(getCode(), 0);

        String sentContent = getContent();
        // TODO Smell
        if (!(this instanceof JavaScriptResponse)) {
            sentContent = locale.replaceMatchingLanguage(sentContent);
        }
        sentContent = theme.replaceThemeColors(sentContent);

        try (
                GZIPOutputStream out = new GZIPOutputStream(exchange.getResponseBody());
                ByteArrayInputStream bis = new ByteArrayInputStream(sentContent.getBytes(StandardCharsets.UTF_8))
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
}
