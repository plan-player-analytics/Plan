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
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.connection.ConnectionLog;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.structure.TabsElement;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.FolderTimeStampFileLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;
import com.djrapitops.plugin.logging.error.DefaultErrorHandler;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.logging.error.FolderTimeStampErrorFileLogger;

import java.io.IOException;
import java.util.*;

/**
 * Html parsing for the Debug page.
 *
 * @author Rsl1122
 */
public class DebugPage implements Page {

    private final Database database;
    private final ServerInfo serverInfo;
    private final ConnectionSystem connectionSystem;
    private final CombineDebugLogger debugLogger;
    private final Timings timings;
    private final ErrorHandler errorHandler;

    private final Formatter<DateHolder> secondFormatter;
    private final Formatter<Long> yearFormatter;

    DebugPage(
            Database database,
            ServerInfo serverInfo,
            ConnectionSystem connectionSystem,
            Formatters formatters,
            DebugLogger debugLogger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        this.database = database;
        this.serverInfo = serverInfo;
        this.connectionSystem = connectionSystem;
        this.debugLogger = (CombineDebugLogger) debugLogger;
        this.timings = timings;
        this.errorHandler = errorHandler;

        this.secondFormatter = formatters.second();
        this.yearFormatter = formatters.yearLong();
    }

    @Override
    public String toHtml() {
        StringBuilder preContent = new StringBuilder();

        String issueLink = Html.LINK_EXTERNAL.parse("https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/new", "Create new issue on Github");
        String hastebinLink = Html.LINK_EXTERNAL.parse("https://hastebin.com/", "Create a new hastebin paste");

        preContent.append("<p>")
                .append(HtmlStructure.separateWithDots(issueLink, hastebinLink)).append("<br><br>")
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
        appendResponseCache(content);
        appendSessionCache(content);
        return content.toString();
    }

    private void appendResponseCache(StringBuilder content) {
        try {
            content.append("<pre>### Cached Responses:<br><br>");
            List<String> cacheKeys = new ArrayList<>(ResponseCache.getCacheKeys());
            if (cacheKeys.isEmpty()) {
                content.append("Empty");
            }
            Collections.sort(cacheKeys);
            for (String cacheKey : cacheKeys) {
                content.append("- ").append(cacheKey).append("<br>");
            }
            content.append("</pre>");
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private void appendSessionCache(StringBuilder content) {
        try {
            content.append("<pre>### Session Cache:<br><br>");
            content.append("UUID | Session Started <br>")
                    .append("-- | -- <br>");
            Set<Map.Entry<UUID, Session>> sessions = SessionCache.getActiveSessions().entrySet();
            if (sessions.isEmpty()) {
                content.append("Empty");
            }
            for (Map.Entry<UUID, Session> entry : sessions) {
                UUID uuid = entry.getKey();
                String start = entry.getValue().getValue(SessionKeys.START).map(yearFormatter).orElse("Unknown");
                content.append(uuid.toString()).append(" | ").append(start).append("<br>");
            }
            content.append("</pre>");
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
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
        appendConnectionLog(content);
        appendBenchmarks(content);

        return content.toString();
    }

    private void appendConnectionLog(StringBuilder content) {
        try {
            Map<String, Map<String, ConnectionLog.Entry>> logEntries = connectionSystem.getConnectionLog().getLogEntries();

            content.append("<pre>### Connection Log:<br><br>");
            content.append("Server Address | Request Type | Response | Sent<br>")
                    .append("-- | -- | -- | --<br>");

            if (logEntries.isEmpty()) {
                content.append("**No Connections Logged**<br>");
            }
            for (Map.Entry<String, Map<String, ConnectionLog.Entry>> entry : logEntries.entrySet()) {
                String address = entry.getKey();
                Map<String, ConnectionLog.Entry> requests = entry.getValue();
                for (Map.Entry<String, ConnectionLog.Entry> requestEntry : requests.entrySet()) {
                    String infoRequest = requestEntry.getKey();
                    ConnectionLog.Entry logEntry = requestEntry.getValue();

                    content.append(address).append(" | ")
                            .append(infoRequest).append(" | ")
                            .append(logEntry.getResponseCode()).append(" | ")
                            .append(secondFormatter.apply(logEntry)).append("<br>");
                }

            }
            content.append("</pre>");

            content.append("<pre>### Servers:<br><br>");
            List<Server> servers = connectionSystem.getBukkitServers();
            content.append("Server Name | Address <br>")
                    .append("-- | --<br>");
            for (Server server : servers) {
                content.append(server.getName()).append(" | ")
                        .append(server.getWebAddress()).append("<br>");
            }
            content.append("</pre>");

        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    private void appendServerInformation(StringBuilder content) {
        ServerProperties serverProperties = serverInfo.getServerProperties();

        content.append("<pre>### Server Information<br>")
                .append("**Plan Version:** ${version}<br>");

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

        if (errorHandler instanceof DefaultErrorHandler) {
            appendErrorLines(content, (DefaultErrorHandler) errorHandler);
        } else {
            content.append("Using incompatible ErrorHandler");
        }

        content.append("</pre>");
    }

    private void appendErrorLines(StringBuilder content, DefaultErrorHandler errorHandler) {
        List<String> lines = errorHandler.getErrorHandler(FolderTimeStampErrorFileLogger.class)
                .flatMap(FolderTimeStampFileLogger::getCurrentFile)
                .map(file -> {
                    try {
                        return FileUtil.lines(file);
                    } catch (IOException e) {
                        errorHandler.log(L.WARN, this.getClass(), e);
                        return new ArrayList<String>();
                    }
                }).orElse(new ArrayList<>());
        SortedMap<String, List<String>> errors = FolderTimeStampErrorFileLogger.splitByError(lines);

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
                content.append("</pre><pre>&#96;&#96;&#96;<br>")
                        .append(error)
                        .append("&#96;&#96;&#96;");
            }
        } else {
            content.append("**No Errors logged.**<br>");
        }
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