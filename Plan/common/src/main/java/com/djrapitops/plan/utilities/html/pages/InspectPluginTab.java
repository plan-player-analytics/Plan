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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionDescriptive;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionInformation;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionPlayerData;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionTabData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.structure.TabsElement;

import java.util.*;

/**
 * Responsible for generating /player page plugin tabs based on DataExtension API data.
 *
 * @author Rsl1122
 */
public class InspectPluginTab implements Comparable<InspectPluginTab> {

    private String serverName;
    private List<ExtensionPlayerData> data;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

    private String nav;
    private String tab;

    public InspectPluginTab(String nav, String tab) {
        this.nav = nav;
        this.tab = tab;
    }

    public InspectPluginTab(
            String serverName,
            List<ExtensionPlayerData> data,
            Formatters formatters
    ) {
        this.serverName = serverName;
        this.data = data;

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

    public String getTab() {
        return tab;
    }

    private void generate() {
        if (data.isEmpty()) {
            nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">" + serverName + " (No Data)</a></li>";
            tab = "<div class=\"tab\"><div class=\"row clearfix\">" +
                    "<div class=\"col-md-12\">" + Html.CARD.parse("<p>No Data (" + serverName + ")</p>") +
                    "</div></div></div>";
        } else {
            nav = "<li><a class=\"nav-button\" href=\"javascript:void(0)\">" + serverName + "</a></li>";
            tab = generatePageTab();
        }
    }

    private String generatePageTab() {
        Collections.sort(data);

        StringBuilder tabBuilder = new StringBuilder();

        for (ExtensionPlayerData datum : data) {
            ExtensionInformation extensionInformation = datum.getExtensionInformation();

            boolean onlyGeneric = datum.hasOnlyGenericTab();

            String tabsElement;
            if (onlyGeneric) {
                ExtensionTabData genericTabData = datum.getTabs().get(0);
                tabsElement = Html.BODY.parse(parseDataHtml(genericTabData));
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

        return new TabsElement.Tab(tabInformation.getTabName(), parseDataHtml(tabData));
    }

    private String parseDataHtml(ExtensionTabData tabData) {
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

    @Override
    public int compareTo(InspectPluginTab other) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.serverName, other.serverName);
    }
}