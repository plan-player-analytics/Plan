package main.java.com.djrapitops.plan.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;

/**
 *
 * @author Rsl1122
 */
public class HtmlUtils {

    /**
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static String getHtmlStringFromResource(String fileName) throws FileNotFoundException {
        Plan plugin = Plan.getInstance();
        File localFile = new File(plugin.getDataFolder(), fileName);
        Scanner scanner = new Scanner(plugin.getResource(fileName));
        if (localFile.exists()) {
            scanner = new Scanner(localFile, "UTF-8");
        }
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        return html;
    }

    /**
     *
     * @param html
     * @param replaceMap
     * @return
     */
    public static String replacePlaceholders(String html, HashMap<String, String> replaceMap) {
        for (String key : replaceMap.keySet()) {
            html = html.replace(key, replaceMap.get(key));
        }
        return html;
    }

    /**
     *
     * @return
     */
    public static String getServerAnalysisUrl() {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = Plan.getInstance().getServer().getIp() + ":" + port;
        String securityCode = Settings.SECURITY_CODE.toString();
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = "http://" + ip + "/" + securityCode + "/server";
        return url;
    }

    /**
     *
     * @param playerName
     * @return
     */
    public static String getInspectUrl(String playerName) {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = Plan.getInstance().getServer().getIp() + ":" + port;
        String securityCode = Settings.SECURITY_CODE.toString();
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = "http://" + ip + "/" + securityCode + "/player/" + playerName;
        return url;
    }

    /**
     *
     * @param string
     * @return
     */
    public static String removeXSS(String string) {
        return string.replace("<!--", "")
                .replace("-->", "")
                .replace("<script>", "")
                .replace("</script>", "");
    }
}
