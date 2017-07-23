package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.html.Html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    public static String getServerAnalysisUrlWithProtocol() {
        return Settings.LINK_PROTOCOL.toString() + ":" + getServerAnalysisUrl();
    }

    /**
     *
     * @return
     */
    public static String getServerAnalysisUrl() {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = Plan.getInstance().getVariable().getIp() + ":" + port;
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = /*"http:*/ "//" + ip + "/server";
        return url;
    }

    /**
     *
     * @param playerName
     * @return
     */
    public static String getInspectUrlWithProtocol(String playerName) {
        return Settings.LINK_PROTOCOL.toString() + ":" + getInspectUrl(playerName);
    }

    /**
     *
     * @param playerName
     * @return
     */
    public static String getInspectUrl(String playerName) {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip = Plan.getInstance().getVariable().getIp() + ":" + port;
        boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        if (useAlternativeIP) {
            ip = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        }
        String url = /*"http:*/ "//" + ip + "/player/" + playerName;
        return url;
    }

    public static String getRelativeInspectUrl(String playerName) {
        return "../player/" + playerName;
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
            html.append(Html.COLUMNS_DIV_WRAPPER.parse(Html.COLUMN_DIV_WRAPPER.parse(getContent(name, placeholders.get(name))) + Html.COLUMN_DIV_WRAPPER.parse("")));
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
        placeholders.stream().forEach(html::append);
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

    public static String separateWithQuotes(String... strings) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            build.append("\"");
            build.append(strings[i]);
            build.append("\"");
            if (i < strings.length - 1) {
                build.append(", ");
            }
        }
        return build.toString();
    }
}
