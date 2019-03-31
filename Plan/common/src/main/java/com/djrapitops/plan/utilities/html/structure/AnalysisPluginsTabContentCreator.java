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
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.utilities.comparators.PluginDataNameComparator;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Creates Plugin section contents for Analysis page.
 *
 * @author Rsl1122
 */
@Singleton
public class AnalysisPluginsTabContentCreator {

    private final HookHandler hookHandler;
    private final HtmlTables tables;
    private final Timings timings;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public AnalysisPluginsTabContentCreator(
            HookHandler hookHandler,
            HtmlTables tables,
            Timings timings,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.hookHandler = hookHandler;
        this.tables = tables;
        this.timings = timings;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    private static void appendNewTab(PluginData pluginData, AnalysisContainer container, StringBuilder nav, StringBuilder otherTabs) {
        nav.append("<li><a class=\"nav-button\" href=\"javascript:void(0)\">").append(pluginData.getSourcePlugin()).append("</a></li>");
        otherTabs.append("<div class=\"tab\"><div class=\"row clearfix\"><div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append(" (Legacy)</h2></div>")
                .append(container.parseHtml())
                .append("</div></div></div></div>");
    }

    private Map<PluginData, AnalysisContainer> analyzeAdditionalPluginData(
            Collection<UUID> uuids,
            com.djrapitops.plan.data.store.containers.AnalysisContainer analysisContainer
    ) {
        Map<PluginData, AnalysisContainer> containers = new HashMap<>();

        List<PluginData> sources = hookHandler.getAdditionalDataSources();

        sources.parallelStream().forEach(source -> {
            String pluginName = source.getSourcePlugin();
            try {
                timings.start("Source " + pluginName);

                source.setAnalysisData(analysisContainer);
                AnalysisContainer container = source.getServerData(uuids, new AnalysisContainer());
                if (container != null && !container.isEmpty()) {
                    containers.put(source, container);
                }

            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                logger.error("A PluginData-source caused an exception: " + pluginName +
                        ", you can disable the integration under 'Plugins." + pluginName + ".Enabled'");
                errorHandler.log(L.WARN, this.getClass(), e);
            } finally {
                timings.end(DebugChannels.ANALYSIS, "Source " + pluginName);
                source.setAnalysisData(null);
            }
        });
        return containers;
    }

    public static void appendThird(PluginData pluginData, InspectContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-4 col-lg-4\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>")
                .append(container.parseHtml())
                .append("</div></div>");
    }

    private static void appendTwoThirds(PluginData pluginData, AnalysisContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-8 col-lg-8\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>")
                .append(container.parseHtml())
                .append("</div></div>");
    }

    private static void appendWhole(PluginData pluginData, AnalysisContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>").append("<div class=\"body\">")
                .append(container.parseHtml())
                .append("</div></div></div>");
    }

    public String[] createContent(
            com.djrapitops.plan.data.store.containers.AnalysisContainer analysisContainer,
            PlayersMutator mutator
    ) {

        if (mutator.all().isEmpty()) {
            return new String[]{"", ""};
        }

        List<UUID> uuids = mutator.uuids();
        Map<PluginData, AnalysisContainer> containers = analyzeAdditionalPluginData(uuids, analysisContainer);

        List<PluginData> order = new ArrayList<>(containers.keySet());
        order.sort(new PluginDataNameComparator());

        StringBuilder nav = new StringBuilder();
        StringBuilder generalTab = new StringBuilder();
        StringBuilder otherTabs = new StringBuilder();

        generalTab.append("<div class=\"tab\"><div class=\"row clearfix\">");

        boolean displayGeneralTab = false;

        for (PluginData pluginData : order) {
            AnalysisContainer container = containers.get(pluginData);

            switch (pluginData.getSize()) {
                case TAB:
                    appendNewTab(pluginData, container, nav, otherTabs);
                    break;
                case WHOLE:
                    if (!container.hasOnlyValues()) {
                        appendWhole(pluginData, container, generalTab);
                        displayGeneralTab = true;
                    }
                    break;
                case TWO_THIRDS:
                    if (!container.hasOnlyValues()) {
                        appendTwoThirds(pluginData, container, generalTab);
                        displayGeneralTab = true;
                    }
                    break;
                case THIRD:
                default:
                    appendThird(pluginData, container, generalTab);
                    displayGeneralTab = true;
                    break;
            }
        }

        generalTab.append("</div></div>");

        String playerListTab = "<div class=\"tab\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-lg-12 col-md-12 col-sm-12 col-xs-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\"><h2><i class=\"fa fa-users\"></i> Plugin Data</h2></div>" +
                "<div class=\"body\">" +
                tables.pluginPlayersTable(containers, mutator.all()).parseHtml() +
                "</div></div></div>" +
                "</div></div>";

        return new String[]{
                (displayGeneralTab ? "<li><a class=\"nav-button\" href=\"javascript:void(0)\">General (Legacy)</a></li>" : "")
                        + "<li><a class=\"nav-button\" href=\"javascript:void(0)\">Player Data (Legacy)</a></li>" + nav.toString(),
                (displayGeneralTab ? generalTab.toString() : "") + playerListTab + otherTabs.toString()
        };
    }
}
