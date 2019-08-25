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
package com.djrapitops.plan.system.json;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.GeoInfoMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.utilities.comparators.PlayerContainerLastPlayedComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.*;

/**
 * Parsing utility for creating jQuery Datatables JSON for a Players Table.
 * <p>
 * See https://www.datatables.net/manual/data/orthogonal-data#HTML-5 for sort kinds
 *
 * @author Rsl1122
 */
public class PlayersTableJSONParser {

    private final List<PlayerContainer> players;
    private final List<ExtensionDescriptive> extensionDescriptives;
    private final Map<UUID, ExtensionTabData> extensionData;

    private final int maxPlayers;
    private final long activeMsThreshold;
    private final boolean openPlayerPageInNewTab;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;

    public PlayersTableJSONParser(
            // Data
            List<PlayerContainer> players,
            Map<UUID, ExtensionTabData> extensionData,
            // Settings
            int maxPlayers, long activeMsThreshold, boolean openPlayerPageInNewTab,
            // Formatters
            Formatters formatters
    ) {
        // Data
        this.players = players;
        this.players.sort(new PlayerContainerLastPlayedComparator());

        this.extensionData = extensionData;

        extensionDescriptives = new ArrayList<>();
        addExtensionDescriptives(extensionData);
        extensionDescriptives.sort((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getName(), two.getName()));

        // Settings
        this.maxPlayers = maxPlayers;
        this.activeMsThreshold = activeMsThreshold;
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
        String data = parseData();
        String columnHeaders = parseColumnHeaders();
        return "{\"columns\":" + columnHeaders + ",\"data\":" + data + '}';
    }

    private String parseData() {
        StringBuilder dataJSON = new StringBuilder("[");

        PlanAPI planAPI = PlanAPI.getInstance();
        long now = System.currentTimeMillis();
        players.sort(new PlayerContainerLastPlayedComparator());

        int currentPlayerNumber = 0;
        for (PlayerContainer player : players) {
            if (currentPlayerNumber >= maxPlayers) {
                break;
            }
            UUID playerUUID = player.getValue(PlayerKeys.UUID).orElse(null);
            if (playerUUID == null) {
                continue;
            }

            if (currentPlayerNumber > 0) {
                dataJSON.append(',');       // Previous item
            }
            dataJSON.append('{');           // Start new item

            appendPlayerData(dataJSON, planAPI, now, player);
            appendExtensionData(dataJSON, extensionData.getOrDefault(playerUUID, new ExtensionTabData.Factory(null).build()));

            dataJSON.append('}');           // Close new item

            currentPlayerNumber++;
        }
        return dataJSON.append(']').toString();
    }

    private void appendPlayerData(StringBuilder dataJSON, PlanAPI planAPI, long now, PlayerContainer player) {
        String name = player.getValue(PlayerKeys.NAME).orElse("Unknown");
        String url = planAPI.getPlayerInspectPageLink(name);

        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
        int loginTimes = sessionsMutator.count();
        long playtime = sessionsMutator.toPlaytime();
        long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
        long lastSeen = sessionsMutator.toLastSeen();

        ActivityIndex activityIndex = player.getActivityIndex(now, activeMsThreshold);
        boolean isBanned = player.getValue(PlayerKeys.BANNED).orElse(false);
        String activityString = activityIndex.getFormattedValue(decimalFormatter)
                + (isBanned ? " (<b>Banned</b>)" : " (" + activityIndex.getGroup() + ")");

        String geolocation = GeoInfoMutator.forContainer(player).mostRecent().map(GeoInfo::getGeolocation).orElse("-");

        Html link = openPlayerPageInNewTab ? Html.LINK_EXTERNAL : Html.LINK;

        dataJSON
                .append(makeDataEntry(link.parse(url, name), "name")).append(',')
                .append(makeDataEntry(activityIndex.getValue(), activityString, "index")).append(',')
                .append(makeDataEntry(playtime, numberFormatters.get(FormatType.TIME_MILLISECONDS).apply(playtime), "playtime")).append(',')
                .append(makeDataEntry(loginTimes, "sessions")).append(',')
                .append(makeDataEntry(registered, numberFormatters.get(FormatType.DATE_YEAR).apply(registered), "registered")).append(',')
                .append(makeDataEntry(lastSeen, numberFormatters.get(FormatType.DATE_YEAR).apply(lastSeen), "seen")).append(',')
                .append(makeDataEntry(geolocation, "geolocation"))
        ;
    }

    private String makeDataEntry(Object data, String dataName) {
        return "\"" + dataName + "\":\"" + data.toString().replace('"', '\'') + "\"";
    }

    private String makeDataEntry(Object data, String formatted, String dataName) {
        return "\"" + dataName + "\":{\"v\":\"" + data.toString().replace('"', '\'') + "\", \"d\":\"" + formatted.replace('"', '\'') + "\"}";
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

    private String parseColumnHeaders() {
        StringBuilder columnHeaders = new StringBuilder("[");

        // Is the data for the column formatted

        columnHeaders
                .append(makeColumnHeader(Icon.called("user") + " Name", "name")).append(',')
                .append(makeFColumnHeader(Icon.called("check") + " Activity Index", "index")).append(',')
                .append(makeFColumnHeader(Icon.called("clock").of(Family.REGULAR) + " Playtime", "playtime")).append(',')
                .append(makeColumnHeader(Icon.called("calendar-plus").of(Family.REGULAR) + " Sessions", "sessions")).append(',')
                .append(makeFColumnHeader(Icon.called("user-plus") + " Registered", "registered")).append(',')
                .append(makeFColumnHeader(Icon.called("calendar-check").of(Family.REGULAR) + " Last Seen", "seen")).append(',')
                .append(makeColumnHeader(Icon.called("globe") + " Geolocation", "geolocation"));

        appendExtensionHeaders(columnHeaders);

        return columnHeaders.append(']').toString();
    }

    private String makeColumnHeader(String title, String dataProperty) {
        return "{\"title\": \"" + title.replace('"', '\'') + "\",\"data\":\"" + dataProperty + "\"}";
    }

    private String makeFColumnHeader(String title, String dataProperty) {
        return "{\"title\": \"" + title.replace('"', '\'') + "\",\"data\":{\"_\":\"" + dataProperty + ".v\",\"display\":\"" + dataProperty + ".d\"}}";
    }

    private void appendExtensionHeaders(StringBuilder columnHeaders) {
        for (ExtensionDescriptive provider : extensionDescriptives) {
            columnHeaders.append(',');
            String headerText = Icon.fromExtensionIcon(provider.getIcon().setColor(Color.NONE)).toHtml().replace('"', '\'') + ' ' + provider.getText();
            columnHeaders.append(makeFColumnHeader(headerText, provider.getName()));
        }
    }
}