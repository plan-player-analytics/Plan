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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
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
 * Handles exporting of /server page html, data and resources.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerPageExporter extends FileExporter {

    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final DBSystem dbSystem;
    private final RootJSONResolver jsonHandler;
    private final Theme theme;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;

    @Inject
    public ServerPageExporter(
            PlanFiles files,
            PageFactory pageFactory,
            DBSystem dbSystem,
            RootJSONResolver jsonHandler,
            Theme theme,
            ServerInfo serverInfo // To know if current server is a Proxy
    ) {
        this.files = files;
        this.pageFactory = pageFactory;
        this.dbSystem = dbSystem;
        this.jsonHandler = jsonHandler;
        this.theme = theme;
        this.serverInfo = serverInfo;

        exportPaths = new ExportPaths();
    }

    /**
     * Perform export for a server page.
     *
     * @param toDirectory Path to Export directory
     * @param server      Server to export
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void export(Path toDirectory, Server server) throws IOException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;

        exportPaths.put("../network", toRelativePathFromRoot("network"));
        exportRequiredResources(toDirectory);
        exportJSON(toDirectory, server);
        exportHtml(toDirectory, server);
        exportPaths.clear();
    }

    private void exportHtml(Path toDirectory, Server server) throws IOException {
        ServerUUID serverUUID = server.getUuid();
        Path to = toDirectory
                .resolve(serverInfo.getServer().isProxy() ? "server/" + toFileName(server.getName()) : "server")
                .resolve("index.html");

        Page page = pageFactory.serverPage(serverUUID);

        // Fixes refreshingJsonRequest ignoring old data of export
        String html = StringUtils.replaceEach(page.toHtml(),
                new String[]{
                        "loadOptimizedPerformanceGraph, 'performance', true);",
                        "loadserverCalendar, 'online-activity-overview', true);",
                        "}, 'playerlist', true);",
                        "<head>"
                },
                new String[]{
                        "loadOptimizedPerformanceGraph, 'performance');",
                        "loadserverCalendar, 'online-activity-overview');",
                        "}, 'playerlist');",
                        "<head><style>.refresh-element {display: none;}</style>"
                });

        export(to, exportPaths.resolveExportPaths(html));
    }

    /**
     * Perform export for a server page json payload.
     *
     * @param toDirectory Path to Export directory
     * @param server      Server to export
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void exportJSON(Path toDirectory, Server server) throws IOException {
        String serverUUID = server.getUuid().toString();

        exportJSON(toDirectory,
                "serverOverview?server=" + serverUUID,
                "onlineOverview?server=" + serverUUID,
                "sessionsOverview?server=" + serverUUID,
                "playerVersus?server=" + serverUUID,
                "playerbaseOverview?server=" + serverUUID,
                "performanceOverview?server=" + serverUUID,
                "graph?type=optimizedPerformance&server=" + serverUUID,
                "graph?type=aggregatedPing&server=" + serverUUID,
                "graph?type=worldPie&server=" + serverUUID,
                "graph?type=activity&server=" + serverUUID,
                "graph?type=geolocation&server=" + serverUUID,
                "graph?type=uniqueAndNew&server=" + serverUUID,
                "graph?type=hourlyUniqueAndNew&server=" + serverUUID,
                "graph?type=joinAddressPie&server=" + serverUUID,
                "graph?type=serverCalendar&server=" + serverUUID,
                "graph?type=punchCard&server=" + serverUUID,
                "players?server=" + serverUUID,
                "kills?server=" + serverUUID,
                "pingTable?server=" + serverUUID,
                "sessions?server=" + serverUUID
        );
    }

    private void exportJSON(Path toDirectory, String... resources) throws IOException {
        for (String resource : resources) {
            exportJSON(toDirectory, resource);
        }
    }

    private void exportJSON(Path toDirectory, String resource) throws IOException {
        Optional<Response> found = getJSONResponse(resource);
        if (!found.isPresent()) {
            throw new NotFoundException(resource + " was not properly exported: not found");
        }

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replace(
                        found.get().getAsString(),
                        StringEscapeUtils.escapeJson("../player"),
                        StringEscapeUtils.escapeJson(toRelativePathFromRoot("player"))
                )
        );
        exportPaths.put("../v1/" + resource, toRelativePathFromRoot("data/" + jsonResourceName));
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

    private void exportRequiredResources(Path toDirectory) throws IOException {
        // Style
        exportResources(toDirectory,
                "../img/Flaticon_circle.png",
                "../css/sb-admin-2.css",
                "../css/style.css",
                "../vendor/datatables/datatables.min.js",
                "../vendor/datatables/datatables.min.css",
                "../vendor/highcharts/modules/map.js",
                "../vendor/highcharts/mapdata/world.js",
                "../vendor/highcharts/modules/drilldown.js",
                "../vendor/highcharts/highcharts.js",
                "../vendor/highcharts/modules/no-data-to-display.js",
                "../vendor/fullcalendar/fullcalendar.min.css",
                "../vendor/momentjs/moment.js",
                "../vendor/masonry/masonry.pkgd.min.js",
                "../vendor/fullcalendar/fullcalendar.min.js",
                "../vendor/fontawesome-free/css/all.min.css",
                "../vendor/fontawesome-free/webfonts/fa-brands-400.eot",
                "../vendor/fontawesome-free/webfonts/fa-brands-400.ttf",
                "../vendor/fontawesome-free/webfonts/fa-brands-400.woff",
                "../vendor/fontawesome-free/webfonts/fa-brands-400.woff2",
                "../vendor/fontawesome-free/webfonts/fa-regular-400.eot",
                "../vendor/fontawesome-free/webfonts/fa-regular-400.ttf",
                "../vendor/fontawesome-free/webfonts/fa-regular-400.woff",
                "../vendor/fontawesome-free/webfonts/fa-regular-400.woff2",
                "../vendor/fontawesome-free/webfonts/fa-solid-900.eot",
                "../vendor/fontawesome-free/webfonts/fa-solid-900.ttf",
                "../vendor/fontawesome-free/webfonts/fa-solid-900.woff",
                "../vendor/fontawesome-free/webfonts/fa-solid-900.woff2",
                "../js/domUtils.js",
                "../js/sb-admin-2.js",
                "../js/xmlhttprequests.js",
                "../js/color-selector.js",
                "../js/sessionAccordion.js",
                "../js/pingTable.js",
                "../js/graphs.js",
                "../js/server-values.js"
        );
    }

    private void exportResources(Path toDirectory, String... resourceNames) throws IOException {
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
        } else if (Resource.isTextResource(resourceName)) {
            export(to, resource.asString());
        } else {
            export(to, resource);
        }
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Server html is exported at /server/<name>/index.html or /server/index.html
        return (serverInfo.getServer().isProxy() ? "../../" : "../") + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(StringUtils.remove(resourceName, "../"), "./");
    }

}