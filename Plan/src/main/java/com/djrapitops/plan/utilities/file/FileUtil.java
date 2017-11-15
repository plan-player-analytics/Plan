package main.java.com.djrapitops.plan.utilities.file;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getStringFromResource(String fileName) throws IOException {
        StringBuilder html = new StringBuilder();
        IPlan plugin = MiscUtils.getIPlan();

        lines(MiscUtils.getIPlan(), new File(plugin.getDataFolder(), fileName.replace("/", File.separator)), fileName)
                .forEach(line -> html.append(line).append("\r\n"));
        return html.toString();
    }

    public static List<String> lines(IPlan plugin, File savedFile, String defaults) throws IOException {
        if (savedFile.exists()) {
            return lines(savedFile);
        }
        return lines(plugin, defaults);
    }

    public static List<String> lines(IPlan plugin, String resource) throws IOException {
        List<String> lines = new ArrayList<>();
        Scanner scanner = null;
        try (InputStream inputStream = plugin.getResource(resource)) {
            scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (NullPointerException e) {
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
