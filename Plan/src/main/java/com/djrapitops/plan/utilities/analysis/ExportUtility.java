package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.systems.webserver.response.PlayersPageResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author Rsl1122
 * @since 3.4.0
 */
public class ExportUtility {

    /**
     * Constructor used to hide the public constructor
     */
    private ExportUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static File getFolder() {
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

    public static void export(AnalysisData analysisData, List<String> playerNames) {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }

//        Benchmark.start("Export","Exporting Html pages");
//        try {
//            File folder = getFolder();
//            Log.logDebug("Export", "Folder: " + folder.getAbsolutePath());
//
//            writePlayersPageHtml(playerNames, new File(folder, "players"));
//            writeAnalysisHtml(analysisData, new File(folder, "server"));
//
//            File playersFolder = getPlayersFolder(folder);
//            Log.logDebug("Export", "Player html files.");
//            Log.logDebug("Export", "Player Page Folder: " + playersFolder.getAbsolutePath());
//
//            String playerHtml = FileUtil.getStringFromResource("player.html");
//
//            Benchmark.start("Exporting Player pages");
//            playerNames.forEach(userData -> writeInspectHtml(userData, playersFolder, playerHtml));
//            Benchmark.stop("Export", "Exporting Player pages");
//        } catch (IOException ex) {
//            Log.toLog("ExportUtils.export", ex);
//        } finally {
//            Benchmark.stop("Export", "Exporting Html pages");
//            Log.logDebug("Export");
//        }
    }

    public static File getPlayersFolder(File folder) {
        File playersFolder = new File(folder, "player");
        playersFolder.mkdirs();
        return playersFolder;
    }

    public static void writeInspectHtml(UserInfo userInfo, File playersFolder, String playerHtml) {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }

        String name = userInfo.getName();

        if (name.endsWith(".")) {
            name = name.replace(".", "%2E");
        }

        if (name.endsWith(" ")) {
            name = name.replace(" ", "%20");
        }

//        try {
//            String inspectHtml = HtmlUtils.replacePlaceholders(playerHtml,
//                    PlaceholderUtils.getInspectReplaceRules(userInfo));

//            File playerFolder = new File(playersFolder, name);
//            playerFolder.mkdirs();
//
//            File inspectHtmlFile = new File(playerFolder, "index.html");
//            Files.write(inspectHtmlFile.toPath(), Collections.singletonList(inspectHtml));
//        } catch (IOException e) {
//            Log.toLog("Export.writeInspectHtml: " + name, e);
//        }
    }

    public static void writeAnalysisHtml(AnalysisData analysisData, File serverFolder) throws IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        serverFolder.mkdirs();
//        String analysisHtml = HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("server.html"),
//                PlaceholderUtils.getAnalysisReplaceRules(analysisData))
//                .replace(HtmlUtils.getInspectUrl(""), "../player/");
//        File analysisHtmlFile = new File(serverFolder, "index.html");
//        Log.logDebug("Export", "Analysis Page File: " + analysisHtmlFile.getAbsolutePath());
//        Files.write(analysisHtmlFile.toPath(), Collections.singletonList(analysisHtml));
    }

    private static void writePlayersPageHtml(List<String> names, File playersFolder) throws IOException {
        String playersHtml = PlayersPageResponse.buildContent(names);
        playersFolder.mkdirs();
        File playersHtmlFile = new File(playersFolder, "index.html");
        Log.logDebug("Export", "Players Page File: " + playersHtmlFile.getAbsolutePath());
        Files.write(playersHtmlFile.toPath(), Collections.singletonList(playersHtml));
    }

}
