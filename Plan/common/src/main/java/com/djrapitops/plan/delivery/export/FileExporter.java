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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * Code that handles writing the actual files that are exported.
 *
 * @author Rsl1122
 */
abstract class FileExporter {

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] bytes = new byte[1024];

        while ((read = in.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
    }

    void export(Path to, List<String> content) throws IOException {
        Files.createDirectories(to.getParent());
        Files.write(to, content, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    void export(Path to, String content) throws IOException {
        Files.createDirectories(to.getParent());
        Files.write(to, Arrays.asList(StringUtils.split(content, "\r\n")), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    void export(Path to, Resource resource) throws IOException {
        Files.createDirectories(to.getParent());

        try (
                InputStream in = resource.asInputStream();
                OutputStream out = Files.newOutputStream(to, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
        ) {
            copy(in, out);
        }
    }

    String toFileName(String resourceName) {
        try {
            return StringUtils.replaceEach(
                    URLEncoder.encode(resourceName, "UTF-8"),
                    new String[]{".", "%2F"},
                    new String[]{"%2E", "-"}
            );
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected: UTF-8 encoding not supported", e);
        }
    }

}