package main.java.com.djrapitops.plan.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.Html;

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
    public static String replacePlaceholders(String html, Map<String, String> replaceMap) {
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

    /**
     *
     * @param pluginNames
     * @param placeholders
     * @return
     */
    public static String getPluginsTabLayout(List<String> pluginNames, Map<String, List<String>> placeholders) {
        boolean sizeIsEvenNumber = pluginNames.size() % 2 == 0;
        StringBuilder html = new StringBuilder();
        String temp = "";
        int evenSize = pluginNames.size() - (pluginNames.size() % 2);
        for (int i = 0; i < evenSize; i++) {
            String name = pluginNames.get(i);
            if (i % 2 == 0) {
                temp = Html.COLUMN_DIV_WRAPPER.parse(getContent(name, placeholders.get(name)));
            } else {
                html.append(Html.COLUMNS_DIV_WRAPPER.parse(temp + Html.COLUMN_DIV_WRAPPER.parse(getContent(name, placeholders.get(name)))));
            }
        }
        if (!sizeIsEvenNumber) {
            int lastIndex = pluginNames.size() - 1;
            String name = pluginNames.get(lastIndex);
            html.append(Html.COLUMNS_DIV_WRAPPER.parse(Html.COLUMN_DIV_WRAPPER.parse(getContent(name, placeholders.get(name)))+Html.COLUMN_DIV_WRAPPER.parse("")));
        }
        String returnValue = html.toString();
        if (returnValue.isEmpty()) {
            return Html.COLUMNS_DIV_WRAPPER.parse(
                    Html.COLUMN_DIV_WRAPPER.parse(
                            Html.PLUGIN_DATA_WRAPPER.parse(
                                    Html.NO_PLUGINS.parse()
                            )
                    )
            );
        }
        return returnValue;
    }

    private static String getContent(String name, List<String> placeholders) {
        StringBuilder html = new StringBuilder();
        html.append(Html.HEADER.parse(name));
        html.append(Html.PLUGIN_CONTAINER_START.parse());
        for (String placeholder : placeholders) {
            html.append(placeholder);
        }
        html.append("</div>");
        return html.toString();
    }
    
    /**
     *
     * @param string
     * @return
     */
    public static String swapColorsToSpan(String string) {
        Html[] replacer = new Html[]{Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
            Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7, Html.COLOR_8, Html.COLOR_9,
            Html.COLOR_a, Html.COLOR_b, Html.COLOR_c, Html.COLOR_d, Html.COLOR_e, Html.COLOR_f};

        for (Html html : replacer) {
            string = string.replaceAll("§" + html.name().charAt(6), html.parse());
        }
        int spans = string.split("<span").length - 1;
        for (int i = 0; i < spans; i++) {
            string = Html.SPAN.parse(string);
        }
        return string.replaceAll("§r", "");
    }
}
