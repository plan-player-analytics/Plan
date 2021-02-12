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

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Utility for sending a Response to HttpExchange.
 *
 * @author Rsl1122
 */
public class ResponseSender {

    private final Addresses addresses;
    private final HttpExchange exchange;
    private final Response response;

    public ResponseSender(Addresses addresses, HttpExchange exchange, Response response) {
        this.addresses = addresses;
        this.exchange = exchange;
        this.response = response;
    }

    public void send() throws IOException {
        setResponseHeaders();
        if ("HEAD".equals(exchange.getRequestMethod()) || response.getCode() == 204) {
            sendHeadResponse();
        } else if ("bytes".equalsIgnoreCase(response.getHeaders().get("Accept-Ranges"))) {
            sendRawBytes();
        } else {
            sendCompressed();
        }
    }

    public void sendHeadResponse() throws IOException {
        try {
            exchange.getResponseHeaders().remove("Content-Length");
            beginSend();
        } finally {
            exchange.getRequestBody().close();
        }
    }

    private void setResponseHeaders() {
        Headers headers = exchange.getResponseHeaders();

        Map<String, String> responseHeaders = response.getHeaders();
        correctRedirect(responseHeaders);

        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            headers.set(header.getKey(), header.getValue());
        }
    }

    private void correctRedirect(Map<String, String> responseHeaders) {
        String redirect = responseHeaders.get("Location");
        if (redirect != null) {
            if (redirect.startsWith("http") || !redirect.startsWith("/")) return;
            addresses.getAccessAddress().ifPresent(address -> responseHeaders.put("Location", address + redirect));
        }
    }

    private void sendCompressed() throws IOException {
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        beginSend();
        try (OutputStream out = new GZIPOutputStream(exchange.getResponseBody())) {
            send(out);
        }
    }

    private void beginSend() throws IOException {
        String length = response.getHeaders().get("Content-Length");
        if (length == null || "0".equals(length)) {
            exchange.getResponseHeaders().remove("Content-Length");
        }
        // Return a content length of -1 for HTTP code 204 (No content)
        // and HEAD requests to avoid warning messages.
        exchange.sendResponseHeaders(response.getCode(), (response.getCode() == 204 || "HEAD".equals(exchange.getRequestMethod()) || length == null) ? -1 : Long.parseLong(length));
    }

    private void sendRawBytes() throws IOException {
        beginSend();
        try (OutputStream out = exchange.getResponseBody()) {
            send(out);
        }
    }

    private void send(OutputStream out) throws IOException {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(response.getBytes())
        ) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }
}