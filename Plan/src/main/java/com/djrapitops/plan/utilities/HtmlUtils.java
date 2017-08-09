package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.ui.webserver.WebServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Rsl1122
 */
public class HtmlUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private HtmlUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
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

    /**
     * @param html
     * @param replaceMap
     * @return
     */
    public static String replacePlaceholders(String html, Map<String, Serializable> replaceMap) {
        for (Map.Entry<String, Serializable> entrySet : replaceMap.entrySet()) {
            String placeholder = entrySet.getKey();
            String replacer = entrySet.getValue().toString();

            html = html.replace(placeholder, replacer);
        }

        return html;
    }

    /**
     * @return
     */
    public static String getServerAnalysisUrlWithProtocol() {
        return getProtocol() + ":" + getServerAnalysisUrl();
    }

    /**
     * @return
     */
    public static String getServerAnalysisUrl() {
        String ip = getIP();
        return "//" + ip + "/server";
    }

    /**
     * Used to get the WebServer's IP with Port.
     *
     * @return For example 127.0.0.1:8804
     */
    public static String getIP() {
        int port = Settings.WEBSERVER_PORT.getNumber();
        String ip;
        if (Settings.SHOW_ALTERNATIVE_IP.isTrue()) {
            ip = Settings.ALTERNATIVE_IP.toString().replace("%port%", String.valueOf(port));
        } else {
            ip = Plan.getInstance().getVariable().getIp() + ":" + port;
        }
        return ip;
    }

    private static String getProtocol() {
        WebServer uiServer = Plan.getInstance().getUiServer();
        return uiServer.isEnabled() ? uiServer.getProtocol() : Settings.LINK_PROTOCOL.toString();
    }

    /**
     * @param playerName
     * @return
     */
    public static String getInspectUrlWithProtocol(String playerName) {
        return getProtocol() + ":" + getInspectUrl(playerName);
    }

    /**
     * @param playerName
     * @return
     */
    public static String getInspectUrl(String playerName) {
        String ip = getIP();
        return "//" + ip + "/player/" + playerName;
    }

    public static String getRelativeInspectUrl(String playerName) {
        return "../player/" + playerName;
    }

    /**
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
        placeholders.forEach(html::append);
        html.append("</div>");
        return html.toString();
    }

    /**
     * @param string
     * @return
     */
    public static String swapColorsToSpan(String string) {
        Html[] replacer = new Html[]{Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
                Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7, Html.COLOR_8, Html.COLOR_9,
                Html.COLOR_A, Html.COLOR_B, Html.COLOR_C, Html.COLOR_D, Html.COLOR_E, Html.COLOR_F};

        for (Html html : replacer) {
            string = string.replace("§" + Character.toLowerCase(html.name().charAt(6)), html.parse());
        }

        int spans = string.split("<span").length - 1;
        for (int i = 0; i < spans; i++) {
            string = Html.SPAN.parse(string);
        }

        return string.replace("§r", "");
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
