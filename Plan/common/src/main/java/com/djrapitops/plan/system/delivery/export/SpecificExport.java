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
package com.djrapitops.plan.system.delivery.export;

import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.json.JSONFactory;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abstract Html Export Task.
 *
 * // TODO Export should check config settings
 *
 * @author Rsl1122
 */
public abstract class SpecificExport {

    private final PlanFiles files;
    private final JSONFactory jsonFactory; // Hacky, TODO export needs a rework
    protected final ServerInfo serverInfo;

    SpecificExport(
            PlanFiles files,
            JSONFactory jsonFactory, ServerInfo serverInfo
    ) {
        this.files = files;
        this.jsonFactory = jsonFactory;
        this.serverInfo = serverInfo;
    }

    protected File getFolder() {
        File folder;

        String path = getPath();
        boolean isAbsolute = Paths.get(path).isAbsolute();
        if (isAbsolute) {
            folder = new File(path);
        } else {
            File dataFolder = files.getDataFolder();
            folder = new File(dataFolder, path);
        }

        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    protected abstract String getPath();

    protected void export(File to, List<String> lines) throws IOException {
        Files.write(to.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    File getServerFolder() {
        File server = new File(getFolder(), "server");
        server.mkdirs();
        return server;
    }

    File getPlayerFolder() {
        File player = new File(getFolder(), "player");
        player.mkdirs();
        return player;
    }

    void exportPlayerPage(String playerName, String html) throws IOException {
        List<String> lines = Arrays.asList(html.replace("../", "../../").split("\n"));

        File htmlLocation = new File(getPlayerFolder(), URLEncoder.encode(playerName, "UTF-8").replace(".", "%2E"));
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");

        export(exportFile, lines);
    }

    void exportAvailablePlayerPage(UUID playerUUID, String name) throws IOException {
        Response response = ResponseCache.loadResponse(PageId.PLAYER.of(playerUUID));
        if (response == null) {
            return;
        }

        String html = response.getContent();
        exportPlayerPage(name, html);
    }

    void exportAvailableServerPage(UUID serverUUID, String serverName) throws IOException {
        // TODO Force export in the future
        Response response = ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));
        if (response == null) {
            return;
        }

        String html = response.getContent()
                .replace("href=\"plugins/", "href=\"../plugins/")
                .replace("href=\"css/", "href=\"../css/")
                .replace("src=\"plugins/", "src=\"../plugins/")
                .replace("src=\"js/", "src=\"../js/")
                .replace("../json/players?serverName=" + serverName, "./players_table.json");

        File htmlLocation;
        if (serverInfo.getServer().isProxy()) {
            if (serverUUID.equals(serverInfo.getServerUUID())) {
                htmlLocation = new File(getFolder(), "network");
            } else {
                htmlLocation = new File(getServerFolder(), URLEncoder.encode(serverName, "UTF-8").replace(".", "%2E"));
                html = html.replace("../", "../../");
                exportPlayersTableJSON(htmlLocation, serverUUID);
            }
        } else {
            htmlLocation = getServerFolder();
            exportPlayersTableJSON(htmlLocation, serverUUID);
        }

        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");

        List<String> lines = Arrays.asList(html.split("\n"));

        export(exportFile, lines);
    }

    private void exportPlayersTableJSON(File htmlLocation, UUID serverUUID) throws IOException {
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "players_table.json");
        export(exportFile, Collections.singletonList(jsonFactory.serverPlayersTableJSON(serverUUID)));
    }
}
