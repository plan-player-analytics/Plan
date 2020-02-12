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
import com.djrapitops.plan.storage.file.PlanFiles;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link Response_old} for raw bytes.
 *
 * @author Rsl1122
 */
public class ByteResponse extends Response_old {

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
             InputStream bis = files.getCustomizableResourceOrDefault(fileName).asInputStream()) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }
}
