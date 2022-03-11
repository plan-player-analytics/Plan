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

import com.djrapitops.plan.delivery.rendering.pages.Page;
import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.web.ResourceService;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

/**
 * Handles exporting of /players page html, data and resources.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayersPageExporter extends FileExporter {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final PageFactory pageFactory;
    private final RootJSONResolver jsonHandler;
    private final Theme theme;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;

    @Inject
    public PlayersPageExporter(
            PlanFiles files,
            DBSystem dbSystem,
            PageFactory pageFactory,
            RootJSONResolver jsonHandler,
            Theme theme,
            ServerInfo serverInfo
    ) {
        this.files = files;
        this.dbSystem = dbSystem;
        this.pageFactory = pageFactory;
        this.jsonHandler = jsonHandler;
        this.theme = theme;
        this.serverInfo = serverInfo;

        exportPaths = new ExportPaths();
    }

    public void export(Path toDirectory) throws IOException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;

        exportPaths.put("href=\"/\"", "href=\"" + toRelativePathFromRoot(serverInfo.getServer().isProxy() ? "network" : "server") + '"');
        exportRequiredResources(toDirectory);
        exportJSON(toDirectory);
        exportHtml(toDirectory);
        exportPaths.clear();
    }

    private void exportHtml(Path toDirectory) throws IOException {
        Path to = toDirectory
                .resolve("players")
                .resolve("index.html");

        Page page = pageFactory.playersPage();

        // Fixes refreshingJsonRequest ignoring old data of export
        String html = StringUtils.replaceEach(page.toHtml(),
                new String[]{
                        "}, 'playerlist', true);",
                        "<head>"
                },
                new String[]{
                        "}, 'playerlist');",
                        "<head><style>.refresh-element {display: none;}</style>"
                });

        export(to, exportPaths.resolveExportPaths(html));
    }

    private void exportJSON(Path toDirectory) throws IOException {
        Optional<Response> found = getJSONResponse("players");
        if (!found.isPresent()) {
            throw new NotFoundException("players page was not properly exported: not found");
        }

        String jsonResourceName = toFileName(toJSONResourceName("players")) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replace(found.get().getAsString(), "../player", toRelativePathFromRoot("player"))
        );
        exportPaths.put("./v1/players", toRelativePathFromRoot("data/" + jsonResourceName));
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "server="}, new String[]{"-", "_", "", ""});
    }

    private Optional<Response> getJSONResponse(String resource) {
        try {
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + resource, null, Collections.emptyMap()));
        } catch (WebException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e.toString(), e);
        }
    }

    private void exportRequiredResources(Path toDirectory) throws IOException {
        // Style
        exportResources(toDirectory,
                "img/Flaticon_circle.png",
                "css/sb-admin-2.css",
                "css/style.css",
                "css/noauth.css",
                "vendor/datatables/datatables.min.js",
                "vendor/datatables/datatables.min.css",
                "vendor/fontawesome-free/css/all.min.css",
                "vendor/fontawesome-free/webfonts/fa-brands-400.eot",
                "vendor/fontawesome-free/webfonts/fa-brands-400.ttf",
                "vendor/fontawesome-free/webfonts/fa-brands-400.woff",
                "vendor/fontawesome-free/webfonts/fa-brands-400.woff2",
                "vendor/fontawesome-free/webfonts/fa-regular-400.eot",
                "vendor/fontawesome-free/webfonts/fa-regular-400.ttf",
                "vendor/fontawesome-free/webfonts/fa-regular-400.woff",
                "vendor/fontawesome-free/webfonts/fa-regular-400.woff2",
                "vendor/fontawesome-free/webfonts/fa-solid-900.eot",
                "vendor/fontawesome-free/webfonts/fa-solid-900.ttf",
                "vendor/fontawesome-free/webfonts/fa-solid-900.woff",
                "vendor/fontawesome-free/webfonts/fa-solid-900.woff2",
                "js/sb-admin-2.js",
                "js/xmlhttprequests.js",
                "js/color-selector.js"
        );
    }

    private void exportResources(Path toDirectory, String... resourceNames) throws IOException {
        for (String resourceName : resourceNames) {
            exportResource(toDirectory, resourceName);
        }
    }

    private void exportResource(Path toDirectory, String resourceName) throws IOException {
        WebResource resource = ResourceService.getInstance().getResource("Plan", resourceName,
                () -> files.getResourceFromJar("web/" + resourceName).asWebResource());
        Path to = toDirectory.resolve(resourceName);

        if (resourceName.endsWith(".css") || resourceName.endsWith("color-selector.js")) {
            export(to, theme.replaceThemeColors(resource.asString()));
        } else if (Resource.isTextResource(resourceName)) {
            export(to, resource.asString());
        } else {
            export(to, resource);
        }

        exportPaths.put(resourceName, toRelativePathFromRoot(resourceName));
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Players html is exported at /players/index.html or /server/index.html
        return "../" + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(resourceName, "../");
    }

}