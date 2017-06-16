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
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

/**
 *
 * @author Rsl1122
 * @since 3.4.0
 */
public class ExportUtility {

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

    public static void export(Plan plugin, AnalysisData analysisData, List<UserData> rawData) {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        Benchmark.start("Exporting Html pages");
        try {
            File folder = getFolder();
            writeAnalysisHtml(analysisData, new File(folder, "server"));
            File playersFolder = getPlayersFolder(folder);
            for (UserData userData : rawData) {
                writeInspectHtml(userData, playersFolder);
            }
        } catch (IOException ex) {
            Log.toLog("ExportUtils.export", ex);
        } finally {
            Benchmark.stop("Exporting Html pages");
        }
    }

    public static File getPlayersFolder(File folder) {
        File playersFolder = new File(folder, "player");
        playersFolder.mkdirs();
        return playersFolder;
    }

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

    public static void writeAnalysisHtml(AnalysisData analysisData, File serverFolder) throws FileNotFoundException, IOException {
        if (!Settings.ANALYSIS_EXPORT.isTrue()) {
            return;
        }
        String analysisHtml = HtmlUtils.replacePlaceholders(HtmlUtils.getHtmlStringFromResource("analysis.html"),
                PlaceholderUtils.getAnalysisReplaceRules(analysisData))
                .replace(HtmlUtils.getInspectUrl(""), "./player/");
        File analysisHtmlFile = new File(serverFolder, "index.html");
        if (analysisHtmlFile.exists()) {
            analysisHtmlFile.delete();
        }
        Files.write(analysisHtmlFile.toPath(), Arrays.asList(analysisHtml));
    }

}
