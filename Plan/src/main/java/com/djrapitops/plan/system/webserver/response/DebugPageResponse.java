/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.ServerVariableHolder;
import com.djrapitops.plan.systems.info.server.BungeeServerInfoManager;
import com.djrapitops.plan.systems.info.server.ServerInfo;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

/**
 * WebServer response for /debug-page used for easing issue reporting.
 *
 * @author Rsl1122
 */
public class DebugPageResponse extends ErrorResponse {

    public DebugPageResponse() {
        super.setHeader("HTTP/1.1 200 OK");
        super.setTitle(Html.FONT_AWESOME_ICON.parse("bug") + " Debug Information");
        super.setParagraph(buildParagraph());
        replacePlaceholders();
    }

    private String buildParagraph() {
        StringBuilder content = new StringBuilder();

        String issueLink = Html.LINK_EXTERNAL.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/new", "Create new issue on Github");
        // Information
        content.append("<p>")
                .append(issueLink).append("<br><br>")
                .append("This page contains debug information for an issue ticket.<br>You can copy it directly into the issue, the info is pre-formatted.")
                .append("</p>");

        appendServerInformation(content);

        if (Check.isBungeeAvailable()) {
            appendBungeeConfiguration(content);
        }

        appendLoggedErrors(content);
        appendDebugLog(content);
        appendBenchmarks(content);
        appendConfig(content);

        return content.toString();
    }

    private void appendServerInformation(StringBuilder content) {
        PlanPlugin plugin = PlanPlugin.getInstance();
        ServerVariableHolder variable = plugin.getVariable();

        content.append("<pre>### Server Information<br>")
                .append("**Plan Version:** ").append(plugin.getVersion()).append("<br>");

        content.append("**Server:** ");
        content.append(variable.getName())
                .append(" ").append(variable.getImplVersion())
                .append(" ").append(variable.getVersion());
        content.append("<br>");

        content.append("**Database:** ").append(plugin.getDB().getName());
        try {
            content.append(" schema v").append(plugin.getDB().getVersion());
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        content.append("<br><br>");

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        Properties properties = System.getProperties();

        String osName = properties.getProperty("os.name");
        String osVersion = properties.getProperty("os.version");
        String osArch = properties.getProperty("os.arch");

        String javaVendor = properties.getProperty("java.vendor");
        String javaVersion = properties.getProperty("java.version");

        String javaVMVendor = properties.getProperty("java.vm.vendor");
        String javaVMName = properties.getProperty("java.vm.name");
        String javaVMVersion = properties.getProperty("java.vm.version");
        List<String> javaVMFlags = runtimeMxBean.getInputArguments();

        content.append("**Operating SubSystem:** ").append(osName).append(" (").append(osArch)
                .append(") version ").append(osVersion).append("<br>");

        content.append("**Java Version:** ").append(javaVersion).append(", ").append(javaVendor).append("<br>");
        content.append("**Java VM Version:** ").append(javaVMName).append(" version ").append(javaVMVersion)
                .append(", ").append(javaVMVendor).append("<br>");
        content.append("**Java VM Flags:** ").append(javaVMFlags).append("<br>");

        content.append("</pre>");
    }

    private void appendConfig(StringBuilder content) {
        try {
            File configFile = new File(PlanPlugin.getInstance().getDataFolder(), "config.yml");
            if (configFile.exists()) {
                content.append("<pre>### config.yml<br>&#96;&#96;&#96;<br>");
                FileUtil.lines(configFile, Charset.forName("UTF-8"))
                        .stream().filter(line -> !line.toLowerCase().contains("pass"))
                        .forEach(line -> content.append(line).append("<br>"));
                content.append("&#96;&#96;&#96;</pre>");
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void appendBungeeConfiguration(StringBuilder content) {

        PlanBungee plugin = PlanBungee.getInstance();
        BungeeServerInfoManager serverInfoManager = plugin.getServerInfoManager();
        Collection<ServerInfo> online = serverInfoManager.getOnlineBukkitServers();
        Collection<ServerInfo> bukkitServers = serverInfoManager.getBukkitServers();

        if (!bukkitServers.isEmpty()) {
            content.append("<p>If your issue is about Bungee-Bukkit connection relations, please include the following debug information of available servers as well:</p>");
            for (ServerInfo info : bukkitServers) {
                String link = Html.LINK.parse(info.getWebAddress() + "/debug", info.getWebAddress() + "/debug");
                content.append("<p>").append(link).append("</p>");
            }
        }

        content.append("<pre>### Bungee Configuration<br>");

        content.append("Server name | Online | Address | UUID<br>")
                .append("-- | -- | -- | --<br>");
        for (ServerInfo info : bukkitServers) {
            content.append(info.getName()).append(" | ")
                    .append(online.contains(info) ? "Online" : "Offline").append(" | ")
                    .append(info.getWebAddress()).append(" | ")
                    .append(info.getUuid()).append("<br>");
        }

        content.append("</pre>");
    }

    private void appendBenchmarks(StringBuilder content) {
        content.append("<pre>### Benchmarks<br>&#96;&#96;&#96;<br>");
        try {
            for (String line : Benchmark.getAverages().asStringArray()) {
                content.append(line).append("<br>");
            }
        } catch (Exception e) {
            content.append("Exception on Benchmark.getAverages().asStringArray()");
        }
        content.append("&#96;&#96;&#96;</pre>");
    }

    private void appendLoggedErrors(StringBuilder content) {
        try {
            content.append("<pre>### Logged Errors<br>");

            TreeMap<String, List<String>> errors = PlanPlugin.getInstance().getInfoManager().getErrors();

            if (!errors.isEmpty()) {
                List<String> errorLines = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : errors.entrySet()) {
                    StringBuilder errorLineBuilder = new StringBuilder();
                    for (String line : entry.getValue()) {
                        errorLineBuilder.append(line).append("<br>");
                    }
                    String error = errorLineBuilder.toString();
                    if (!errorLines.contains(error)) {
                        errorLines.add(error);
                    }
                }
                for (String error : errorLines) {
                    content.append("&#96;&#96;&#96;<br>")
                            .append(error)
                            .append("&#96;&#96;&#96;<br>")
                            .append("- [ ] Fixed<br>");
                }
            } else {
                content.append("**No Errors logged.**<br>");
            }
            content.append("</pre>");
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void appendDebugLog(StringBuilder content) {
        content.append("<pre>### Debug Log<br>&#96;&#96;&#96;<br>");
        for (String line : Log.getDebugLogInMemory()) {
            content.append(line).append("<br>");
        }
        content.append("&#96;&#96;&#96;</pre>");
    }
}