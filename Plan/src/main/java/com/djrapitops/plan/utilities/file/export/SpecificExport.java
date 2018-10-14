/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.file.export;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.api.Check;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
public abstract class SpecificExport implements Runnable {

    private final PlanFiles files;
    private final PlanConfig config;
    private final ServerInfo serverInfo;

    protected final File outputFolder;
    private final boolean usingProxy;

    protected SpecificExport(
            PlanFiles files,
            PlanConfig config,
            ServerInfo serverInfo
    ) {
        this.files = files;
        this.config = config;
        this.serverInfo = serverInfo;
        outputFolder = getFolder();
        usingProxy = Check.isBungeeAvailable() || Check.isVelocityAvailable();
    }

    protected File getFolder() {
        File folder;

        String path = config.getString(Settings.ANALYSIS_EXPORT_PATH);
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

    protected void export(File to, List<String> lines) throws IOException {
        Files.write(to.toPath(), lines, Charset.forName("UTF-8"));
    }

    protected File getServerFolder() {
        File server = new File(outputFolder, "server");
        server.mkdirs();
        return server;
    }

    protected File getPlayerFolder() {
        File player = new File(outputFolder, "player");
        player.mkdirs();
        return player;
    }

    protected void exportAvailablePlayerPage(UUID uuid, String name) throws IOException {
        Response response = ResponseCache.loadResponse(PageId.PLAYER.of(uuid));
        if (response == null) {
            return;
        }

        String html = response.getContent().replace("../", "../../");
        List<String> lines = Arrays.asList(html.split("\n"));

        File htmlLocation = new File(getPlayerFolder(), name.replace(" ", "%20").replace(".", "%2E"));
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");

        export(exportFile, lines);
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
                htmlLocation = new File(outputFolder, "network");
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
