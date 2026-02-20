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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles exporting of /player page html, data and resources.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayerPageExporter extends FileExporter {

    private final PlanFiles files;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final RootJSONResolver jsonHandler;

    @Inject
    public PlayerPageExporter(
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

    public static String[] getRedirections(UUID playerUUID) {
        String player = "player/";
        return new String[]{
                player + playerUUID,
                player + playerUUID + "/overview",
                player + playerUUID + "/sessions",
                player + playerUUID + "/pvppve",
                player + playerUUID + "/servers",
        };
    }

    /**
     * Perform export for a player page.
     *
     * @param toDirectory Path to Export directory
     * @param playerUUID  UUID of the player
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void export(Path toDirectory, UUID playerUUID) throws IOException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState == Database.State.CLOSED || dbState == Database.State.CLOSING) return;
        if (Boolean.FALSE.equals(dbSystem.getDatabase().query(PlayerFetchQueries.isPlayerRegistered(playerUUID)))) {
            return;
        }

        ExportPaths exportPaths = new ExportPaths();
        exportPaths.put("../network", toRelativePathFromRoot("network"));
        exportPaths.put("../server/", toRelativePathFromRoot("server"));

        Path playerDirectory = toDirectory.resolve("player/" + toFileName(playerUUID.toString()));
        exportJSON(exportPaths, playerDirectory, playerUUID);
        exportReactRedirects(toDirectory, playerUUID);
        exportPaths.clear();
    }

    private void exportReactRedirects(Path toDirectory, UUID playerUUID) throws IOException {
        exportReactRedirects(toDirectory, files, config, getRedirections(playerUUID));
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, UUID playerUUID) throws IOException {
        exportJSON(exportPaths, toDirectory, "player?player=" + playerUUID);
    }

    private void exportJSON(ExportPaths exportPaths, Path toDirectory, String resource) throws IOException {
        Response response = getJSONResponse(resource)
                .orElseThrow(() -> new NotFoundException(resource + " was not properly exported: no response"));

        String jsonResourceName = toFileName(toJSONResourceName(resource)) + ".json";

        export(toDirectory.resolve(jsonResourceName), response.getBytes());
        exportPaths.put("../v1/player?player=${encodeURIComponent(playerUUID)}", "./" + jsonResourceName);
    }

    private String toJSONResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&", "type=", "player="}, new String[]{"-", "_", "", ""});
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
        // Player html is exported at /player/<uuid>/index.html
        return "../../" + toNonRelativePath(resourceName);
    }

    private String toNonRelativePath(String resourceName) {
        return Strings.CI.remove(Strings.CI.remove(resourceName, "../"), "./");
    }

}