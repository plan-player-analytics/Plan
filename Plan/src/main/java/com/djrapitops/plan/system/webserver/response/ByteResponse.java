package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link Response} for raw bytes.
 *
 * @author Rsl1122
 */
public class ByteResponse extends Response {

    private final PlanFiles files;
    private final String fileName;

    public ByteResponse(ResponseType type, String fileName, PlanFiles files) {
        super(type);
        this.fileName = fileName;
        this.files = files;

        setHeader("HTTP/1.1 200 OK");
    }

    @Override
    public void send(HttpExchange exchange, Locale locale, Theme theme) throws IOException {
        responseHeaders.set("Accept-Ranges", "bytes");
        exchange.sendResponseHeaders(getCode(), 0);

        try (OutputStream out = exchange.getResponseBody();
             InputStream bis = files.readCustomizableResource(fileName)) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }
}
