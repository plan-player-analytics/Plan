package main.java.com.djrapitops.plan.utilities.analysis;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
        if (path.contains(":")) {
            File folder = new File(path);
            if (folder.exists()
                    && folder.isDirectory()) {
                return folder;
            }
            folder.mkdirs();
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
        String processName = "Exporting Html pages";
        plugin.processStatus().startExecution(processName);
        try {
            File folder = getFolder();
            writePlayersPageHtml(rawData, new File(folder, "players"));
            writeAnalysisHtml(analysisData, new File(folder, "server"));
            File playersFolder = getPlayersFolder(folder);
            plugin.processStatus().setStatus(processName, "Player html files.");
            for (UserData userData : rawData) {
                writeInspectHtml(userData, playersFolder);
            }
        } catch (IOException ex) {
            Log.toLog("ExportUtils.export", ex);
        } finally {
            plugin.processStatus().finishExecution(processName);
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
    public static void writeInspectHtml(UserData userData, File playersFolder) throws IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        String inspectHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getStringFromResource("player.html"),
                PlaceholderUtils.getInspectReplaceRules(userData));
        File playerFolder = new File(playersFolder, userData.getName());
        playerFolder.mkdir();
        File inspectHtmlFile = new File(playerFolder, "index.html");
        inspectHtmlFile.delete();
        Files.write(inspectHtmlFile.toPath(), Collections.singletonList(inspectHtml));
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
        String analysisHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getStringFromResource("analysis.html"),
                PlaceholderUtils.getAnalysisReplaceRules(analysisData))
                .replace(HtmlUtils.getInspectUrl(""), "../player/");
        File analysisHtmlFile = new File(serverFolder, "index.html");
        analysisHtmlFile.delete();

        Files.write(analysisHtmlFile.toPath(), Collections.singletonList(analysisHtml));
    }

    private static void writePlayersPageHtml(List<UserData> rawData, File playersFolder) throws IOException {
        String playersHtml = PlayersPageResponse.buildContent(rawData);
        playersFolder.mkdirs();
        File playersHtmlFile = new File(playersFolder, "index.html");
        Files.write(playersHtmlFile.toPath(), Collections.singletonList(playersHtml));
    }

}
