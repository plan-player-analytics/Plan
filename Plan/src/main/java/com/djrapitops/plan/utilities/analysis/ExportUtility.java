package main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

import java.io.File;
import java.io.FileNotFoundException;
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

    /**
     * @return
     */
    public static File getFolder() {
        String path = Settings.ANALYSIS_EXPORT_PATH.toString();

        Log.debug("Export", "Path: " + path);
        boolean isAbsolute = Paths.get(path).isAbsolute();
        Log.debug("Export", "Absolute: " + (isAbsolute ? "Yes" : "No"));
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

    /**
     * @param plugin
     * @param analysisData
     * @param rawData
     */
    public static void export(Plan plugin, AnalysisData analysisData, List<UserData> rawData) {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        Benchmark.start("Exporting Html pages");
        try {
            File folder = getFolder();
            Log.debug("Export", "Folder: " + folder.getAbsolutePath());
            writePlayersPageHtml(rawData, new File(folder, "players"));
            writeAnalysisHtml(analysisData, new File(folder, "server"));
            File playersFolder = getPlayersFolder(folder);
            Log.debug("Export", "Player html files.");
            Log.debug("Export", "Player Page Folder: " + playersFolder.getAbsolutePath());
            String playerHtml = HtmlUtils.getStringFromResource("player.html");
            rawData.forEach(userData -> writeInspectHtml(userData, playersFolder, playerHtml));
        } catch (IOException ex) {
            Log.toLog("ExportUtils.export", ex);
        } finally {
            Benchmark.stop("Export", "Exporting Html pages");
            Log.logDebug("Export");
        }
    }

    /**
     * @param folder
     * @return
     */
    public static File getPlayersFolder(File folder) {
        File playersFolder = new File(folder, "player");
        playersFolder.mkdirs();
        return playersFolder;
    }

    /**
     * @param userData
     * @param playersFolder
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean writeInspectHtml(UserData userData, File playersFolder, String playerHtml) {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return false;
        }
        String name = userData.getName();
        if (name.endsWith(".")) {
            name = name.replace(".", "%2E");
        }
        if (name.endsWith(" ")) {
            name = name.replace(" ", "%20");
        }

        try {
            String inspectHtml = HtmlUtils.replacePlaceholders(playerHtml,
                    PlaceholderUtils.getInspectReplaceRules(userData));
            File playerFolder = new File(playersFolder, name);
            playerFolder.mkdirs();
            File inspectHtmlFile = new File(playerFolder, "index.html");
            inspectHtmlFile.createNewFile();
            Files.write(inspectHtmlFile.toPath(), Collections.singletonList(inspectHtml));
        } catch (IOException e) {
            Log.toLog("Export.inspectPage: " + name, e);
            return false;
        }
        return true;
    }

    /**
     * @param analysisData
     * @param serverFolder
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeAnalysisHtml(AnalysisData analysisData, File serverFolder) throws IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        serverFolder.mkdirs();
        String analysisHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getStringFromResource("analysis.html"),
                PlaceholderUtils.getAnalysisReplaceRules(analysisData))
                .replace(HtmlUtils.getInspectUrl(""), "../player/");
        File analysisHtmlFile = new File(serverFolder, "index.html");
        Log.debug("Export", "Analysis Page File: " + analysisHtmlFile.getAbsolutePath());
        analysisHtmlFile.createNewFile();
        Files.write(analysisHtmlFile.toPath(), Collections.singletonList(analysisHtml));
    }

    private static void writePlayersPageHtml(List<UserData> rawData, File playersFolder) throws IOException {
        String playersHtml = PlayersPageResponse.buildContent(rawData);
        playersFolder.mkdirs();
        File playersHtmlFile = new File(playersFolder, "index.html");
        Log.debug("Export", "Players Page File: " + playersHtmlFile.getAbsolutePath());
        playersHtmlFile.createNewFile();
        Files.write(playersHtmlFile.toPath(), Collections.singletonList(playersHtml));
    }

}
