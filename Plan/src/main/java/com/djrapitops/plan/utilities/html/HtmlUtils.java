package com.djrapitops.plan.utilities.html;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;

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
            ip = ServerInfo.getServerProperties().getIp() + ":" + port;
        }
        return ip;
    }

    /**
     * Attempts to remove XSS components.
     *
     * @param string String to remove XSS components from
     * @return String but with the components removed
     */
    public static String removeXSS(String string) {
        return string.replace("<!--", "").replace("-->", "").replace("</script>", "").replace("<script>", "");
    }

    /**
     * Changes Minecraft color codes to HTML span elements with correct color class assignments.
     *
     * @param string String to replace Minecraft color codes from
     * @return String with span elements.
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

        return string.replace("§r", "").replace("§l", "").replace("§m", "").replace("§n", "").replace("§o", "").replace("§k", "");
    }
}
