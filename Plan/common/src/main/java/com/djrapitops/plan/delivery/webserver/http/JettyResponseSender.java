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
package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.webserver.Addresses;
import org.apache.commons.lang3.Strings;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.zip.GZIPOutputStream;

public class JettyResponseSender {

    private final com.djrapitops.plan.delivery.web.resolver.Response response;
    private final Request jettyRequest;
    private final Response jettyResponse;
    private final Addresses addresses;

    public JettyResponseSender(com.djrapitops.plan.delivery.web.resolver.Response response, Request jettyRequest, Response jettyResponse, Addresses addresses) {
        this.response = response;
        this.jettyRequest = jettyRequest;
        this.jettyResponse = jettyResponse;
        this.addresses = addresses;
    }

    public void send() throws IOException {
        try {
            sendAsync().join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e.getCause() != null ? e.getCause() : e);
        }
    }

    public CompletableFuture<Void> sendAsync() {
        if ("HEAD".equals(jettyRequest.getMethod()) || response.getCode() == 204 || response.getCode() == 304) {
            setResponseHeaders();
            return sendHeadResponse();
        } else if (canGzip()) {
            return sendCompressed();
        } else {
            setResponseHeaders();
            return sendRawBytes();
        }
    }

    private boolean canGzip() {
        String method = jettyRequest.getMethod();
        String mimeType = response.getHeaders().get(HttpHeader.CONTENT_TYPE.asString());
        return "GET".equals(method) && Strings.CS.containsAny(mimeType, MimeType.HTML, MimeType.CSS, MimeType.JS, MimeType.JSON, "text/plain");
    }

    public CompletableFuture<Void> sendHeadResponse() {
        response.getHeaders().remove(HttpHeader.CONTENT_LENGTH.asString());
        beginSend();
        Callback.Completable completable = new Callback.Completable();
        jettyResponse.write(true, ByteBuffer.wrap(new byte[0]), completable);
        return completable;
    }

    private void setResponseHeaders() {
        Map<String, String> responseHeaders = response.getHeaders();
        correctRedirect(responseHeaders);

        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            jettyResponse.getHeaders().add(header.getKey(), header.getValue());
        }
    }

    private void correctRedirect(Map<String, String> responseHeaders) {
        String redirect = responseHeaders.get(HttpHeader.LOCATION.asString());
        if (redirect != null) {
            if (redirect.startsWith("http") || !redirect.startsWith("/")) return;
            addresses.getAccessAddress().ifPresent(address -> responseHeaders.put(HttpHeader.LOCATION.asString(), address + redirect));
        }
    }

    private CompletableFuture<Void> sendCompressed() {
        return response.getBytesAsync().thenCompose(rawBytes -> {
            try {
                response.getHeaders().remove(HttpHeader.ACCEPT_RANGES.asString());
                response.getHeaders().put(HttpHeader.CONTENT_ENCODING.asString(), "gzip");

                byte[] gzipped = gzip(rawBytes);
                response.getHeaders().put(HttpHeader.CONTENT_LENGTH.asString(), String.valueOf(gzipped.length));
                setResponseHeaders();

                jettyResponse.setStatus(response.getCode());

                Callback.Completable completable = new Callback.Completable();
                jettyResponse.write(true, ByteBuffer.wrap(gzipped), completable);
                return completable;
            } catch (IOException e) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(e);
                return failed;
            }
        });
    }

    private byte[] gzip(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipStream = new GZIPOutputStream(bufferStream)
        ) {
            gzipStream.write(bytes);
            gzipStream.finish();
            gzipStream.flush();
            return bufferStream.toByteArray();
        }
    }

    private void beginSend() {
        String length = response.getHeaders().get(HttpHeader.CONTENT_LENGTH.asString());
        if (length == null
                || "0".equals(length)
                || response.getCode() == 204
                || response.getCode() == 304
                || "HEAD".equals(jettyRequest.getMethod())
        ) {
            jettyResponse.getHeaders().remove(HttpHeader.CONTENT_LENGTH.asString());
        }
        // Return a content length of -1 for HTTP code 204 (No content)
        // and HEAD requests to avoid warning messages.
        jettyResponse.setStatus(response.getCode());
    }

    private CompletableFuture<Void> sendRawBytes() {
        beginSend();
        return response.getBytesAsync().thenCompose(bytes -> {
            Callback.Completable completable = new Callback.Completable();
            jettyResponse.write(true, ByteBuffer.wrap(bytes), completable);
            return completable;
        });
    }
}
