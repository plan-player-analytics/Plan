package main.java.com.djrapitops.plan.utilities.file;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtil {

    public static String getStringFromResource(String fileName) throws FileNotFoundException {
        InputStream resourceStream = null;
        Scanner scanner = null;
        try {
            Plan plugin = Plan.getInstance();
            File localFile = new File(plugin.getDataFolder(), fileName);

            if (localFile.exists()) {
                scanner = new Scanner(localFile, "UTF-8");
            } else {
                resourceStream = plugin.getResource(fileName);
                scanner = new Scanner(resourceStream);
            }

            StringBuilder html = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                html.append(line).append("\r\n");
            }
            return html.toString();
        } finally {
            MiscUtils.close(resourceStream, scanner);
        }
    }

    public static List<String> lines(JavaPlugin plugin, String resource) throws IOException {
        List<String> lines = new ArrayList<>();
        Scanner scanner = null;
        try (InputStream inputStream = plugin.getResource(resource)) {
            scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } finally {
            MiscUtils.close(scanner);
        }
        return lines;
    }

}
