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

import com.djrapitops.plan.delivery.domain.datatransfer.OnlineActivityType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
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
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

        exportJSON(toDirectory, server.getUuid());
        exportReactRedirects(toDirectory, server.getUuid());
    }

    private void exportReactRedirects(Path toDirectory, ServerUUID serverUUID) throws IOException {
        exportReactRedirects(toDirectory, files, config, getRedirections(serverUUID));
    }

    /**
     * Perform export for a server page json payload.
     *
     * @param toDirectory Path to Export directory
     * @param serverUUID  Server to export
     * @throws IOException       If a template can not be read from jar/disk or the result written
     * @throws NotFoundException If a file or resource that is being exported can not be found
     */
    public void exportJSON(Path toDirectory, ServerUUID serverUUID) throws IOException {
        long month = TimeUnit.DAYS.toMillis(30L);
        long monthAgo = System.currentTimeMillis() - month;
        long week = TimeUnit.DAYS.toMillis(7L);
        long twoWeeks = week * 2;
        long day = TimeUnit.DAYS.toMillis(1);

        String datapointType = "datapoint?type=";
        String after = "&after=" + monthAgo;
        String server = "&server=" + serverUUID;
        String afterMillis = "&afterMillisAgo=";
        String beforeMillis = "&beforeMillisAgo=";
        exportJSON(toDirectory,
                "onlineInsights?server=" + serverUUID,
                "playerVersus?server=" + serverUUID,
                "playerbaseOverview?server=" + serverUUID,
                "graph?type=playersOnline&server=" + serverUUID,
                "graph?type=optimizedPerformance&server=" + serverUUID,
                "graph?type=aggregatedPing&server=" + serverUUID,
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
                "gameAllowlistBounces?server=" + serverUUID,
                datapointType + DatapointType.PLAYTIME + server,
                datapointType + DatapointType.PLAYTIME + after + server,
                datapointType + DatapointType.PLAYTIME + afterMillis + month + server,
                datapointType + DatapointType.AFK_TIME + after + server,
                datapointType + DatapointType.AFK_TIME + afterMillis + month + server,
                datapointType + DatapointType.AFK_TIME_PERCENTAGE + after + server,
                datapointType + DatapointType.AFK_TIME_PERCENTAGE + afterMillis + month + server,
                datapointType + DatapointType.SERVER_OCCUPIED + afterMillis + month + server,
                datapointType + DatapointType.MOST_ACTIVE_GAME_MODE + afterMillis + month + server,
                datapointType + DatapointType.WORLD_PIE + server,
                datapointType + DatapointType.NEW_PLAYERS + server,
                datapointType + DatapointType.REGULAR_PLAYERS + server,
                datapointType + DatapointType.PLAYERS_ONLINE_PEAK + afterMillis + TimeUnit.DAYS.toMillis(2) + server,
                datapointType + DatapointType.PLAYERS_ONLINE_PEAK + server,
                datapointType + DatapointType.PLAYERS_ONLINE + server,
                datapointType + DatapointType.SESSION_COUNT + server,
                datapointType + DatapointType.SESSION_COUNT + after + server,
                datapointType + DatapointType.SESSION_COUNT + afterMillis + month + server,
                datapointType + DatapointType.PLAYTIME_PER_PLAYER_AVERAGE + server,
                datapointType + DatapointType.SESSION_LENGTH_AVERAGE + server,
                datapointType + DatapointType.PLAYER_KILLS + server,
                datapointType + DatapointType.MOB_KILLS + server,
                datapointType + DatapointType.DEATHS + server,
                datapointType + DatapointType.UPTIME_CURRENT + server,
                datapointType + DatapointType.UNIQUE_PLAYERS_AVERAGE + afterMillis + week + server,
                datapointType + DatapointType.NEW_PLAYER_RETENTION + afterMillis + week + server,
                // Week comparison
                datapointType + DatapointType.UNIQUE_PLAYERS_COUNT + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.NEW_PLAYERS + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.REGULAR_PLAYERS + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.PLAYTIME_PER_PLAYER_AVERAGE + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.SESSION_LENGTH_AVERAGE + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.SESSION_COUNT + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.PLAYER_KILLS + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.MOB_KILLS + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.DEATHS + afterMillis + twoWeeks + beforeMillis + TimeUnit.DAYS.toMillis(7L) + server,
                datapointType + DatapointType.UNIQUE_PLAYERS_COUNT + afterMillis + week + server,
                datapointType + DatapointType.NEW_PLAYERS + afterMillis + week + server,
                datapointType + DatapointType.REGULAR_PLAYERS + afterMillis + week + server,
                datapointType + DatapointType.PLAYTIME_PER_PLAYER_AVERAGE + afterMillis + week + server,
                datapointType + DatapointType.SESSION_LENGTH_AVERAGE + afterMillis + week + server,
                datapointType + DatapointType.SESSION_COUNT + afterMillis + week + server,
                datapointType + DatapointType.PLAYER_KILLS + afterMillis + week + server,
                datapointType + DatapointType.MOB_KILLS + afterMillis + week + server,
                datapointType + DatapointType.DEATHS + afterMillis + week + server,
                // Online Activity overview
                datapointType + DatapointType.UNIQUE_PLAYERS_COUNT + afterMillis + day + server,
                datapointType + DatapointType.UNIQUE_PLAYERS_COUNT + afterMillis + month + server,
                datapointType + DatapointType.UNIQUE_PLAYERS_AVERAGE + afterMillis + day + server,
                datapointType + DatapointType.UNIQUE_PLAYERS_AVERAGE + afterMillis + month + server,
                datapointType + DatapointType.NEW_PLAYERS + afterMillis + day + server,
                datapointType + DatapointType.NEW_PLAYERS + afterMillis + month + server,
                datapointType + DatapointType.NEW_PLAYERS_AVERAGE + afterMillis + day + server,
                datapointType + DatapointType.NEW_PLAYERS_AVERAGE + afterMillis + week + server,
                datapointType + DatapointType.NEW_PLAYERS_AVERAGE + afterMillis + month + server,
                datapointType + DatapointType.NEW_PLAYER_RETENTION + afterMillis + day + server,
                datapointType + DatapointType.NEW_PLAYER_RETENTION + afterMillis + month + server,
                datapointType + DatapointType.PLAYTIME + afterMillis + day + server,
                datapointType + DatapointType.PLAYTIME + afterMillis + week + server,
                datapointType + DatapointType.PLAYTIME + afterMillis + month + server,
                datapointType + DatapointType.PLAYTIME_PER_DAY_AVERAGE + afterMillis + day + server,
                datapointType + DatapointType.PLAYTIME_PER_DAY_AVERAGE + afterMillis + week + server,
                datapointType + DatapointType.PLAYTIME_PER_DAY_AVERAGE + afterMillis + month + server,
                datapointType + DatapointType.SESSION_LENGTH_AVERAGE + afterMillis + day + server,
                datapointType + DatapointType.SESSION_LENGTH_AVERAGE + afterMillis + month + server,
                datapointType + DatapointType.SESSION_COUNT + afterMillis + day + server,
                datapointType + DatapointType.SESSION_COUNT + afterMillis + month + server
        );
        // Performance overview
        exportDatapointJSON(toDirectory, DatapointType.TPS_LOW_SPIKES, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_AVERAGE_LOW_TPS, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_MAX_95TH_LOW_TPS, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.DOWNTIME, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.UPTIME, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.PLAYERS_ONLINE_AVERAGE, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.PLAYERS_ONLINE_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_AVERAGE, serverUUID, OnlineActivityType.IDLE);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_IMPACT_PER_PLAYER, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_IMPACT_PER_CHUNK, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_MAX_95TH, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_JITTER_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.MSPT_JITTER_MAX, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.CPU_AVERAGE, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.CPU_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.CPU_AVERAGE, serverUUID, OnlineActivityType.IDLE);
        exportDatapointJSON(toDirectory, DatapointType.CPU_IMPACT_PER_PLAYER, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.RAM_AVERAGE, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.ENTITIES_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.ENTITIES_PER_CHUNK, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.CHUNKS_AVERAGE, serverUUID, OnlineActivityType.ACTIVE);
        exportDatapointJSON(toDirectory, DatapointType.CHUNKS_PER_PLAYER, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.DISK_MAX, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.DISK_MIN, serverUUID);
        exportDatapointJSON(toDirectory, DatapointType.TPS_AVERAGE, serverUUID);
    }

    private void exportDatapointJSON(Path toDirectory, DatapointType type, ServerUUID serverUUID) throws IOException {
        long month = TimeUnit.DAYS.toMillis(30L);
        long week = TimeUnit.DAYS.toMillis(7L);
        long twoWeeks = week * 2;
        long threeWeeks = week * 3;
        long fourWeeks = week * 4;
        long day = TimeUnit.DAYS.toMillis(1);

        String datapointType = "datapoint?type=";
        String server = "&server=" + serverUUID;
        String afterMillis = "&afterMillisAgo=";
        String beforeMillis = "&beforeMillisAgo=";
        exportJSON(toDirectory,
                datapointType + type + afterMillis + day + server,
                datapointType + type + afterMillis + week + server,
                datapointType + type + afterMillis + twoWeeks + beforeMillis + week + server,
                datapointType + type + afterMillis + threeWeeks + beforeMillis + twoWeeks + server,
                datapointType + type + afterMillis + fourWeeks + beforeMillis + threeWeeks + server,
                datapointType + type + afterMillis + month + server
        );
    }

    private void exportDatapointJSON(Path toDirectory, DatapointType type, ServerUUID serverUUID, OnlineActivityType activityType) throws IOException {
        long month = TimeUnit.DAYS.toMillis(30L);
        long week = TimeUnit.DAYS.toMillis(7L);
        long twoWeeks = week * 2;
        long threeWeeks = week * 3;
        long fourWeeks = week * 4;
        long day = TimeUnit.DAYS.toMillis(1);

        String datapointType = "datapoint?type=";
        String server = "&server=" + serverUUID;
        String afterMillis = "&afterMillisAgo=";
        String beforeMillis = "&beforeMillisAgo=";
        String active = "&activityType=" + activityType.name();
        exportJSON(toDirectory,
                datapointType + type + afterMillis + day + server + active,
                datapointType + type + afterMillis + week + server + active,
                datapointType + type + afterMillis + twoWeeks + beforeMillis + week + server + active,
                datapointType + type + afterMillis + threeWeeks + beforeMillis + twoWeeks + server + active,
                datapointType + type + afterMillis + fourWeeks + beforeMillis + threeWeeks + server + active,
                datapointType + type + afterMillis + month + server + active
        );
    }


    private void exportJSON(Path toDirectory, String... resources) throws IOException {
        for (String resource : resources) {
            exportJSON(toDirectory, resource);
        }
    }

    private void exportJSON(Path toDirectory, String resource) throws IOException {
        Optional<Response> response = getJSONResponse(resource);

        String jsonResourceName = toFileName(toJSONResourceName(resource), "json");

        if (response.isPresent()) {
            export(toDirectory.resolve("data").resolve(jsonResourceName),
                    // Replace ../player in urls to fix player page links
                    Strings.CI.replace(
                            response.get().getAsString(),
                            StringEscapeUtils.escapeJson("../player"),
                            StringEscapeUtils.escapeJson(toRelativePathFromRoot("player"))
                    )
            );
        } else {
            Files.deleteIfExists(toDirectory.resolve("data").resolve(jsonResourceName));
        }
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
}