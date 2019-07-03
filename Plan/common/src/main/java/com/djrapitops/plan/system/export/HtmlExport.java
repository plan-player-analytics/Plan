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

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.BaseUser;
import com.djrapitops.plan.db.access.queries.objects.BaseUserQueries;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.json.JSONFactory;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.ExportSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.NetworkPageResponse;
import com.djrapitops.plan.utilities.html.pages.InspectPage;
import com.djrapitops.plan.utilities.html.pages.NetworkPage;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Class responsible for Html Export task.
 *
 * @author Rsl1122
 */
@Singleton
public class HtmlExport extends SpecificExport {

    private final PlanConfig config;
    private final Theme theme;
    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final PageFactory pageFactory;
    private final ConnectionSystem connectionSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public HtmlExport(
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            PageFactory pageFactory,
            JSONFactory jsonFactory,
            ServerInfo serverInfo,
            ConnectionSystem connectionSystem,
            ErrorHandler errorHandler
    ) {
        super(files, jsonFactory, serverInfo);
        this.config = config;
        this.theme = theme;
        this.files = files;
        this.dbSystem = dbSystem;
        this.pageFactory = pageFactory;
        this.connectionSystem = connectionSystem;
        this.errorHandler = errorHandler;
    }

    @Override
    protected String getPath() {
        return config.get(ExportSettings.HTML_EXPORT_PATH);
    }

    public void exportServer(UUID serverUUID) {
        if (serverInfo.getServer().isNotProxy() && connectionSystem.isServerAvailable()) {
            return;
        }
        dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .map(Server::getName)
                .ifPresent(serverName -> {
                    try {
                        exportAvailableServerPage(serverUUID, serverName);
                    } catch (IOException e) {
                        errorHandler.log(L.WARN, this.getClass(), e);
                    }
                });
    }

    public void exportPlayerPage(UUID playerUUID) {
        Optional<String> name = dbSystem.getDatabase().query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID));
        exportPlayerPage(playerUUID, name.orElse("Unknown"));
    }

    public void exportPlayerPage(UUID playerUUID, String playerName) {
        InspectPage playerPage = pageFactory.inspectPage(playerUUID);
        try {
            exportPlayerPage(playerName, playerPage.toHtml());
        } catch (ParseException | IOException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    public void exportCachedPlayerPage(UUID playerUUID) {
        if (serverInfo.getServer().isNotProxy() && connectionSystem.isServerAvailable()) {
            return;
        }

        dbSystem.getDatabase().query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID))
                .ifPresent(playerName -> {
                    try {
                        exportAvailablePlayerPage(playerUUID, playerName);
                    } catch (IOException e) {
                        errorHandler.log(L.WARN, this.getClass(), e);
                    }
                });
    }

    public void exportPlayersPage() {
        try {
            String html = pageFactory.playersPage().toHtml()
                    .replace("href=\"plugins/", "href=\"../plugins/")
                    .replace("href=\"css/", "href=\"../css/")
                    .replace("src=\"plugins/", "src=\"../plugins/")
                    .replace("src=\"js/", "src=\"../js/");
            List<String> lines = Arrays.asList(html.split("\n"));

            File htmlLocation = new File(getFolder(), "players");
            Verify.isTrue(htmlLocation.exists() && htmlLocation.isDirectory() || htmlLocation.mkdirs(),
                    () -> new FileNotFoundException("Output folder could not be created at" + htmlLocation.getAbsolutePath()));
            File exportFile = new File(htmlLocation, "index.html");
            export(exportFile, lines);
        } catch (IOException | DBOpException | ParseException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public void exportAvailablePlayers() {
        try {
            Collection<BaseUser> users = dbSystem.getDatabase().query(BaseUserQueries.fetchAllBaseUsers());
            for (BaseUser user : users) {
                exportAvailablePlayerPage(user.getUuid(), user.getName());
            }
        } catch (IOException | DBOpException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public void cacheNetworkPage() {
        if (serverInfo.getServer().isNotProxy()) {
            return;
        }

        NetworkPage networkPage = pageFactory.networkPage();
        ResponseCache.cacheResponse(PageId.SERVER.of(serverInfo.getServerUUID()), () -> {
            try {
                return new NetworkPageResponse(networkPage);
            } catch (ParseException e) {
                errorHandler.log(L.WARN, this.getClass(), e);
                return null;
            }
        });
    }

    public void exportNetworkPage() {
        if (serverInfo.getServer().isNotProxy()) {
            return;
        }

        cacheNetworkPage();
        try {
            exportAvailableServerPage(serverInfo.getServerUUID(), serverInfo.getServer().getName());
        } catch (IOException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public void exportAvailableServerPages() {
        try {
            Map<UUID, String> serverNames = dbSystem.getDatabase().query(ServerQueries.fetchServerNames());

            for (Map.Entry<UUID, String> entry : serverNames.entrySet()) {
                exportAvailableServerPage(entry.getKey(), entry.getValue());
            }
        } catch (IOException | DBOpException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public void exportCss() {
        String[] resources = new String[]{
                "web/css/main.css",
                "web/css/materialize.css",
                "web/css/style.css",
                "web/css/themes/all-themes.css"
        };
        copyFromJar(resources);
    }

    public void exportJs() {
        String[] resources = new String[]{
                "web/js/demo.js",
                "web/js/admin.js",
                "web/js/helpers.js",
                "web/js/script.js",
                "web/js/charts/activityPie.js",
                "web/js/charts/lineGraph.js",
                "web/js/charts/horizontalBarGraph.js",
                "web/js/charts/stackGraph.js",
                "web/js/charts/performanceGraph.js",
                "web/js/charts/playerGraph.js",
                "web/js/charts/playerGraphNoNav.js",
                "web/js/charts/resourceGraph.js",
                "web/js/charts/diskGraph.js",
                "web/js/charts/tpsGraph.js",
                "web/js/charts/worldGraph.js",
                "web/js/charts/worldMap.js",
                "web/js/charts/punchCard.js",
                "web/js/charts/serverPie.js",
                "web/js/charts/worldPie.js",
                "web/js/charts/healthGauge.js",
                "web/js/charts/sessionCalendar.js",
                "web/js/charts/onlineActivityCalendar.js"
        };
        copyFromJar(resources);

        try {
            String demo = files.getCustomizableResourceOrDefault("web/js/demo.js")
                    .asString()
                    .replace("${defaultTheme}", theme.getValue(ThemeVal.THEME_DEFAULT));
            List<String> lines = Arrays.asList(demo.split("\n"));
            File outputFolder = new File(getFolder(), "js");
            Verify.isTrue(outputFolder.exists() && outputFolder.isDirectory() || outputFolder.mkdirs(),
                    () -> new FileNotFoundException("Output folder could not be created at " + outputFolder.getAbsolutePath()));
            export(new File(outputFolder, "demo.js"), lines);
        } catch (IOException e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public void exportPlugins() {
        String[] resources = new String[]{
                "web/plugins/node-waves/waves.css",
                "web/plugins/node-waves/waves.js",
                "web/plugins/animate-css/animate.css",
                "web/plugins/jquery-slimscroll/jquery.slimscroll.js",
                "web/plugins/jquery/jquery.min.js",
                "web/plugins/fullcalendar/fullcalendar.min.js",
                "web/plugins/fullcalendar/fullcalendar.min.css",
                "web/plugins/momentjs/moment.js",
        };
        copyFromJar(resources);
    }

    private void copyFromJar(String[] resources) {
        for (String resource : resources) {
            try {
                copyFromJar(resource);
            } catch (IOException e) {
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        }
    }

    private void copyFromJar(String resource) throws IOException {
        List<String> lines = files.getCustomizableResourceOrDefault(resource).asLines();

        File to = new File(getFolder(), resource.replace("web/", "").replace("/", File.separator));
        File locationFolder = to.getParentFile();

        Verify.isTrue(locationFolder.exists() && locationFolder.isDirectory() || locationFolder.mkdirs(),
                () -> new FileNotFoundException("Output folder could not be created at" + locationFolder.getAbsolutePath()));

        if (to.exists()) {
            Files.delete(to.toPath());
            if (!to.createNewFile()) {
                return;
            }
        }

        export(to, lines);
    }
}
