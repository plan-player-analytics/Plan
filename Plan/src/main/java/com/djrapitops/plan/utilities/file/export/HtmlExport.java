/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.file.export;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.webserver.response.pages.PlayersPageResponse;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

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
public class HtmlExport extends SpecificExport {

    private final PlanPlugin plugin;

    public HtmlExport(PlanPlugin plugin) {
        super("HtmlExportTask");
        this.plugin = plugin;
    }

    public static void exportServer(UUID serverUUID) {
        Optional<String> serverName = Database.getActive().fetch().getServerName(serverUUID);
        serverName.ifPresent(s -> RunnableFactory.createNew(new AnalysisExport(serverUUID, s)).runTaskAsynchronously());
    }

    public static void exportPlayer(UUID playerUUID) {
        String playerName = Database.getActive().fetch().getPlayerName(playerUUID);
        if (playerName != null) {
            RunnableFactory.createNew(new PlayerExport(playerUUID, playerName)).runTaskAsynchronously();
        }
    }

    @Override
    public void run() {
        try {
            if (Check.isBukkitAvailable() && ConnectionSystem.getInstance().isServerAvailable()) {
                return;
            }

            exportCss();
            exportJs();
            exportPlugins();

            exportAvailableServerPages();
            exportAvailablePlayers();
            exportPlayersPage();
        } catch (IOException | DBOpException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            try {
                this.cancel();
            } catch (ConcurrentModificationException | IllegalArgumentException ignore) {
            }
        }
    }

    private void exportPlayersPage() throws IOException {
        PlayersPageResponse playersPageResponse = new PlayersPageResponse();

        String html = playersPageResponse.getContent()
                .replace("href=\"plugins/", "href=\"../plugins/")
                .replace("href=\"css/", "href=\"../css/")
                .replace("src=\"plugins/", "src=\"../plugins/")
                .replace("src=\"js/", "src=\"../js/");
        List<String> lines = Arrays.asList(html.split("\n"));

        File htmlLocation = new File(outputFolder, "players");
        Verify.isTrue(htmlLocation.exists() && htmlLocation.isDirectory() || htmlLocation.mkdirs(),
                () -> new FileNotFoundException("Output folder could not be created at" + htmlLocation.getAbsolutePath()));
        File exportFile = new File(htmlLocation, "index.html");
        export(exportFile, lines);
    }

    private void exportAvailablePlayers() throws IOException {
        for (Map.Entry<UUID, UserInfo> entry : Database.getActive().fetch().getUsers().entrySet()) {
            exportAvailablePlayerPage(entry.getKey(), entry.getValue().getName());
        }
    }

    private void exportAvailableServerPages() throws IOException {
        Map<UUID, String> serverNames = Database.getActive().fetch().getServerNames();

        for (Map.Entry<UUID, String> entry : serverNames.entrySet()) {
            exportAvailableServerPage(entry.getKey(), entry.getValue());
        }
    }

    private void exportCss() {
        String[] resources = new String[]{
                "web/css/main.css",
                "web/css/materialize.css",
                "web/css/style.css",
                "web/css/themes/all-themes.css"
        };
        copyFromJar(resources);
    }

    private void exportJs() {
        String[] resources = new String[]{
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
            String demo = FileUtil.getStringFromResource("web/js/demo.js")
                    .replace("${defaultTheme}", Theme.getValue(ThemeVal.THEME_DEFAULT));
            List<String> lines = Arrays.asList(demo.split("\n"));
            File outputFolder = new File(this.outputFolder, "js");
            Verify.isTrue(outputFolder.exists() && outputFolder.isDirectory() || outputFolder.mkdirs(),
                    () -> new FileNotFoundException("Output folder could not be created at" + outputFolder.getAbsolutePath()));
            export(new File(outputFolder, "demo.js"), lines);
        } catch (IOException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void exportPlugins() {
        String[] resources = new String[]{
                "web/plugins/bootstrap/css/bootstrap.css",
                "web/plugins/node-waves/waves.css",
                "web/plugins/node-waves/waves.js",
                "web/plugins/animate-css/animate.css",
                "web/plugins/jquery-slimscroll/jquery.slimscroll.js",
                "web/plugins/jquery/jquery.min.js",
                "web/plugins/bootstrap/js/bootstrap.js",
                "web/plugins/jquery-datatable/skin/bootstrap/js/dataTables.bootstrap.js",
                "web/plugins/jquery-datatable/jquery.dataTables.js",
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
                Log.toLog(this.getClass(), e);
            }
        }
    }

    private void copyFromJar(String resource) throws IOException {
        String possibleFile = resource.replace("web/", "").replace("/", File.separator);
        List<String> lines = FileUtil.lines(plugin, new File(plugin.getDataFolder(), possibleFile), resource);
        String outputFile = possibleFile.replace("web/", "");
        File to = new File(outputFolder, outputFile);
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
