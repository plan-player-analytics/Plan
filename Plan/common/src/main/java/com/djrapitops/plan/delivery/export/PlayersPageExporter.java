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
import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONHandler;
import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * Handles exporting of /players page html, data and resources.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayersPageExporter extends FileExporter {

    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final RootJSONHandler jsonHandler;
    private final Locale locale;
    private final Theme theme;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;

    @Inject
    public PlayersPageExporter(
            PlanFiles files,
            PageFactory pageFactory,
            RootJSONHandler jsonHandler,
            Locale locale,
            Theme theme,
            ServerInfo serverInfo
    ) {
        this.files = files;
        this.pageFactory = pageFactory;
        this.jsonHandler = jsonHandler;
        this.locale = locale;
        this.theme = theme;
        this.serverInfo = serverInfo;

        exportPaths = new ExportPaths();
    }

    public void export(Path toDirectory) throws IOException, NotFoundException, ParseException {
        exportPaths.put("/", toRelativePathFromRoot(serverInfo.getServer().isProxy() ? "network" : "server"));
        exportRequiredResources(toDirectory);
        exportJSON(toDirectory);
        exportHtml(toDirectory);
    }

    private void exportHtml(Path toDirectory) throws IOException, ParseException {
        Path to = toDirectory
                .resolve("players")
                .resolve("index.html");

        Page page = pageFactory.playersPage();
        export(to, exportPaths.resolveExportPaths(locale.replaceMatchingLanguage(page.toHtml())));
    }

    private void exportJSON(Path toDirectory) throws NotFoundException, IOException {
        Response found = getJSONResponse("players");
        if (found instanceof ErrorResponse) {
            throw new NotFoundException("players page was not properly exported: " + found.getContent());
        }

        String jsonResourceName = toFileName(toJSONResourceName("players")) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName), found.getContent());
        exportPaths.put("../v1/players", toRelativePathFromRoot("data/" + jsonResourceName));
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
        exportImage(toDirectory, "img/Flaticon_circle.png");

        // Style
        exportResources(toDirectory,
                "css/sb-admin-2.css",
                "css/style.css",
                "vendor/jquery/jquery.min.js",
                "vendor/bootstrap/js/bootstrap.bundle.min.js",
                "vendor/jquery-easing/jquery.easing.min.js",
                "vendor/datatables/jquery.dataTables.min.js",
                "vendor/datatables/dataTables.bootstrap4.min.js",
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
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);

        if (resourceName.endsWith(".css")) {
            export(to, theme.replaceThemeColors(resource.asString()));
        } else {
            export(to, resource.asLines());
        }

        exportPaths.put(resourceName, toRelativePathFromRoot(resourceName));
    }

    private void exportImage(Path toDirectory, String resourceName) throws IOException {
        Resource resource = files.getCustomizableResourceOrDefault("web/" + resourceName);
        Path to = toDirectory.resolve(resourceName);
        export(to, resource);

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