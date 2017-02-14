package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Plan;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.com.djrapitops.plan.Settings;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class HtmlUtils {

    public static String getHtmlStringFromResource(String fileName) {
        Plan plugin = getPlugin(Plan.class);
        File localFile = new File(plugin.getDataFolder(), fileName);        
        Scanner scanner = new Scanner(plugin.getResource(fileName));
        if (localFile.exists()) {
            try {
                scanner = new Scanner(localFile, "UTF-8");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        return html;
    }

    public static String replacePlaceholders(String html, HashMap<String, String> replaceMap) {
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        return html;
    }

    public static String getServerAnalysisUrl() {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = getPlugin(Plan.class).getServer().getIp() + ":" + port;
        String securityCode = Settings.SECURITY_CODE.toString();
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = "http://" + ip + "/" + securityCode + "/server";
        return url;
    }

    public static String getInspectUrl(String playerName) {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = getPlugin(Plan.class).getServer().getIp() + ":" + port;
        String securityCode = Settings.SECURITY_CODE.toString();
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = "http://" + ip + "/" + securityCode + "/player/" + playerName;
        return url;
    }
}
