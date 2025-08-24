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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
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
    private static final String PLAYERS_TABLE = "playersTable";
    private final DBSystem dbSystem;
    private final RootJSONResolver jsonHandler;
    private final ServerInfo serverInfo;

    private final ExportPaths exportPaths;
    private final PlanConfig config;

    @Inject
    public PlayersPageExporter(
            PlanFiles files,
            PlanConfig config, DBSystem dbSystem,
            RootJSONResolver jsonHandler,
            ServerInfo serverInfo
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.jsonHandler = jsonHandler;
        this.serverInfo = serverInfo;

        exportPaths = new ExportPaths();
    }

    public void export(Path toDirectory) throws IOException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;

        exportPaths.put("href=\"/\"", "href=\"" + toRelativePathFromRoot(serverInfo.getServer().isProxy() ? "network" : "server") + '"');
        exportJSON(toDirectory);
        exportReactRedirects(toDirectory);
        exportPaths.clear();
    }

    private void exportReactRedirects(Path toDirectory) throws IOException {
        String[] redirections = {"players"};
        exportReactRedirects(toDirectory, files, config, redirections);
    }

    private void exportJSON(Path toDirectory) throws IOException {
        Response response = getJSONResponse()
                .orElseThrow(() -> new NotFoundException("players page was not properly exported: not found"));

        String jsonResourceName = toFileName(toJSONResourceName()) + ".json";

        export(toDirectory.resolve("data").resolve(jsonResourceName),
                // Replace ../player in urls to fix player page links
                StringUtils.replace(response.getAsString(), "../player", toRelativePathFromRoot("player"))
        );
        exportPaths.put("./v1/" + PLAYERS_TABLE, toRelativePathFromRoot("data/" + jsonResourceName));
    }

    private String toJSONResourceName() {
        return StringUtils.replaceEach(PLAYERS_TABLE, new String[]{"?", "&", "type=", "server="}, new String[]{"-", "_", "", ""});
    }

    private Optional<Response> getJSONResponse() {
        try {
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + PLAYERS_TABLE, null, Collections.emptyMap(), null));
        } catch (WebUserAuthException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e.toString(), e);
        }
    }

    private String toRelativePathFromRoot(String resourceName) {
        // Players html is exported at /players/index.html or /server/index.html
        return "../" + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return StringUtils.remove(resourceName, "../");
    }

}