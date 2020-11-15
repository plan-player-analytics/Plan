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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.html.icon.Family;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

/**
 * Utility for creating jQuery Datatables JSON for a Players Table.
 * <p>
 * See https://www.datatables.net/manual/data/orthogonal-data#HTML-5 for sort kinds
 *
 * @author Rsl1122
 */
public class PlayersTableJSONCreator {

    private final List<TablePlayer> players;
    private final List<ExtensionDescriptive> extensionDescriptives;
    private final Map<UUID, ExtensionTabData> extensionData;
    private final Locale locale;

    private final boolean openPlayerPageInNewTab;

    private final Map<FormatType, Formatter<Long>> numberFormatters;

    private final Formatter<Double> decimalFormatter;

    public PlayersTableJSONCreator(
            List<TablePlayer> players,
            Map<UUID, ExtensionTabData> extensionData,
            // Settings
            boolean openPlayerPageInNewTab,
            Formatters formatters,
            Locale locale
    ) {
        // Data
        this.players = players;
        this.extensionData = extensionData;
        this.locale = locale;

        extensionDescriptives = new ArrayList<>();
        addExtensionDescriptives(extensionData);
        extensionDescriptives.sort((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getName(), two.getName()));

        // Settings
        this.openPlayerPageInNewTab = openPlayerPageInNewTab;
        // Formatters
        numberFormatters = new EnumMap<>(FormatType.class);
        numberFormatters.put(FormatType.DATE_SECOND, formatters.secondLong());
        numberFormatters.put(FormatType.DATE_YEAR, formatters.yearLong());
        numberFormatters.put(FormatType.TIME_MILLISECONDS, formatters.timeAmount());
        numberFormatters.put(FormatType.NONE, Object::toString);

        this.decimalFormatter = formatters.decimals();
    }

    private void addExtensionDescriptives(Map<UUID, ExtensionTabData> extensionData) {
        Set<String> foundDescriptives = new HashSet<>();
        for (ExtensionTabData tabData : extensionData.values()) {
            for (ExtensionDescriptive descriptive : tabData.getDescriptives()) {
                if (!foundDescriptives.contains(descriptive.getName())) {
                    extensionDescriptives.add(descriptive);
                    foundDescriptives.add(descriptive.getName());
                }
            }
        }
    }

    public String toJSONString() {
        String data = createData();
        String columnHeaders = createColumnHeaders();
        return "{\"columns\":" + columnHeaders + ",\"data\":" + data + '}';
    }

    private String createData() {
        StringBuilder dataJSON = new StringBuilder("[");

        int currentPlayerNumber = 0;
        for (TablePlayer player : players) {
            UUID playerUUID = player.getPlayerUUID();
            if (playerUUID == null) {
                continue;
            }

            if (currentPlayerNumber > 0) {
                dataJSON.append(',');       // Previous item
            }
            dataJSON.append('{');           // Start new item

            appendPlayerData(dataJSON, player);
            appendExtensionData(dataJSON, extensionData.getOrDefault(playerUUID, new ExtensionTabData.Builder(null).build()));

            dataJSON.append('}');           // Close new item

            currentPlayerNumber++;
        }
        return dataJSON.append(']').toString();
    }

    private void appendPlayerData(StringBuilder dataJSON, TablePlayer player) {
        String name = player.getName().orElse(player.getPlayerUUID().toString());
        String url = "../player/" + Html.encodeToURL(name);

        int loginTimes = player.getSessionCount().orElse(0);
        long playtime = player.getPlaytime().orElse(-1L);
        long registered = player.getRegistered().orElse(-1L);
        long lastSeen = player.getLastSeen().orElse(-1L);

        ActivityIndex activityIndex = player.getCurrentActivityIndex().orElseGet(() -> new ActivityIndex(0.0, 0));
        boolean isBanned = player.isBanned();
        String activityString = activityIndex.getFormattedValue(decimalFormatter)
                + (isBanned ? " (<b>" + locale.get(HtmlLang.LABEL_BANNED) + "</b>)" : " (" + activityIndex.getGroup() + ")");

        String geolocation = player.getGeolocation().orElse("-");

        Html link = openPlayerPageInNewTab ? Html.LINK_EXTERNAL : Html.LINK;

        dataJSON.append(makeDataEntry(link.create(url, StringUtils.replace(StringEscapeUtils.escapeHtml4(name), "\\", "\\\\")), "name")).append(',') // Backslashes escaped to prevent json errors
                .append(makeDataEntry(activityIndex.getValue(), activityString, "index")).append(',')
                .append(makeDataEntry(playtime, numberFormatters.get(FormatType.TIME_MILLISECONDS).apply(playtime), "playtime")).append(',')
                .append(makeDataEntry(loginTimes, "sessions")).append(',')
                .append(makeDataEntry(registered, numberFormatters.get(FormatType.DATE_YEAR).apply(registered), "registered")).append(',')
                .append(makeDataEntry(lastSeen, numberFormatters.get(FormatType.DATE_YEAR).apply(lastSeen), "seen")).append(',')
                .append(makeDataEntry(geolocation, "geolocation"));
    }

    private String makeDataEntry(Object data, String dataName) {
        return "\"" + dataName + "\":\"" + StringEscapeUtils.escapeJson(data.toString()) + "\"";
    }

    private String makeDataEntry(Object data, String formatted, String dataName) {
        return "\"" + dataName + "\":{\"v\":\"" + StringEscapeUtils.escapeJson(data.toString()) + "\", \"d\":\"" + StringEscapeUtils.escapeJson(formatted) + "\"}";
    }

    private void appendExtensionData(StringBuilder dataJSON, ExtensionTabData tabData) {
        for (ExtensionDescriptive descriptive : extensionDescriptives) {
            dataJSON.append(',');
            String key = descriptive.getName();

            // If it's a double, append a double
            Optional<ExtensionDoubleData> doubleValue = tabData.getDouble(key);

            if (doubleValue.isPresent()) {
                dataJSON.append(makeDataEntry(doubleValue.get().getRawValue(), doubleValue.get().getFormattedValue(decimalFormatter), key));
                continue;
            }

            Optional<ExtensionNumberData> numberValue = tabData.getNumber(key);
            if (numberValue.isPresent()) {
                ExtensionNumberData numberData = numberValue.get();
                FormatType formatType = numberData.getFormatType();
                dataJSON.append(makeDataEntry(numberData.getRawValue(), numberData.getFormattedValue(numberFormatters.get(formatType)), key));
                continue;
            }

            // If it's a String append a String, otherwise the player has no value for this extension provider.
            String stringValue = tabData.getString(key).map(ExtensionStringData::getFormattedValue).orElse("-");
            dataJSON.append(makeDataEntry(stringValue, stringValue, key));
        }
    }

    private String createColumnHeaders() {
        StringBuilder columnHeaders = new StringBuilder("[");

        // Is the data for the column formatted

        columnHeaders
                .append(makeColumnHeader(Icon.called("user") + " " + locale.get(HtmlLang.LABEL_NAME), "name")).append(',')
                .append(makeFColumnHeader(Icon.called("check") + " " + locale.get(HtmlLang.LABEL_ACTIVITY_INDEX), "index")).append(',')
                .append(makeFColumnHeader(Icon.called("clock").of(Family.REGULAR) + " " + locale.get(HtmlLang.LABEL_PLAYTIME), "playtime")).append(',')
                .append(makeColumnHeader(Icon.called("calendar-plus").of(Family.REGULAR) + " " + locale.get(HtmlLang.SIDE_SESSIONS), "sessions")).append(',')
                .append(makeFColumnHeader(Icon.called("user-plus") + " " + locale.get(HtmlLang.LABEL_REGISTERED), "registered")).append(',')
                .append(makeFColumnHeader(Icon.called("calendar-check").of(Family.REGULAR) + " " + locale.get(HtmlLang.LABEL_LAST_SEEN), "seen")).append(',')
                .append(makeColumnHeader(Icon.called("globe") + " " + locale.get(HtmlLang.TITLE_COUNTRY), "geolocation"));

        appendExtensionHeaders(columnHeaders);

        return columnHeaders.append(']').toString();
    }

    private String makeColumnHeader(String title, String dataProperty) {
        return "{\"title\": \"" + StringEscapeUtils.escapeJson(title) + "\",\"data\":\"" + dataProperty + "\"}";
    }

    private String makeFColumnHeader(String title, String dataProperty) {
        return "{\"title\": \"" + StringEscapeUtils.escapeJson(title) + "\",\"data\":{\"_\":\"" + dataProperty + ".v\",\"display\":\"" + dataProperty + ".d\"}}";
    }

    private void appendExtensionHeaders(StringBuilder columnHeaders) {
        for (ExtensionDescriptive provider : extensionDescriptives) {
            columnHeaders.append(',');
            String headerText = Icon.fromExtensionIcon(provider.getIcon().setColor(Color.NONE)).toHtml().replace('"', '\'') + ' ' + provider.getText();
            columnHeaders.append(makeFColumnHeader(headerText, provider.getName()));
        }
    }
}