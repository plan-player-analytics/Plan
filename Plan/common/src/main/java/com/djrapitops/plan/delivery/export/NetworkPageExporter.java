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
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

/**
 * Handles exporting of /network page html, data and resources.
 *
 * @author AuroraLS3
 */
@Singleton
public class NetworkPageExporter extends FileExporter {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final PageFactory pageFactory;
    private final RootJSONResolver jsonHandler;
    private final Theme theme;

    @Inject
    public NetworkPageExporter(
            PlanFiles files,
            DBSystem dbSystem,
            PageFactory pageFactory,
            RootJSONResolver jsonHandler,
            Theme theme
    ) {
        this.files = files;
        this.dbSystem = dbSystem;
        this.pageFactory = pageFactory;
        this.jsonHandler = jsonHandler;
        this.theme = theme;
    }

    /**
     * Perform export for a network page.
     *
     * @param toDirectory Path to Export directory
     * @param server      Server to export as Network page, {@link Server#isProxy()} assumed true.
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void export(Path toDirectory, Server server) throws IOException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;

        ExportPaths exportPaths = new ExportPaths();
        exportPaths.put("./players", toRelativePathFromRoot("players"));
        exportRequiredResources(exportPaths, toDirectory);
        exportJSON(exportPaths, toDirectory, server);
        exportHtml(exportPaths, toDirectory);
    }

    private void exportHtml(ExportPaths exportPaths, Path toDirectory) throws IOException {
        Path to = toDirectory
                .resolve("network")
                .resolve("index.html");

        Page page = pageFactory.networkPage();

        // Fixes refreshingJsonRequest ignoring old data of export
        String html = StringUtils.replaceEach(page.toHtml(),
                new String[]{"loadPlayersOnlineGraph, 'network-overview', true);",
                        "&middot; Performance",
                        "<head>"
                },
                new String[]{"loadPlayersOnlineGraph, 'network-overview');",
                        "&middot; Performance (Unavailable with Export)",
                        "<head><style>.refresh-element {display: none;}</style>"
                });

        export(to, exportPaths.resolveExportPaths(html));
    }

    /**
     * Perform export for a network page json payload.
     *
     * @param exportPaths Replacement store for player file paths.
     * @param toDirectory Path to Export directory
     * @param server      Server to export as Network page, {@link Server#isProxy()} assumed true.
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void exportJSON(ExportPaths exportPaths, Path toDirectory, Server server) throws IOException {
        String serverUUID = server.getUuid().toString();

        exportJSON(exportPaths, toDirectory,
                "network/overview",
                "network/servers",
                "network/sessionsOverview",
                "network/playerbaseOverview",
                "graph?type=playersOnline&server=" + serverUUID,
                "graph?type=uniqueAndNew",
                "graph?type=hourlyUniqueAndNew",
                "graph?type=serverPie",
                "graph?type=joinAddressPie",
                "graph?type=activity",
                "graph?type=geolocation",
                "graph?type=uniqueAndNew",
                "network/pingTable",
                "sessions"
        );
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, String... resources) throws IOException {
        for (String resource : resources) {
            exportJSON(exportPaths, toDirectory, resource);
        }
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, String resource) throws IOException {
        Optional<Response> found = getJSONResponse(resource);
        if (!found.isPresent()) {
            throw new NotFoundException(resource + " was not properly exported: not found");
        }

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        String relativePlayerLink = toRelativePathFromRoot("player");
        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replaceEach(found.get().getAsString(),
                        new String[]{StringEscapeUtils.escapeJson("../player"), StringEscapeUtils.escapeJson("./player")},
                        new String[]{StringEscapeUtils.escapeJson(relativePlayerLink), StringEscapeUtils.escapeJson(relativePlayerLink)}
                )
        );
        exportPaths.put("./v1/" + resource, toRelativePathFromRoot("data/" + jsonResourceName));
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "server="}, new String[]{"-", "_", "", ""});
    }

    private Optional<Response> getJSONResponse(String resource) {
        try {
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + resource, null, Collections.emptyMap()));
        } catch (WebException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e, e);
        }
    }

    private void exportRequiredResources(ExportPaths exportPaths, Path toDirectory) throws IOException {
        exportResources(exportPaths, toDirectory,
                "./img/Flaticon_circle.png",
                "./css/sb-admin-2.css",
                "./css/style.css",
                "./css/noauth.css",
                "./vendor/datatables/datatables.min.js",
                "./vendor/datatables/datatables.min.css",
                "./vendor/highcharts/modules/map.js",
                "./vendor/highcharts/mapdata/world.js",
                "./vendor/highcharts/modules/drilldown.js",
                "./vendor/highcharts/highcharts.js",
                "./vendor/highcharts/modules/no-data-to-display.js",
                "./vendor/masonry/masonry.pkgd.min.js",
                "./vendor/fontawesome-free/css/all.min.css",
                "./vendor/fontawesome-free/webfonts/fa-brands-400.eot",
                "./vendor/fontawesome-free/webfonts/fa-brands-400.ttf",
                "./vendor/fontawesome-free/webfonts/fa-brands-400.woff",
                "./vendor/fontawesome-free/webfonts/fa-brands-400.woff2",
                "./vendor/fontawesome-free/webfonts/fa-regular-400.eot",
                "./vendor/fontawesome-free/webfonts/fa-regular-400.ttf",
                "./vendor/fontawesome-free/webfonts/fa-regular-400.woff",
                "./vendor/fontawesome-free/webfonts/fa-regular-400.woff2",
                "./vendor/fontawesome-free/webfonts/fa-solid-900.eot",
                "./vendor/fontawesome-free/webfonts/fa-solid-900.ttf",
                "./vendor/fontawesome-free/webfonts/fa-solid-900.woff",
                "./vendor/fontawesome-free/webfonts/fa-solid-900.woff2",
                "./js/domUtils.js",
                "./js/sb-admin-2.js",
                "./js/xmlhttprequests.js",
                "./js/color-selector.js",
                "./js/sessionAccordion.js",
                "./js/pingTable.js",
                "./js/graphs.js",
                "./js/network-values.js"
        );
    }

    private void exportResources(ExportPaths exportPaths, Path toDirectory, String... resourceNames) throws IOException {
        for (String resourceName : resourceNames) {
            String nonRelativePath = toNonRelativePath(resourceName);
            exportResource(toDirectory, nonRelativePath);
            exportPaths.put(resourceName, toRelativePathFromRoot(nonRelativePath));
        }
    }

    private void exportResource(Path toDirectory, String resourceName) throws IOException {
        WebResource resource = ResourceService.getInstance().getResource("Plan", resourceName,
                () -> files.getResourceFromJar("web/" + resourceName).asWebResource());
        Path to = toDirectory.resolve(resourceName);

        if (resourceName.endsWith(".css") || resourceName.endsWith("color-selector.js")) {
            export(to, theme.replaceThemeColors(resource.asString()));
        } else if ("js/network-values.js".equalsIgnoreCase(resourceName) || "js/sessionAccordion.js".equalsIgnoreCase(resourceName)) {
            String relativePlayerLink = toRelativePathFromRoot("player");
            String relativeServerLink = toRelativePathFromRoot("server/");
            export(to, StringUtils.replaceEach(resource.asString(),
                    new String[]{"../player", "./player", "./server/", "server/"},
                    new String[]{relativePlayerLink, relativePlayerLink, relativeServerLink, relativeServerLink}
            ));
        } else if (Resource.isTextResource(resourceName)) {
            export(to, resource.asString());
        } else {
            export(to, resource);
        }
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Network html is exported at /network//index.html or /server/index.html
        return "../" + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(StringUtils.remove(resourceName, "../"), "./");
    }

}