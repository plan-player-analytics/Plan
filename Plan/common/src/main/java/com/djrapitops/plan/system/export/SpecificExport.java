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
package com.djrapitops.plan.system.export;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.api.Check;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Abstract Html Export Task.
 *
 * @author Rsl1122
 */
public abstract class SpecificExport {

    private final PlanFiles files;
    private final ServerInfo serverInfo;

    private final boolean usingProxy;

    protected SpecificExport(
            PlanFiles files,
            ServerInfo serverInfo
    ) {
        this.files = files;
        this.serverInfo = serverInfo;
        usingProxy = Check.isBungeeAvailable() || Check.isVelocityAvailable();
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
        Files.write(to.toPath(), lines, StandardCharsets.UTF_8);
    }

    protected File getServerFolder() {
        File server = new File(getFolder(), "server");
        server.mkdirs();
        return server;
    }

    protected File getPlayerFolder() {
        File player = new File(getFolder(), "player");
        player.mkdirs();
        return player;
    }

    protected void exportPlayerPage(String playerName, String html) throws IOException {
        List<String> lines = Arrays.asList(html.split("\n"));

        File htmlLocation = new File(getPlayerFolder(), playerName.replace(" ", "%20").replace(".", "%2E"));
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");

        export(exportFile, lines);
    }

    protected void exportAvailablePlayerPage(UUID playerUUID, String name) throws IOException {
        Response response = ResponseCache.loadResponse(PageId.PLAYER.of(playerUUID));
        if (response == null) {
            return;
        }

        String html = response.getContent().replace("../", "../../");
        exportPlayerPage(name, html);
    }

    protected void exportAvailableServerPage(UUID serverUUID, String serverName) throws IOException {

        Response response = ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));
        if (response == null) {
            return;
        }

        String html = response.getContent()
                .replace("href=\"plugins/", "href=\"../plugins/")
                .replace("href=\"css/", "href=\"../css/")
                .replace("src=\"plugins/", "src=\"../plugins/")
                .replace("src=\"js/", "src=\"../js/");

        File htmlLocation;
        if (usingProxy) {
            if (serverUUID.equals(serverInfo.getServerUUID())) {
                htmlLocation = new File(getFolder(), "network");
            } else {
                htmlLocation = new File(getServerFolder(), serverName.replace(" ", "%20").replace(".", "%2E"));
                html = html.replace("../", "../../");
            }
        } else {
            htmlLocation = getServerFolder();
        }
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");

        List<String> lines = Arrays.asList(html.split("\n"));

        export(exportFile, lines);
    }
}
