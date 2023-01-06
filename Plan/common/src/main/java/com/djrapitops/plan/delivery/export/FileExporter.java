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

import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * Code that handles writing the actual files that are exported.
 *
 * @author AuroraLS3
 */
abstract class FileExporter {

    private static final OpenOption[] OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] bytes = new byte[1024];

        while ((read = in.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
    }

    void export(Path to, List<String> content) throws IOException {
        Path dir = to.getParent();
        if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
        Files.write(to, content, StandardCharsets.UTF_8, OPEN_OPTIONS);
    }

    void export(Path to, String content) throws IOException {
        export(to, Arrays.asList(StringUtils.split(content, "\r\n")));
    }

    void export(Path to, Resource resource) throws IOException {
        export(to, resource.asWebResource());
    }

    void export(Path to, WebResource resource) throws IOException {
        Path dir = to.getParent();
        if (!Files.isSymbolicLink(dir) && !Files.isDirectory(dir)) {
            Files.createDirectories(dir);
        }

        try (
                InputStream in = resource.asStream();
                OutputStream out = Files.newOutputStream(to, OPEN_OPTIONS)
        ) {
            copy(in, out);
        }
    }

    void export(Path to, byte[] resource) throws IOException {
        Path dir = to.getParent();
        if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);

        try (
                InputStream in = new ByteArrayInputStream(resource);
                OutputStream out = Files.newOutputStream(to, OPEN_OPTIONS)
        ) {
            copy(in, out);
        }
    }

    String toFileName(String resourceName) {
        return StringUtils.replaceEach(
                Html.encodeToURL(resourceName),
                new String[]{".", "%2F", "%20"},
                new String[]{"%2E", "-", " "}
        );
    }

    void exportReactRedirects(Path toDirectory, PlanFiles files, PlanConfig config, String[] redirections) throws IOException {
        String redirectPageHtml = files.getResourceFromJar("web/export-redirect.html").asString();
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
        placeholderReplacer.put("PLAN_ADDRESS", config.get(WebserverSettings.EXTERNAL_LINK));
        redirectPageHtml = placeholderReplacer.apply(redirectPageHtml);

        for (String redirection : redirections) {
            exportReactRedirect(toDirectory, redirectPageHtml, redirection);
        }
    }

    private void exportReactRedirect(Path toDirectory, String redirectHtml, String path) throws IOException {
        export(toDirectory.resolve(path).resolve("index.html"), redirectHtml);
    }

}