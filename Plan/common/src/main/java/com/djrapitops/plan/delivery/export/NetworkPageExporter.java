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
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Handles exporting of /network page html, data and resources.
 *
 * @author Rsl1122
 */
@Singleton
public class NetworkPageExporter extends FileExporter {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final PageFactory pageFactory;
    private final RootJSONResolver jsonHandler;
    private final Locale locale;
    private final Theme theme;

    private final ExportPaths exportPaths;

    @Inject
    public NetworkPageExporter(
            PlanFiles files,
            DBSystem dbSystem,
            PageFactory pageFactory,
            RootJSONResolver jsonHandler,
            Locale locale,
            Theme theme
    ) {
        this.files = files;
        this.dbSystem = dbSystem;
        this.pageFactory = pageFactory;
        this.jsonHandler = jsonHandler;
        this.locale = locale;
        this.theme = theme;

        exportPaths = new ExportPaths();
    }

    public void export(Path toDirectory, Server server) throws IOException, NotFoundException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;

        exportPaths.put("./players", toRelativePathFromRoot("players"));
        exportRequiredResources(toDirectory);
        exportJSON(toDirectory, server);
        exportHtml(toDirectory);
        exportPaths.clear();
    }

    private void exportHtml(Path toDirectory) throws IOException {
        Path to = toDirectory
                .resolve("network")
                .resolve("index.html");

        Page page = pageFactory.networkPage();
        export(to, exportPaths.resolveExportPaths(locale.replaceLanguageInHtml(page.toHtml())));
    }

    public void exportJSON(Path toDirectory, Server server) throws IOException, NotFoundException {
        String serverUUID = server.getUuid().toString();

        exportJSON(toDirectory,
                "network/overview",
                "network/servers",
                "network/sessionsOverview",
                "network/playerbaseOverview",
                "graph?type=playersOnline&server=" + serverUUID,
                "graph?type=uniqueAndNew",
                "graph?type=serverPie",
                "graph?type=activity",
                "graph?type=geolocation",
                "graph?type=uniqueAndNew",
                "network/pingTable",
                "sessions"
        );
    }

    private void exportJSON(Path toDirectory, String... resources) throws NotFoundException, IOException {
        for (String resource : resources) {
            exportJSON(toDirectory, resource);
        }
    }

    private void exportJSON(Path toDirectory, String resource) throws NotFoundException, IOException {
        Response_old found = getJSONResponse(resource);
        if (found instanceof ErrorResponse) {
            throw new NotFoundException(resource + " was not properly exported: " + found.getContent());
        }

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        String relativePlayerLink = toRelativePathFromRoot("player");
        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replaceEach(found.getContent(),
                        new String[]{"../player", "./player"},
                        new String[]{relativePlayerLink, relativePlayerLink}
                )
        );
        exportPaths.put("./v1/" + resource, toRelativePathFromRoot("data/" + jsonResourceName));
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "server="}, new String[]{"-", "_", "", ""});
    }

    private Response_old getJSONResponse(String resource) {
        try {
            return jsonHandler.resolve(null, new RequestTarget(URI.create(resource)));
        } catch (WebException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e.toString(), e);
        }
    }

    private void exportRequiredResources(Path toDirectory) throws IOException {
        exportResources(toDirectory,
                "./img/Flaticon_circle.png",
                "./css/sb-admin-2.css",
                "./css/style.css",
                "./vendor/jquery/jquery.min.js",
                "./vendor/bootstrap/js/bootstrap.bundle.min.js",
                "./vendor/jquery-easing/jquery.easing.min.js",
                "./vendor/datatables/jquery.dataTables.min.js",
                "./vendor/datatables/dataTables.bootstrap4.min.js",
                "./vendor/highcharts/highstock.js",
                "./vendor/highcharts/map.js",
                "./vendor/highcharts/world.js",
                "./vendor/highcharts/drilldown.js",
                "./vendor/highcharts/highcharts-more.js",
                "./vendor/highcharts/no-data-to-display.js",
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
                "./js/sb-admin-2.js",
                "./js/xmlhttprequests.js",
                "./js/color-selector.js",
                "./js/sessionAccordion.js",
                "./js/pingTable.js",
                "./js/graphs.js",
                "./js/network-values.js"
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
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);

        if (resourceName.endsWith(".css")) {
            export(to, theme.replaceThemeColors(resource.asString()));
        } else if ("js/network-values.js".equalsIgnoreCase(resourceName) || "js/sessionAccordion.js".equalsIgnoreCase(resourceName)) {
            String relativePlayerLink = toRelativePathFromRoot("player");
            String relativeServerLink = toRelativePathFromRoot("server/");
            export(to, StringUtils.replaceEach(resource.asString(),
                    new String[]{"../player", "./player", "./server/", "server/"},
                    new String[]{relativePlayerLink, relativePlayerLink, relativeServerLink, relativeServerLink}
            ));
        } else if (Resource.isTextResource(resourceName)) {
            export(to, resource.asLines());
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