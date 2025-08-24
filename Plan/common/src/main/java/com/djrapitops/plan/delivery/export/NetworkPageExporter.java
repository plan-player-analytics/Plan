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

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
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
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final RootJSONResolver jsonHandler;

    @Inject
    public NetworkPageExporter(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            RootJSONResolver jsonHandler
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.jsonHandler = jsonHandler;
    }

    public static String[] getRedirections() {
        return new String[]{
                "network",
                "network/overview",
                "network/serversOverview",
                "network/sessions",
                "network/playerbase",
                "network/join-addresses",
                "network/retention",
                "network/players",
                "network/geolocations",
                "network/plugins-overview",
        };
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
        exportJSON(exportPaths, toDirectory, server);
        exportReactRedirects(toDirectory);
    }

    private void exportReactRedirects(Path toDirectory) throws IOException {
        exportReactRedirects(toDirectory, files, config, getRedirections());
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
                "graph?type=playersOnlineProxies",
                "graph?type=uniqueAndNew",
                "graph?type=hourlyUniqueAndNew",
                "graph?type=serverPie",
                "graph?type=joinAddressByDay",
                "graph?type=activity",
                "graph?type=geolocation",
                "graph?type=uniqueAndNew",
                "graph?type=serverCalendar",
                "network/pingTable",
                "sessions",
                "extensionData?server=" + serverUUID,
                "retention",
                "joinAddresses",
                "playersTable"
        );
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, String... resources) throws IOException {
        for (String resource : resources) {
            exportJSON(exportPaths, toDirectory, resource);
        }
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, String resource) throws IOException {
        Response response = getJSONResponse(resource)
                .orElseThrow(() -> new NotFoundException(resource + " was not properly exported: not found"));

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        String relativePlayerLink = toRelativePathFromRoot("player");
        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replaceEach(response.getAsString(),
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
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + resource, null, Collections.emptyMap(), null));
        } catch (WebUserAuthException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e, e);
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