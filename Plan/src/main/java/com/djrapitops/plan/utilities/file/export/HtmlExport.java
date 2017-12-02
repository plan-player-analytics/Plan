/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.file.export;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.container.UserInfo;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.settings.theme.ThemeVal;
import main.java.com.djrapitops.plan.systems.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.PostHtmlWebAPI;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Class responsible for Html Export task.
 *
 * @author Rsl1122
 */
public class HtmlExport extends SpecificExport {

    private final IPlan plugin;

    public HtmlExport(IPlan plugin) {
        super("HtmlExportTask");
        this.plugin = plugin;
    }

    public static void exportServer(IPlan plugin, UUID serverUUID) {
        try {
            Optional<String> serverName = plugin.getDB().getServerTable().getServerName(serverUUID);
            serverName.ifPresent(s -> RunnableFactory.createNew(new AnalysisExport(serverUUID, s)).runTaskAsynchronously());
        } catch (SQLException e) {
            Log.toLog(PostHtmlWebAPI.class.getClass().getName(), e);
        }
    }

    public static void exportPlayer(IPlan plugin, UUID playerUUID) {
        try {
            String playerName = plugin.getDB().getUsersTable().getPlayerName(playerUUID);
            if (playerName != null) {
                RunnableFactory.createNew(new PlayerExport(playerUUID, playerName)).runTaskAsynchronously();
            }
        } catch (SQLException e) {
            Log.toLog(PostHtmlWebAPI.class.getClass().getName(), e);
        }
    }

    @Override
    public void run() {
        try {
            boolean usingAnotherWebServer = plugin.getInfoManager().isUsingAnotherWebServer();
            if (usingAnotherWebServer) {
                return;
            }

            exportCss();
            exportJs();
            exportPlugins();

            exportAvailableServerPages();
            exportAvailablePlayers();
            exportPlayersPage();
        } catch (IOException | SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            this.cancel();
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
        htmlLocation.mkdirs();
        File exportFile = new File(htmlLocation, "index.html");
        export(exportFile, lines);
    }

    private void exportAvailablePlayers() throws SQLException, IOException {
        for (Map.Entry<UUID, UserInfo> entry : plugin.getDB().getUsersTable().getUsers().entrySet()) {
            exportAvailablePlayerPage(entry.getKey(), entry.getValue().getName());
        }
    }

    private void exportAvailableServerPages() throws SQLException, IOException {
        Map<UUID, String> serverNames = plugin.getDB().getServerTable().getServerNames();

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
        copyFromJar(resources, true);
    }

    private void exportJs() {
        String[] resources = new String[]{
                "web/js/admin.js",
                "web/js/helpers.js",
                "web/js/script.js",
                "web/js/charts/activityPie.js",
                "web/js/charts/activityStackGraph.js",
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
                "web/js/charts/healthGauge.js"
        };
        copyFromJar(resources, false);

        try {
            String demo = FileUtil.getStringFromResource("web/js/demo.js")
                    .replace("${defaultTheme}", Theme.getValue(ThemeVal.THEME_DEFAULT));
            List<String> lines = Arrays.asList(demo.split("\n"));
            File outputFolder = new File(this.outputFolder, "js");
            outputFolder.mkdirs();
            export(new File(outputFolder, "demo.js"), lines);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
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
                "web/plugins/jquery-datatable/jquery.dataTables.js"
        };
        copyFromJar(resources, true);
    }


    private void copyFromJar(String[] resources, boolean overwrite) {
        for (String resource : resources) {
            try {
                copyFromJar(resource, overwrite);
            } catch (IOException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }

    private void copyFromJar(String resource, boolean overwrite) throws IOException {
        String possibleFile = resource.replace("web/", "").replace("/", File.separator);
        List<String> lines = FileUtil.lines(plugin, new File(plugin.getDataFolder(), possibleFile), resource);
        String outputFile = possibleFile.replace("web/", "");
        File to = new File(outputFolder, outputFile);
        to.mkdirs();
        if (to.exists()) {
            if (overwrite) {
                to.delete();
                to.createNewFile();
            } else {
                return;
            }
        }
        export(to, lines);
    }
}