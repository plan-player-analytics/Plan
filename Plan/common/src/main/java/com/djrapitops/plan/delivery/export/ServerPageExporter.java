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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
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
 * Handles exporting of /server page html, data and resources.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerPageExporter extends FileExporter {

    private final PlanFiles files;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final RootJSONResolver jsonHandler;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;

    @Inject
    public ServerPageExporter(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            RootJSONResolver jsonHandler,
            ServerInfo serverInfo // To know if current server is a Proxy
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.jsonHandler = jsonHandler;
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
        exportJSON(toDirectory, server);
        exportReactRedirects(toDirectory, server.getUuid());
        exportPaths.clear();
    }

    public static String[] getRedirections(ServerUUID serverUUID) {
        String server = "server/";
        return new String[]{
                server + serverUUID,
                server + serverUUID + "/overview",
                server + serverUUID + "/online-activity",
                server + serverUUID + "/sessions",
                server + serverUUID + "/pvppve",
                server + serverUUID + "/playerbase",
                server + serverUUID + "/join-addresses",
                server + serverUUID + "/retention",
                server + serverUUID + "/allowlist",
                server + serverUUID + "/players",
                server + serverUUID + "/geolocations",
                server + serverUUID + "/performance",
                server + serverUUID + "/plugins-overview",
        };
    }

    private void exportReactRedirects(Path toDirectory, ServerUUID serverUUID) throws IOException {
        exportReactRedirects(toDirectory, files, config, getRedirections(serverUUID));
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
                "graph?type=playersOnline&server=" + serverUUID,
                "graph?type=optimizedPerformance&server=" + serverUUID,
                "graph?type=aggregatedPing&server=" + serverUUID,
                "graph?type=worldPie&server=" + serverUUID,
                "graph?type=activity&server=" + serverUUID,
                "graph?type=geolocation&server=" + serverUUID,
                "graph?type=uniqueAndNew&server=" + serverUUID,
                "graph?type=hourlyUniqueAndNew&server=" + serverUUID,
                "graph?type=joinAddressByDay&server=" + serverUUID,
                "graph?type=serverCalendar&server=" + serverUUID,
                "graph?type=punchCard&server=" + serverUUID,
                "playersTable?server=" + serverUUID,
                "kills?server=" + serverUUID,
                "pingTable?server=" + serverUUID,
                "sessions?server=" + serverUUID,
                "extensionData?server=" + serverUUID,
                "serverIdentity?server=" + serverUUID,
                "retention?server=" + serverUUID,
                "joinAddresses?server=" + serverUUID,
                "gameAllowlistBounces?server=" + serverUUID
        );
    }

    private void exportJSON(Path toDirectory, String... resources) throws IOException {
        for (String resource : resources) {
            exportJSON(toDirectory, resource);
        }
    }

    private void exportJSON(Path toDirectory, String resource) throws IOException {
        Response response = getJSONResponse(resource)
                .orElseThrow(() -> new NotFoundException(resource + " was not properly exported: not found"));

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replace(
                        response.getAsString(),
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
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + resource, null, Collections.emptyMap(), null));
        } catch (WebUserAuthException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e, e);
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