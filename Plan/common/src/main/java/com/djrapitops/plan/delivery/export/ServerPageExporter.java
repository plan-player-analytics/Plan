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

import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.rendering.pages.ServerPage;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONHandler;
import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Handles exporting of /server page html, data and resources.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerPageExporter extends FileExporter {

    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final RootJSONHandler jsonHandler;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;

    @Inject
    public ServerPageExporter(
            PlanFiles files,
            PageFactory pageFactory,
            RootJSONHandler jsonHandler,
            ServerInfo serverInfo // To know if current server is a Proxy
    ) {
        this.files = files;
        this.pageFactory = pageFactory;
        this.jsonHandler = jsonHandler;
        this.serverInfo = serverInfo;

        exportPaths = new ExportPaths();
    }

    public void export(Path toDirectory, Server server) throws IOException, NotFoundException, ParseException {
        exportRequiredResources(toDirectory);
        exportJSON(toDirectory, server);
        exportHtml(toDirectory, server);
    }

    private void exportHtml(Path toDirectory, Server server) throws IOException, NotFoundException, ParseException {
        UUID serverUUID = server.getUuid();
        Path to = toDirectory
                .resolve(serverInfo.getServer().isProxy() ? "server/" + server.getName() : "server")
                .resolve("index.html");

        ServerPage serverPage = pageFactory.serverPage(serverUUID);
        export(to, exportPaths.resolveExportPaths(serverPage.toHtml()));
    }

    private void exportJSON(Path toDirectory, Server server) throws IOException, NotFoundException {
        String serverName = server.getName();

        exportJSON(toDirectory, "serverOverview?server=" + serverName);
        exportJSON(toDirectory, "onlineOverview?server=" + serverName);
        exportJSON(toDirectory, "sessionsOverview?server=" + serverName);
        exportJSON(toDirectory, "playerVersus?server=" + serverName);
        exportJSON(toDirectory, "playerbaseOverview?server=" + serverName);
        exportJSON(toDirectory, "performanceOverview?server=" + serverName);
        exportJSON(toDirectory, "graph?type=performance&server=" + serverName);
        exportJSON(toDirectory, "graph?type=aggregatedPing&server=" + serverName);
        exportJSON(toDirectory, "graph?type=worldPie&server=" + serverName);
        exportJSON(toDirectory, "graph?type=activity&server=" + serverName);
        exportJSON(toDirectory, "graph?type=geolocation&server=" + serverName);
        exportJSON(toDirectory, "graph?type=uniqueAndNew&server=" + serverName);
        exportJSON(toDirectory, "graph?type=serverCalendar&server=" + serverName);
        exportJSON(toDirectory, "graph?type=punchCard&server=" + serverName);
        exportJSON(toDirectory, "players?server=" + serverName);
        exportJSON(toDirectory, "kills?server=" + serverName);
        exportJSON(toDirectory, "pingTable?server=" + serverName);
        exportJSON(toDirectory, "sessions?server=" + serverName);
    }

    private void exportJSON(Path toDirectory, String resource) throws NotFoundException, IOException {
        Response found = getJSONResponse(resource);
        if (found instanceof ErrorResponse) {
            throw new NotFoundException(resource + " was not properly exported: " + found.getContent());
        }

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName), found.getContent());
        exportPaths.put("../v1/" + resource, toRelativePathFromRoot("data/" + jsonResourceName));
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "server="}, new String[]{"-", "_", "", ""});
    }

    private Response getJSONResponse(String resource) {
        try {
            return jsonHandler.getResponse(null, new RequestTarget(URI.create(resource)));
        } catch (WebException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e.toString(), e);
        }
    }

    private void exportRequiredResources(Path toDirectory) throws IOException {
        // Style
        exportResource(toDirectory, "css/sb-admin-2.css");
        exportResource(toDirectory, "css/style.css");
        exportImage(toDirectory, "img/Flaticon_circle.png");

        // Plugins
        exportResource(toDirectory, "vendor/jquery/jquery.min.js");
        exportResource(toDirectory, "vendor/bootstrap/js/bootstrap.bundle.min.js");
        exportResource(toDirectory, "vendor/jquery-easing/jquery.easing.min.js");
        exportResource(toDirectory, "vendor/datatables/jquery.dataTables.min.js");
        exportResource(toDirectory, "vendor/datatables/dataTables.bootstrap4.min.js");
        exportResource(toDirectory, "vendor/highcharts/highstock.js");
        exportResource(toDirectory, "vendor/highcharts/map.js");
        exportResource(toDirectory, "vendor/highcharts/world.js");
        exportResource(toDirectory, "vendor/highcharts/drilldown.js");
        exportResource(toDirectory, "vendor/highcharts/highcharts-more.js");
        exportResource(toDirectory, "vendor/highcharts/no-data-to-display.js");
        exportResource(toDirectory, "vendor/fullcalendar/fullcalendar.min.css");
        exportResource(toDirectory, "vendor/momentjs/moment.js");
        exportResource(toDirectory, "vendor/fullcalendar/fullcalendar.min.js");

        // Page level plugins
        exportResource(toDirectory, "js/sb-admin-2.js");
        exportResource(toDirectory, "js/xmlhttprequests.js");
        exportResource(toDirectory, "js/color-selector.js");

        // Page level scripts
        exportResource(toDirectory, "js/sessionAccordion.js");
        exportResource(toDirectory, "js/pingTable.js");
        exportResource(toDirectory, "js/graphs.js");
        exportResource(toDirectory, "js/server-values.js");
    }

    private void exportResource(Path toDirectory, String resourceName) throws IOException {
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);
        export(to, resource.asLines());

        exportPaths.put(resourceName, toRelativePathFromRoot(resourceName));
    }

    private void exportImage(Path toDirectory, String resourceName) throws IOException {
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);
        export(to, resource);

        exportPaths.put(resourceName, toRelativePathFromRoot(resourceName));
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Server html is exported at /server/<name>/index.html or /server/index.html
        return (serverInfo.getServer().isProxy() ? "../../" : "../") + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(resourceName, "../");
    }

}