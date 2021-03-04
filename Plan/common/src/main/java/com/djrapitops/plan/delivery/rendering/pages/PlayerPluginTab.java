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

import java.util.*;

/**
 * Responsible for generating /player page plugin tabs based on DataExtension API data.
 *
 * @author AuroraLS3
 */
public class PlayerPluginTab implements Comparable<PlayerPluginTab> {

    private String serverName;
    private List<ExtensionData> playerData;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

    private String nav;
    private String tab;

    private boolean hasWideTable;

    public PlayerPluginTab(String nav, String tab) {
        this.nav = nav;
        this.tab = tab;
    }

    public PlayerPluginTab(
            String serverName,
            List<ExtensionData> playerData,
            Formatters formatters
    ) {
        this.serverName = serverName;
        this.playerData = playerData;

        numberFormatters = new EnumMap<>(FormatType.class);
        numberFormatters.put(FormatType.DATE_SECOND, formatters.secondLong());
        numberFormatters.put(FormatType.DATE_YEAR, formatters.yearLong());
        numberFormatters.put(FormatType.TIME_MILLISECONDS, formatters.timeAmount());
        numberFormatters.put(FormatType.NONE, Object::toString);

        this.decimalFormatter = formatters.decimals();
        this.percentageFormatter = formatters.percentage();

        hasWideTable = false;

        generate();
    }

    public String getNav() {
        return nav;
    }

    public String getTab() {
        return tab;
    }

    private void generate() {
        if (playerData.isEmpty()) {
            nav = NavLink.collapsed(Icon.called("cubes").build(), "plugins-" + serverName + " (No Data)", serverName + " (No Data)").toHtml();
            tab = wrapInWideTab(
                    serverName + " (No Data)",
                    "<div class=\"card\"><div class=\"card-body\"><p>No Extension Data</p></div></div>"
            );
        } else {
            nav = NavLink.collapsed(Icon.called("cubes").build(), "plugins-" + serverName, serverName).toHtml();
            tab = generatePageTab();
        }
    }

    private String generatePageTab() {
        Collections.sort(playerData);

        StringBuilder tabBuilder = new StringBuilder();

        for (ExtensionData datum : playerData) {
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

            tabBuilder.append(wrapInContainer(extensionInformation, tabsElement));
        }

        return wrapInCardColumnsTab(serverName, tabBuilder.toString());
    }

    private String wrapInWideTab(String serverName, String content) {
        return "<div class=\"tab\" id=\"" + NavLink.format("plugins-" + serverName) + "\"><div class=\"container-fluid mt-4\">" +
                // Page heading
                "<div class=\"d-sm-flex align-items-center justify-content-between mb-4\">" +
                "<h1 class=\"h3 mb-0 text-gray-800\"><i class=\"sidebar-toggler fa fa-fw fa-bars\"></i>" + serverName + " &middot; Plugins Overview</h1>${backButton}" +
                "</div>" +
                // End Page heading
                "<div class=\"row\"><div class=\"col-md-12\">" + content + "</div></div></div></div>";
    }

    private String wrapInCardColumnsTab(String serverName, String content) {
        return "<div class=\"tab\" id=\"" + NavLink.format("plugins-" + serverName) + "\"><div class=\"container-fluid mt-4\">" +
                // Page heading
                "<div class=\"d-sm-flex align-items-center justify-content-between mb-4\">" +
                "<h1 class=\"h3 mb-0 text-gray-800\"><i class=\"sidebar-toggler fa fa-fw fa-bars\"></i>" + serverName + " &middot; Plugins Overview</h1>${backButton}" +
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

        ElementOrder[] order = tabInformation.getTabElementOrder().orElse(ElementOrder.values());
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
            if (tableData.isWideTable()) {
                hasWideTable = true;
            }
            builder.append(tableData.getHtmlTable().toHtml());
        }
        return builder.toString();
    }

    private String buildValuesHtml(ExtensionTabData tabData) {
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
                .append(' ').append(descriptive.getText()).append("<span class=\"float-right\"><b>").append(formattedValue).append("</b></span></p>");
    }

    private String wrapInContainer(ExtensionInformation information, String tabsElement) {
        String colWidth = hasWideTable ? "col-md-8 col-lg-8" : "col-md-4 col-lg-4";
        // TODO move large tables to their own tabs
        return "<div class=\"card shadow mb-4\">" +
                "<div class=\"card-header py-3\">" +
                "<h6 class=\"m-0 font-weight-bold col-black\">" + Icon.fromExtensionIcon(information.getIcon()) + ' ' + information.getPluginName() + "</h6>" +
                "</div>" +
                tabsElement +
                "</div>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerPluginTab)) return false;
        PlayerPluginTab that = (PlayerPluginTab) o;
        return Objects.equals(serverName, that.serverName) &&
                Objects.equals(nav, that.nav);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, nav);
    }

    @Override
    public int compareTo(PlayerPluginTab other) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.serverName, other.serverName);
    }
}