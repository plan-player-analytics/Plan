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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.html.structure.NavLink;
import com.djrapitops.plan.delivery.rendering.html.structure.TabsElement;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;

/**
 * Responsible for generating /server page plugin tabs based on DataExtension API data.
 * <p>
 * Currently very similar to {@link PlayerPluginTab}.
 * This will become more complex once tables are added, since some big tables will be moved to their own tabs.
 *
 * @author AuroraLS3
 */
public class ServerPluginTabs {

    private List<ExtensionData> serverData;
    private List<ExtensionData> extraTabServerData;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

    private StringBuilder nav;
    private String tab;

    public ServerPluginTabs(String nav, String tab) {
        this.nav = new StringBuilder(nav);
        this.tab = tab;
    }

    public ServerPluginTabs(
            List<ExtensionData> serverData,
            Formatters formatters
    ) {
        this.serverData = serverData;
        Collections.sort(serverData);
        this.extraTabServerData = Lists.filter(serverData, ExtensionData::doesNeedWiderSpace);
        this.serverData.removeAll(extraTabServerData);

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
        return nav.toString();
    }

    public String getTabs() {
        return tab;
    }

    private void generate() {
        String tabID = "plugins-overview";
        if (serverData.isEmpty()) {
            nav = new StringBuilder(NavLink.main(Icon.called("cubes").build(), tabID, "Overview (No Data)").toHtml());
            tab = wrapInWideColumnTab(
                    "Overview", "<div class=\"card\"><div class=\"card-body\"><p>No Extension Data</p></div></div>"
            );
        } else {
            nav = new StringBuilder(NavLink.main(Icon.called("cubes").build(), tabID, "Overview").toHtml());
            tab = generateOverviewTab();
        }
        tab += generateExtraTabs();
    }

    private String generateExtraTabs() {
        StringBuilder tabBuilder = new StringBuilder();

        for (ExtensionData datum : extraTabServerData) {
            ExtensionInformation extensionInformation = datum.getExtensionInformation();

            boolean onlyGeneric = datum.hasOnlyGenericTab();

            String tabsElement;
            if (onlyGeneric) {
                ExtensionTabData genericTabData = datum.getTabs().get(0);
                tabsElement = buildContentHtml(genericTabData);
            } else {
                tabsElement = new TabsElement(
                        datum.getTabs().stream().map(this::wrapToTabElementTab).toArray(TabsElement.Tab[]::new)
                ).toHtmlFull();
            }

            String tabName = extensionInformation.getPluginName();
            tabBuilder.append(wrapInWideColumnTab(tabName, wrapInContainer(extensionInformation, tabsElement)));
            nav.append(NavLink.main(Icon.fromExtensionIcon(extensionInformation.getIcon()), "plugins-" + tabName, tabName).toHtml());
        }
        return tabBuilder.toString();
    }

    private String generateOverviewTab() {
        StringBuilder contentBuilder = new StringBuilder();

        for (ExtensionData datum : serverData) {
            ExtensionInformation extensionInformation = datum.getExtensionInformation();

            boolean onlyGeneric = datum.hasOnlyGenericTab();

            String tabsElement;
            if (onlyGeneric) {
                ExtensionTabData genericTabData = datum.getTabs().get(0);
                tabsElement = buildContentHtml(genericTabData);
            } else {
                tabsElement = new TabsElement(
                        datum.getTabs().stream().map(this::wrapToTabElementTab).toArray(TabsElement.Tab[]::new)
                ).toHtmlFull();
            }

            contentBuilder.append(wrapInContainer(extensionInformation, tabsElement));
        }

        return wrapInOverviewTab(contentBuilder.toString());
    }

    private String wrapInWideColumnTab(String tabName, String content) {
        return "<div class=\"tab\" id=\"" + NavLink.format("plugins-" + tabName) + "\"><div class=\"container-fluid mt-4\">" +
                // Page heading
                "<div class=\"d-sm-flex align-items-center justify-content-between mb-4\">" +
                "<h1 class=\"h3 mb-0 text-gray-800\"><i class=\"sidebar-toggler fa fa-fw fa-bars\"></i>${serverName} &middot; " + tabName + "</h1>${backButton}" +
                "</div>" +
                // End Page heading
                "<div class=\"row\"><div class=\"col-md-12\">" + content + "</div></div></div></div>";
    }

    private String wrapInOverviewTab(String content) {
        return "<div class=\"tab\" id=\"" + NavLink.format("plugins-overview") + "\"><div class=\"container-fluid mt-4\">" +
                // Page heading
                "<div class=\"d-sm-flex align-items-center justify-content-between mb-4\">" +
                "<h1 class=\"h3 mb-0 text-gray-800\"><i class=\"sidebar-toggler fa fa-fw fa-bars\"></i>${serverName} &middot; Plugins Overview</h1>${backButton}" +
                "</div>" +
                // End Page heading
                "<div class=\"card-columns\">" + content + "</div></div></div>";
    }

    private TabsElement.Tab wrapToTabElementTab(ExtensionTabData tabData) {
        TabInformation tabInformation = tabData.getTabInformation();
        String tabContentHtml = buildContentHtml(tabData);

        String tabName = tabInformation.getTabName();
        return new TabsElement.Tab(tabName.isEmpty()
                ? Icon.called("info-circle").build().toHtml() + " General"
                : Icon.fromExtensionIcon(tabInformation.getTabIcon()).toHtml() + ' ' + tabName,
                tabContentHtml);
    }

    private String buildContentHtml(ExtensionTabData tabData) {
        TabInformation tabInformation = tabData.getTabInformation();

        List<ElementOrder> order = tabInformation.getTabElementOrder();
        String values = buildValuesHtml(tabData);
        String valuesHtml = values.isEmpty() ? "" : "<div class=\"card-body\">" + values + "</div>";
        String tablesHtml = buildTablesHtml(tabData);

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

    private String buildTablesHtml(ExtensionTabData tabData) {
        StringBuilder builder = new StringBuilder();
        for (ExtensionTableData tableData : tabData.getTableData()) {
            builder.append(tableData.getHtmlTable().toHtml());
        }
        return builder.toString();
    }

    private String buildValuesHtml(ExtensionTabData tabData) {
        StringBuilder builder = new StringBuilder();
        for (String key : tabData.getValueOrder()) {
            tabData.getBoolean(key).ifPresent(data -> append(builder, data.getDescription(), data.getFormattedValue()));
            tabData.getDouble(key).ifPresent(data -> append(builder, data.getDescription(), data.getFormattedValue(decimalFormatter)));
            tabData.getPercentage(key).ifPresent(data -> append(builder, data.getDescription(), data.getFormattedValue(percentageFormatter)));
            tabData.getNumber(key).ifPresent(data -> append(builder, data.getDescription(), data.getFormattedValue(numberFormatters.get(data.getFormatType()))));
            tabData.getString(key).ifPresent(data -> append(builder, data.getDescription(), data.getFormattedValue()));
        }
        return builder.toString();
    }

    private void append(StringBuilder builder, ExtensionDescription description, String formattedValue) {
        Optional<String> textDescription = description.getDescription();
        if (textDescription.isPresent()) {
            builder.append("<p title=\"").append(textDescription.get()).append("\">");
        } else {
            builder.append("<p>");
        }
        builder.append(Icon.fromExtensionIcon(description.getIcon()))
                .append(' ').append(description.getText()).append("<span class=\"float-right\"><b>").append(formattedValue).append("</b></span></p>");
    }

    private String wrapInContainer(ExtensionInformation information, String tabsElement) {
        return "<div class=\"card shadow mb-4\">" +
                "<div class=\"card-header py-3\">" +
                "<h6 class=\"m-0 font-weight-bold col-black\">" + Icon.fromExtensionIcon(information.getIcon()) + ' ' + information.getPluginName() + "</h6>" +
                "</div>" +
                tabsElement +
                "</div>";
    }
}