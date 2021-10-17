package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.webserver.Addresses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class JettyResponseSender {

    private final Response response;
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;
    private final Addresses addresses;

    public JettyResponseSender(Response response, HttpServletRequest servletRequest, HttpServletResponse servletResponse, Addresses addresses) {
        this.response = response;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.addresses = addresses;
    }

    public void send() throws IOException {
        setResponseHeaders();
        if ("HEAD".equals(servletRequest.getMethod()) || response.getCode() == 204) {
            sendHeadResponse();
        } else if ("bytes".equalsIgnoreCase(response.getHeaders().get(HttpHeader.ACCEPT_RANGES.asString()))) {
            sendRawBytes();
        } else {
            sendCompressed();
        }
    }

    public void sendHeadResponse() throws IOException {
        try {
            response.getHeaders().remove(HttpHeader.CONTENT_LENGTH.asString());
            beginSend();
        } finally {
            servletResponse.getOutputStream().close();
        }
    }

    private void setResponseHeaders() {
        Map<String, String> responseHeaders = response.getHeaders();
        correctRedirect(responseHeaders);

        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            servletResponse.setHeader(header.getKey(), header.getValue());
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
        servletResponse.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
        beginSend();
        try (OutputStream out = new GZIPOutputStream(servletResponse.getOutputStream())) {
            send(out);
        }
    }

    private void beginSend() throws IOException {
        String length = response.getHeaders().get(HttpHeader.CONTENT_LENGTH.asString());
        if (length == null || "0".equals(length) || response.getCode() == 204 || "HEAD".equals(servletRequest.getMethod())) {
            servletResponse.setHeader(HttpHeader.CONTENT_LENGTH.asString(), null);
        }
        // Return a content length of -1 for HTTP code 204 (No content)
        // and HEAD requests to avoid warning messages.
        servletResponse.setStatus(response.getCode());
    }

    private void sendRawBytes() throws IOException {
        beginSend();
        try (OutputStream out = servletResponse.getOutputStream()) {
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
