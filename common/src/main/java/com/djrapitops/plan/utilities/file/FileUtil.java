package com.djrapitops.plan.utilities.file;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getStringFromResource(String fileName) throws IOException {
        StringBuilder html = new StringBuilder();
        PlanPlugin plugin = PlanHelper.getInstance();

        lines(PlanHelper.getInstance(), new File(plugin.getDataFolder(), fileName.replace("/", File.separator)), fileName)
                .forEach(line -> html.append(line).append("\r\n"));
        return html.toString();
    }

    public static List<String> lines(PlanPlugin plugin, File savedFile, String defaults) throws IOException {
        if (savedFile.exists()) {
            return lines(savedFile);
        } else {
            String fileName = savedFile.getName();
            File found = attemptToFind(fileName, new File(plugin.getDataFolder(), "web"));
            if (found != null) {
                return lines(found);
            }
        }
        return lines(plugin, defaults);
    }

    /**
     * Breadth-First search through the file tree to find the file.
     *
     * @param fileName   Name of the searched file
     * @param dataFolder Folder to look from
     * @return File if found or null
     */
    private static File attemptToFind(String fileName, File dataFolder) {
        if (dataFolder.exists() && dataFolder.isDirectory()) {
            ArrayDeque<File> que = new ArrayDeque<>();
            que.add(dataFolder);

            while (!que.isEmpty()) {
                File file = que.pop();
                if (file.isFile() && fileName.equals(file.getName())) {
                    return file;
                }
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        que.addAll(Arrays.asList(files));
                    }
                }
            }
        }
        return null;
    }

    public static List<String> lines(PlanPlugin plugin, String resource) throws IOException {
        List<String> lines = new ArrayList<>();
        Scanner scanner = null;
        try (InputStream inputStream = plugin.getResource(resource)) {
            scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (NullPointerException e) {
            Log.infoColor("§ea Resource was not found inside the jar (" + resource + "), Plan does not support /reload or updates using " +
                    "Plugin Managers, restart the server and see if the error persists.");
            throw new FileNotFoundException("File not found inside jar: " + resource);
        } finally {
            MiscUtils.close(scanner);
        }
        return lines;
    }

    public static List<String> lines(File file) throws IOException {
        return lines(file, StandardCharsets.UTF_8);
    }

    public static List<String> lines(File file, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        if (Verify.exists(file)) {
            try (Stream<String> linesStream = Files.lines(file.toPath(), charset)) {
                lines = linesStream.collect(Collectors.toList());
            }
        }
        return lines;
    }
}