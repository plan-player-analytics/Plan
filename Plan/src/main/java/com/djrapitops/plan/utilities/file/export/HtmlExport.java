/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.file.export;

import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class responsible for Html Export task.
 *
 * @author Rsl1122
 */
public class HtmlExport extends AbsRunnable {

    private final IPlan plugin;
    private final boolean usingBungee;
    private final boolean exportSpecificPage;

    private String specificPage;

    private final File outputFolder;

    public HtmlExport(IPlan plugin) {
        super("HtmlExportTask");
        usingBungee = Check.isBungeeAvailable();
        this.plugin = plugin;

        outputFolder = getFolder();
        exportSpecificPage = false;
    }

    public HtmlExport(IPlan plugin, String specificPage) {
        super("HtmlExportTask");
        usingBungee = Check.isBungeeAvailable();
        this.plugin = plugin;

        outputFolder = getFolder();
        exportSpecificPage = true;
        this.specificPage = specificPage;
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
        } catch (IOException | SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            this.cancel();
        }
    }

    private void exportAvailableServerPages() throws SQLException, IOException {
        Map<UUID, String> serverNames = plugin.getDB().getServerTable().getServerNames();

        for (Map.Entry<UUID, String> entry : serverNames.entrySet()) {
            UUID serverUUID = entry.getKey();

            Response response = PageCache.loadPage("analysisPage:" + serverUUID);
            if (response == null) {
                continue;
            }

            String html = response.getContent()
                    .replace("href=\"plugins/", "href=\"../plugins/")
                    .replace("href=\"css/", "href=\"../css/")
                    .replace("src=\"plugins/", "src=\"../plugins/")
                    .replace("src=\"js/", "src=\"../js/");

            File htmlLocation = null;
            if (usingBungee && serverUUID.equals(MiscUtils.getIPlan().getServerUuid())) {
                htmlLocation = new File(outputFolder, "network");
            } else {
                String serverName = entry.getValue();
                File serverFolder = getServerFolder();
                htmlLocation = new File(serverFolder, serverName.replace(" ", "%20"));
                html = html.replace("../", "../../");
            }
            htmlLocation.mkdirs();
            File exportFile = new File(htmlLocation, "index.html");

            List<String> lines = Arrays.asList(html.split("\n"));

            export(exportFile, lines);
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
                "web/js/demo.js",
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
        copyFromJar(resources);
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
        copyFromJar(resources);
    }

    public File getFolder() {
        String path = Settings.ANALYSIS_EXPORT_PATH.toString();

        Log.logDebug("Export", "Path: " + path);
        boolean isAbsolute = Paths.get(path).isAbsolute();
        Log.logDebug("Export", "Absolute: " + (isAbsolute ? "Yes" : "No"));
        if (isAbsolute) {
            File folder = new File(path);
            if (!folder.exists() || !folder.isDirectory()) {
                folder.mkdirs();
            }
            return folder;
        }
        File dataFolder = Plan.getInstance().getDataFolder();
        File folder = new File(dataFolder, path);
        folder.mkdirs();
        return folder;
    }

    private void copyFromJar(String[] resources) {
        for (String resource : resources) {
            try {
                copyFromJar(resource);
            } catch (IOException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }

    private void copyFromJar(String resource) throws IOException {
        String possibleFile = resource.replace("web/", "").replace("/", File.separator);
        List<String> lines = FileUtil.lines(plugin, new File(plugin.getDataFolder(), possibleFile), resource);
        String outputFile = possibleFile.replace("web/", "");
        File to = new File(outputFolder, outputFile);
        to.mkdirs();
        if (to.exists()) {
            to.delete();
            to.createNewFile();
        }
        export(to, lines);
    }

    private void export(File to, List<String> lines) throws IOException {
        Files.write(to.toPath(), lines, Charset.forName("UTF-8"));
    }

    public File getServerFolder() {
        File server = new File(outputFolder, "server");
        server.mkdirs();
        return server;
    }
}