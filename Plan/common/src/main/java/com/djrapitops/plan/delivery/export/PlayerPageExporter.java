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
import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.exceptions.GenerationException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
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
 * Handles exporting of /player page html, data and resources.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerPageExporter extends FileExporter {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final PageFactory pageFactory;
    private final RootJSONResolver jsonHandler;
    private final Locale locale;
    private final Theme theme;

    private final ExportPaths exportPaths;

    @Inject
    public PlayerPageExporter(
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

    public void export(Path toDirectory, UUID playerUUID, String playerName) throws IOException, NotFoundException, GenerationException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;
        if (!dbSystem.getDatabase().query(PlayerFetchQueries.isPlayerRegistered(playerUUID))) return;

        exportPaths.put("../network/", toRelativePathFromRoot("network"));
        exportPaths.put("../server/", toRelativePathFromRoot("server"));
        exportRequiredResources(toDirectory);

        Path playerDirectory = toDirectory.resolve("player/" + toFileName(playerName));
        exportJSON(playerDirectory, playerUUID, playerName);
        exportHtml(playerDirectory, playerUUID);
        exportPaths.clear();
    }

    private void exportHtml(Path playerDirectory, UUID playerUUID) throws IOException, GenerationException, NotFoundException {
        Path to = playerDirectory.resolve("index.html");

        try {
            Page page = pageFactory.playerPage(playerUUID);
            export(to, exportPaths.resolveExportPaths(locale.replaceLanguageInHtml(page.toHtml())));
        } catch (IllegalStateException notFound) {
            throw new NotFoundException(notFound.getMessage());
        }
    }

    private void exportJSON(Path toDirectory, UUID playerUUID, String playerName) throws IOException, NotFoundException {
        exportJSON(toDirectory, "player?player=" + playerUUID, playerName);
    }

    private void exportJSON(Path toDirectory, String resource, String playerName) throws NotFoundException, IOException {
        Response found = getJSONResponse(resource);
        if (found instanceof ErrorResponse) {
            throw new NotFoundException(resource + " was not properly exported: " + found.getContent());
        }

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        export(toDirectory.resolve(jsonResourceName), found.getContent());
        exportPaths.put("../v1/player?player=" + playerName, "./" + jsonResourceName);
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "player="}, new String[]{"-", "_", "", ""});
    }

    private Response getJSONResponse(String resource) {
        try {
            return jsonHandler.resolve(null, new RequestTarget(URI.create(resource)));
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
                "vendor/jquery/jquery.min.js",
                "vendor/bootstrap/js/bootstrap.bundle.min.js",
                "vendor/jquery-easing/jquery.easing.min.js",
                "vendor/datatables/jquery.dataTables.min.js",
                "vendor/datatables/dataTables.bootstrap4.min.js",
                "vendor/highcharts/highstock.js",
                "vendor/highcharts/map.js",
                "vendor/highcharts/world.js",
                "vendor/highcharts/drilldown.js",
                "vendor/highcharts/highcharts-more.js",
                "vendor/highcharts/no-data-to-display.js",
                "vendor/fullcalendar/fullcalendar.min.css",
                "vendor/momentjs/moment.js",
                "vendor/fullcalendar/fullcalendar.min.js",
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
                "js/color-selector.js",
                "js/sessionAccordion.js",
                "js/graphs.js",
                "js/player-values.js"
        );
    }

    private void exportResources(Path toDirectory, String... resourceNames) throws IOException {
        for (String resourceName : resourceNames) {
            exportResource(toDirectory, resourceName);
        }
    }

    private void exportResource(Path toDirectory, String resourceName) throws IOException {
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);

        if (resourceName.endsWith(".css")) {
            export(to, theme.replaceThemeColors(resource.asString()));
        } else if (Resource.isTextResource(resourceName)) {
            export(to, resource.asLines());
        } else {
            export(to, resource);
        }

        exportPaths.put(resourceName, toRelativePathFromRoot(resourceName));
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Player html is exported at /player/<name>/index.html
        return "../../" + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(resourceName, "../");
    }

}