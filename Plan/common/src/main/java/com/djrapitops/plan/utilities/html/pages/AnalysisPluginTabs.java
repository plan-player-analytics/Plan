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

import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionDescriptive;
import com.djrapitops.plan.extension.implementation.results.ExtensionInformation;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTableData;
import com.djrapitops.plan.extension.implementation.results.server.ExtensionServerData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.structure.TabsElement;

import java.util.*;

/**
 * Responsible for generating /server page plugin tabs based on DataExtension API data.
 * <p>
 * Currently very similar to {@link InspectPluginTab}.
 * This will become more complex once tables are added, since some big tables will be moved to their own tabs.
 *
 * @author Rsl1122
 */
public class AnalysisPluginTabs {

    private List<ExtensionServerData> serverData;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

    private String nav;
    private String tab;

    public AnalysisPluginTabs(String nav, String tab) {
        this.nav = nav;
        this.tab = tab;
    }

    public AnalysisPluginTabs(
            List<ExtensionServerData> serverData,
            Formatters formatters
    ) {
        this.serverData = serverData;

        numberFormatters = new EnumMap<>(FormatType.class);
        numberFormatters.put(FormatType.DATE_SECOND, formatters.secondLong());
        numberFormatters.put(FormatType.DATE_YEAR, formatters.yearLong());
        numberFormatters.put(FormatType.TIME_MILLISECONDS, formatters.timeAmount());
        numberFormatters.put(FormatType.NONE, Object::toString);

        this.decimalFormatter = formatters.decimals();
        this.percentageFormatter = formatters.percentage();

        generate();
    }

    public String getNav() {
        return nav;
    }

    public String getTabs() {
        return tab;
    }

    private void generate() {
        if (serverData.isEmpty()) {
            nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">Extensions (No Data)</a></li>";
            tab = "<div class=\"tab\"><div class=\"row clearfix\">" +
                    "<div class=\"col-md-12\">" + Html.CARD.parse("<div class=\"body\"><p>No Extension Data</p></div>") +
                    "</div></div></div>";
        } else {
            nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">General</a></li>";
            tab = generatePageTab();
        }
    }

    private String generatePageTab() {
        Collections.sort(serverData);

        StringBuilder tabBuilder = new StringBuilder();

        for (ExtensionServerData datum : serverData) {
            ExtensionInformation extensionInformation = datum.getExtensionInformation();

            boolean onlyGeneric = datum.hasOnlyGenericTab();

            String tabsElement;
            if (onlyGeneric) {
                ExtensionTabData genericTabData = datum.getTabs().get(0);
                tabsElement = Html.BODY.parse(parseContentHtml(genericTabData));
            } else {
                tabsElement = new TabsElement(
                        datum.getTabs().stream().map(this::wrapToTabElementTab).toArray(TabsElement.Tab[]::new)
                ).toHtmlFull();
            }

            tabBuilder.append(wrapInContainer(extensionInformation, tabsElement));
        }

        return wrapInTab(tabBuilder.toString());
    }

    private String wrapInTab(String content) {
        return "<div class=\"tab\"><div class=\"row clearfix\">" + content + "</div></div>";
    }

    private TabsElement.Tab wrapToTabElementTab(ExtensionTabData tabData) {
        TabInformation tabInformation = tabData.getTabInformation();
        String tabContentHtml = parseContentHtml(tabData);

        return new TabsElement.Tab(tabInformation.getTabName(), tabContentHtml);
    }

    private String parseContentHtml(ExtensionTabData tabData) {
        TabInformation tabInformation = tabData.getTabInformation();

        ElementOrder[] order = tabInformation.getTabElementOrder().orElse(ElementOrder.values());
        String valuesHtml = parseValuesHtml(tabData);
        String tablesHtml = parseTablesHtml(tabData);

        StringBuilder builder = new StringBuilder();

        for (ElementOrder ordering : order) {
            switch (ordering) {
                case VALUES:
                    builder.append(valuesHtml);
                    break;
                case TABLE:
                    builder.append(tablesHtml);
                    break;
                default:
                    break;
            }
        }

        return builder.toString();
    }

    private String parseTablesHtml(ExtensionTabData tabData) {
        StringBuilder builder = new StringBuilder();
        for (ExtensionTableData tableData : tabData.getTableData()) {
            builder.append(tableData.getHtmlTable().parseHtml());
        }
        return builder.toString();
    }

    private String parseValuesHtml(ExtensionTabData tabData) {
        StringBuilder builder = new StringBuilder();
        for (String key : tabData.getValueOrder()) {
            tabData.getBoolean(key).ifPresent(data -> append(builder, data.getDescriptive(), data.getFormattedValue()));
            tabData.getDouble(key).ifPresent(data -> append(builder, data.getDescriptive(), data.getFormattedValue(decimalFormatter)));
            tabData.getPercentage(key).ifPresent(data -> append(builder, data.getDescriptive(), data.getFormattedValue(percentageFormatter)));
            tabData.getNumber(key).ifPresent(data -> append(builder, data.getDescriptive(), data.getFormattedValue(numberFormatters.get(data.getFormatType()))));
            tabData.getString(key).ifPresent(data -> append(builder, data.getDescriptive(), data.getFormattedValue()));
        }
        return builder.toString();
    }

    private void append(StringBuilder builder, ExtensionDescriptive descriptive, String formattedValue) {
        Optional<String> description = descriptive.getDescription();
        if (description.isPresent()) {
            builder.append("<p title=\"").append(description.get()).append("\">");
        } else {
            builder.append("<p>");
        }
        builder.append(Icon.fromExtensionIcon(descriptive.getIcon()))
                .append(' ').append(descriptive.getText()).append(": ").append(formattedValue).append("</p>");
    }

    private String wrapInContainer(ExtensionInformation information, String tabsElement) {
        return "<div class=\"col-xs-12 col-sm-12 col-md-4 col-lg-4\"><div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>" + Icon.fromExtensionIcon(information.getIcon()) + ' ' + information.getPluginName() + "</h2>" +
                "</div>" +
                tabsElement +
                "</div></div>";
    }
}