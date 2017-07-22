package main.java.com.djrapitops.plan.utilities.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

/**
 *
 * @author Rsl1122
 * @since 3.4.0
 */
public class ExportUtility {

    /**
     *
     * @return @throws IOException
     */
    public static File getFolder() throws IOException {
        String path = Settings.ANALYSIS_EXPORT_PATH.toString();
        if (path.contains(":")) {
            File folder = new File(path);
            if (folder.exists()) {
                if (folder.isDirectory()) {
                    return folder;
                }
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
     *
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
     *
     * @param folder
     * @return
     */
    public static File getPlayersFolder(File folder) {
        File playersFolder = new File(folder, "player");
        playersFolder.mkdirs();
        return playersFolder;
    }

    /**
     *
     * @param userData
     * @param playersFolder
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeInspectHtml(UserData userData, File playersFolder) throws FileNotFoundException, IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        String inspectHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getHtmlStringFromResource("player.html"),
                PlaceholderUtils.getInspectReplaceRules(userData));
        File playerFolder = new File(playersFolder, userData.getName());
        playerFolder.mkdir();
        File inspectHtmlFile = new File(playerFolder, "index.html");
        if (inspectHtmlFile.exists()) {
            inspectHtmlFile.delete();
        }
        Files.write(inspectHtmlFile.toPath(), Arrays.asList(inspectHtml));
    }

    /**
     *
     * @param analysisData
     * @param serverFolder
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeAnalysisHtml(AnalysisData analysisData, File serverFolder) throws FileNotFoundException, IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        String analysisHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getHtmlStringFromResource("analysis.html"),
                PlaceholderUtils.getAnalysisReplaceRules(analysisData))
                .replace(HtmlUtils.getInspectUrl(""), "../player/");
        File analysisHtmlFile = new File(serverFolder, "index.html");
        if (analysisHtmlFile.exists()) {
            analysisHtmlFile.delete();
        }
        Files.write(analysisHtmlFile.toPath(), Arrays.asList(analysisHtml));
    }

    private static void writePlayersPageHtml(List<UserData> rawData, File playersfolder) throws IOException {
        String playersHtml = PlayersPageResponse.buildContent(rawData);
        playersfolder.mkdirs();
        File playersHtmlFile = new File(playersfolder, "index.html");
        Files.write(playersHtmlFile.toPath(), Arrays.asList(new String[]{playersHtml}));
    }

}
