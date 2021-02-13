/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.html.structure.TabsElement;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.ResourceCache;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;

import java.util.*;

/**
 * Html String generator for /debug page.
 *
 * @author AuroraLS3
 */
public class DebugPage implements Page {

    private final String template;
    private final Database database;
    private final ServerInfo serverInfo;
    private final VersionChecker versionChecker;
    private final CombineDebugLogger debugLogger;
    private final Timings timings;
    private final ErrorLogger errorLogger;

    private final Formatter<Long> yearFormatter;

    DebugPage(
            String htmlTemplate,

            Database database,
            ServerInfo serverInfo,
            Formatters formatters,
            VersionChecker versionChecker,
            DebugLogger debugLogger,
            Timings timings,
            ErrorLogger errorLogger
    ) {
        this.template = htmlTemplate;

        this.database = database;
        this.serverInfo = serverInfo;
        this.versionChecker = versionChecker;
        this.debugLogger = (CombineDebugLogger) debugLogger;
        this.timings = timings;
        this.errorLogger = errorLogger;

        this.yearFormatter = formatters.yearLong();
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("title", Icon.called("bug") + " Debug Information");
        placeholders.put("titleText", "Debug Information");
        placeholders.put("paragraph", createContent());
        placeholders.put("version", versionChecker.getUpdateButton().orElse(versionChecker.getCurrentVersionButton()));
        placeholders.put("updateModal", versionChecker.getUpdateModal());
        placeholders.put("contributors", Contributors.generateContributorHtml());
        return placeholders.apply(template);
    }

    private String createContent() {
        StringBuilder preContent = new StringBuilder();

        String issueLink = Html.LINK_EXTERNAL.create("https://github.com/plan-player-analytics/Plan/issues/new", "Create new issue on Github");
        String hastebinLink = Html.LINK_EXTERNAL.create("https://hastebin.com/", "Create a new hastebin paste");

        preContent.append("<p>")
                .append(Html.separateWithDots(issueLink, hastebinLink)).append("<br><br>")
                .append("This page contains debug information for an issue ticket. You can copy it directly into the issue, the info is pre-formatted.")
                .append("</p>");

        TabsElement.Tab info = new TabsElement.Tab(Icon.called("server") + " Server Information", createServerInfoContent());
        TabsElement.Tab errors = new TabsElement.Tab(Icon.called("exclamation-circle") + " Errors", createErrorContent());
        TabsElement.Tab debugLog = new TabsElement.Tab(Icon.called("bug") + " Debug Log", createDebugLogContent());
        TabsElement.Tab caches = new TabsElement.Tab(Icon.called("archive") + " Plan Caches", createCacheContent());

        TabsElement tabs = new TabsElement(info, errors, debugLog, caches);

        return preContent + tabs.toHtmlFull();
    }

    private String createCacheContent() {
        StringBuilder content = new StringBuilder();
        appendResourceCache(content);
        appendSessionCache(content);
        return content.toString();
    }

    private void appendResourceCache(StringBuilder content) {
        try {
            content.append("<pre>### Cached Resources (from File or Jar):<br><br>");
            List<String> cacheKeys = ResourceCache.getCachedResourceNames();
            if (cacheKeys.isEmpty()) {
                content.append("Empty");
            }
            for (String cacheKey : cacheKeys) {
                content.append("- ").append(cacheKey).append("<br>");
            }
            content.append("</pre>");
        } catch (Exception e) {
            errorLogger.log(L.WARN, e, ErrorContext.builder().related("/debug page access, resource cache").build());
        }
    }

    private void appendSessionCache(StringBuilder content) {
        try {
            content.append("<pre>### Session Cache:<br><br>");
            content.append("Name | Session Started <br>")
                    .append("-- | -- <br>");
            Set<Map.Entry<UUID, Session>> sessions = SessionCache.getActiveSessions().entrySet();
            if (sessions.isEmpty()) {
                content.append("Empty");
            }
            for (Map.Entry<UUID, Session> entry : sessions) {
                Session session = entry.getValue();
                String name = session.getValue(SessionKeys.NAME).orElse(entry.getKey().toString());
                String start = session.getValue(SessionKeys.START).map(yearFormatter).orElse("Unknown");
                content.append(name).append(" | ").append(start).append("<br>");
            }
            content.append("</pre>");
        } catch (Exception e) {
            errorLogger.log(L.WARN, e, ErrorContext.builder().related("/debug page access, Session cache").build());
        }
    }

    private String createDebugLogContent() {
        StringBuilder content = new StringBuilder();
        appendDebugLog(content);
        return content.toString();
    }

    private String createErrorContent() {
        StringBuilder content = new StringBuilder();
        appendLoggedErrors(content);
        return content.toString();
    }

    private String createServerInfoContent() {
        StringBuilder content = new StringBuilder();

        appendServerInformation(content);
        appendBenchmarks(content);

        return content.toString();
    }

    private void appendServerInformation(StringBuilder content) {
        ServerProperties serverProperties = serverInfo.getServerProperties();

        content.append("<pre>### Server Information<br>")
                .append("**Plan Version:** ")
                .append(versionChecker.getCurrentVersion())
                .append("<br>");

        content.append("**Server:** ");
        content.append(serverProperties.getName())
                .append(" ").append(serverProperties.getImplVersion())
                .append(" (").append(serverProperties.getVersion());
        content.append(")<br>");

        content.append("**Database:** ").append(database.getType().getName());
        content.append("<br><br>");

        Properties properties = System.getProperties();

        String osName = properties.getProperty("os.name");
        String osVersion = properties.getProperty("os.version");
        String osArch = properties.getProperty("os.arch");

        String javaVendor = properties.getProperty("java.vendor");
        String javaVersion = properties.getProperty("java.version");

        String javaVMVendor = properties.getProperty("java.vm.vendor");
        String javaVMName = properties.getProperty("java.vm.name");
        String javaVMVersion = properties.getProperty("java.vm.version");

        content.append("**Operating SubSystem:** ").append(osName).append(" (").append(osArch)
                .append(") version ").append(osVersion).append("<br>");

        content.append("**Java Version:** ").append(javaVersion).append(", ").append(javaVendor).append("<br>");
        content.append("**Java VM Version:** ").append(javaVMName).append(" version ").append(javaVMVersion)
                .append(", ").append(javaVMVendor).append("<br>");

        content.append("</pre>");
    }

    private void appendBenchmarks(StringBuilder content) {
        content.append("<pre>### Benchmarks<br>&#96;&#96;&#96;<br>");
        try {
            for (Benchmark result : timings.getAverageResults()) {
                content.append(result.toString()).append("<br>");
            }
        } catch (Exception e) {
            content.append("Exception on Timings#getAverageResults");
        }
        content.append("&#96;&#96;&#96;</pre>");
    }

    private void appendLoggedErrors(StringBuilder content) {
        content.append("<pre>### Logged Errors<br>");
        content.append("Using incompatible ErrorHandler");
        content.append("</pre>");
    }

    private void appendDebugLog(StringBuilder content) {
        Optional<MemoryDebugLogger> memoryDebugLogger = this.debugLogger.getDebugLogger(MemoryDebugLogger.class);
        Map<String, List<String>> channels = memoryDebugLogger.map(MemoryDebugLogger::getChannels).orElse(new HashMap<>());

        if (channels.isEmpty()) {
            content.append("Incompatible Debug Logger in use (No MemoryDebugLogger)");
            return;
        }

        TabsElement.Tab[] tabs = channels.entrySet().stream()
                .sorted((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getKey(), two.getKey()))
                .map(channel -> {
                    String name = channel.getKey().isEmpty() ? "Default" : channel.getKey();
                    return new TabsElement.Tab(name, debugChannelContent(name, channel.getValue()));
                })
                .toArray(TabsElement.Tab[]::new);

        content.append(new TabsElement(tabs).toHtmlFull());
    }

    private String debugChannelContent(String channelName, List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("<pre>### Debug (").append(channelName).append(")<br>&#96;&#96;&#96;<br>");
        for (String line : lines) {
            content.append(line).append("<br>");
        }
        content.append("&#96;&#96;&#96;</pre>");
        return content.toString();
    }
}