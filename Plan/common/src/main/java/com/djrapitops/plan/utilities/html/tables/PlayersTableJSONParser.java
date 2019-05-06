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
package com.djrapitops.plan.utilities.html.tables;

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

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
    private final int activeLoginThreshold;
    private final boolean openPlayerPageInNewTab;

    private Map<FormatType, Formatter<Long>> numberFormatters;

    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

    public PlayersTableJSONParser(
            // Data
            List<PlayerContainer> players,
            Map<UUID, ExtensionTabData> extensionData,
            // Settings
            int maxPlayers, long activeMsThreshold, int activeLoginThreshold, boolean openPlayerPageInNewTab,
            // Formatters
            Formatters formatters
    ) {
        // Data
        this.players = players;
        this.extensionData = extensionData;
        extensionDescriptives = extensionData.values().stream()
                .map(ExtensionTabData::getDescriptives)
                .flatMap(Collection::stream)
                .distinct().sorted()
                .collect(Collectors.toList());
        // Settings
        this.maxPlayers = maxPlayers;
        this.activeMsThreshold = activeMsThreshold;
        this.activeLoginThreshold = activeLoginThreshold;
        this.openPlayerPageInNewTab = openPlayerPageInNewTab;
        // Formatters
        numberFormatters = new EnumMap<>(FormatType.class);
        numberFormatters.put(FormatType.DATE_SECOND, formatters.secondLong());
        numberFormatters.put(FormatType.DATE_YEAR, formatters.yearLong());
        numberFormatters.put(FormatType.TIME_MILLISECONDS, formatters.timeAmount());
        numberFormatters.put(FormatType.NONE, Object::toString);

        this.decimalFormatter = formatters.decimals();
        this.percentageFormatter = formatters.percentage();

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
            dataJSON.append('[');           // Start new item

            appendPlayerData(dataJSON, planAPI, now, player);
            appendExtensionData(dataJSON, extensionData.getOrDefault(playerUUID, new ExtensionTabData.Factory(null).build()));

            dataJSON.append(']');           // Close new item

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

        ActivityIndex activityIndex = player.getActivityIndex(now, activeMsThreshold, activeLoginThreshold);
        boolean isBanned = player.getValue(PlayerKeys.BANNED).orElse(false);
        String activityString = activityIndex.getFormattedValue(decimalFormatter)
                + (isBanned ? " (<b>Banned</b>)" : " (" + activityIndex.getGroup() + ")");

        String geolocation = GeoInfoMutator.forContainer(player).mostRecent().map(GeoInfo::getGeolocation).orElse("-");

        Html link = openPlayerPageInNewTab ? Html.LINK_EXTERNAL : Html.LINK;

        String display = "{\"display\":\"";
        String sort = "\",\"sort\": ";

        appendData(dataJSON,
                '"' + link.parse(url, name).replace('"', '\'') + '"',
                display + activityString + sort + activityIndex.getValue() + '}',
                display + numberFormatters.get(FormatType.TIME_MILLISECONDS).apply(playtime) + sort + playtime + '}',
                loginTimes,
                display + numberFormatters.get(FormatType.DATE_YEAR).apply(registered) + sort + registered + '}',
                display + numberFormatters.get(FormatType.DATE_YEAR).apply(lastSeen) + sort + lastSeen + '}',
                '"' + geolocation + '"'
        );
    }

    private void appendExtensionData(StringBuilder dataJSON, ExtensionTabData tabData) {
        for (ExtensionDescriptive descriptive : extensionDescriptives) {
            dataJSON.append(',');
            String key = descriptive.getName();

            // If it's a double, append a double
            Optional<ExtensionDoubleData> doubleValue = tabData.getDouble(key);
            if (doubleValue.isPresent()) {
                dataJSON.append(doubleValue.get().getFormattedValue(decimalFormatter));
                continue;
            }

            // If it's a percentage, append a percentage
            Optional<ExtensionDoubleData> percentageValue = tabData.getPercentage(key);
            if (percentageValue.isPresent()) {
                dataJSON.append("{\"display\": \"").append(percentageValue.get().getFormattedValue(percentageFormatter))
                        .append("\",\"sort\": ").append(percentageValue.get().getRawValue()).append('}');
                continue;
            }

            Optional<ExtensionNumberData> numberValue = tabData.getNumber(key);
            if (numberValue.isPresent()) {
                ExtensionNumberData numberData = numberValue.get();
                FormatType formatType = numberData.getFormatType();
                if (formatType == FormatType.NONE) {
                    // If it's a number, append a number
                    dataJSON.append(numberData.getFormattedValue(numberFormatters.get(formatType)));
                } else {
                    // If it's a formatted number, append a formatted number and sort by the number value
                    dataJSON.append("{\"display\": \"").append(numberData.getFormattedValue(numberFormatters.get(formatType)))
                            .append("\",\"sort\": ").append(numberData.getRawValue()).append('}');
                }
                continue;
            }

            // If it's a String append a String, otherwise the player has no value for this extension provider.
            String stringValue = tabData.getString(key).map(ExtensionStringData::getFormattedValue).orElse("-");
            dataJSON.append('"').append(stringValue).append('"');
        }
    }

    private void appendData(StringBuilder dataJSON, Serializable... dataRows) {
        int max = dataRows.length;
        for (int i = 0; i < max; i++) {
            dataJSON.append(dataRows[i]);
            if (i < max - 1) {
                dataJSON.append(',');
            }
        }
    }

    private String parseColumnHeaders() {
        StringBuilder columnHeaders = new StringBuilder("[");

        appendDataHeaders(columnHeaders,
                Icon.called("user") + " Name",
                Icon.called("check") + " Activity Index",
                Icon.called("clock").of(Family.REGULAR) + " Playtime",
                Icon.called("calendar-plus").of(Family.REGULAR) + " Sessions",
                Icon.called("user-plus") + " Registered",
                Icon.called("calendar-check").of(Family.REGULAR) + " Last Seen",
                Icon.called("globe") + " Geolocation"
        );

        appendExtensionHeaders(columnHeaders);

        return columnHeaders.append(']').toString();
    }

    private void appendDataHeaders(StringBuilder columnHeaders, Serializable... headers) {
        int max = headers.length;
        for (int i = 0; i < max; i++) {
            columnHeaders.append("{\"title\": \"").append(headers[i].toString().replace('"', '\'')).append("\"}");
            if (i < max - 1) {
                columnHeaders.append(',');
            }
        }
    }

    private void appendExtensionHeaders(StringBuilder columnHeaders) {
        for (ExtensionDescriptive provider : extensionDescriptives) {
            columnHeaders.append(',');
            columnHeaders.append("{\"title\": \"")
                    .append(Icon.fromExtensionIcon(provider.getIcon().setColor(Color.NONE)).toHtml().replace('"', '\''))
                    .append(' ').append(provider.getText())
                    .append("\"}");
        }
    }
}