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
import com.djrapitops.plan.delivery.domain.datatransfer.PlayerListDto;
import com.djrapitops.plan.delivery.domain.datatransfer.TablePlayerDto;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionDescriptionDto;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionTabDataDto;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionValueDataDto;
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
import com.djrapitops.plan.utilities.java.Maps;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for creating jQuery Datatables JSON for a Players Table.
 *
 * @author AuroraLS3
 */
public class PlayersTableJSONCreator {

    private final List<TablePlayer> players;
    private final List<ExtensionDescription> extensionDescriptions;
    private final Map<UUID, ExtensionTabData> extensionData;
    private final Locale locale;
    private final boolean playersPage;

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
        this(players, extensionData, openPlayerPageInNewTab, formatters, locale, false);
    }

    public PlayersTableJSONCreator(
            List<TablePlayer> players,
            Map<UUID, ExtensionTabData> extensionData,
            // Settings
            boolean openPlayerPageInNewTab,
            Formatters formatters,
            Locale locale,
            boolean playersPage
    ) {
        // Data
        this.players = players;
        this.extensionData = extensionData;
        this.locale = locale;
        this.playersPage = playersPage;

        extensionDescriptions = new ArrayList<>();
        addExtensionDescriptions(extensionData);
        extensionDescriptions.sort((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getName(), two.getName()));

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

    private void addExtensionDescriptions(Map<UUID, ExtensionTabData> extensionData) {
        Set<String> foundDescriptions = new HashSet<>();
        for (ExtensionTabData tabData : extensionData.values()) {
            for (ExtensionDescription description : tabData.getDescriptions()) {
                if (!foundDescriptions.contains(description.getName())) {
                    extensionDescriptions.add(description);
                    foundDescriptions.add(description.getName());
                }
            }
        }
    }

    /**
     * This method is kept for /v1/players backwards compatibility.
     *
     * @deprecated Use {@link PlayersTableJSONCreator#toPlayerList()}.
     */
    @Deprecated(since = "5.6")
    public Map<String, Object> toJSONMap() {
        return Maps.builder(String.class, Object.class)
                .put("columns", createColumnHeaders())
                .put("data", createData())
                .build();
    }

    public PlayerListDto toPlayerList() {
        return new PlayerListDto(toPlayers(), getExtensionDescriptors());
    }

    private List<TablePlayerDto> toPlayers() {
        return players.stream()
                .map(player -> TablePlayerDto.builder()
                        .withUuid(player.getPlayerUUID())
                        .withName(player.getName().orElseGet(() -> player.getPlayerUUID().toString()))
                        .withActivityIndex(player.getCurrentActivityIndex().map(ActivityIndex::getValue).orElse(0.0))
                        .withSessionCount((long) player.getSessionCount().orElse(0))
                        .withPlaytimeActive(player.getActivePlaytime().orElse(null))
                        .withLastSeen(player.getLastSeen().orElse(null))
                        .withRegistered(player.getRegistered().orElse(null))
                        .withCountry(player.getGeolocation().orElse(null))
                        .withExtensionValues(mapToExtensionValues(extensionData.get(player.getPlayerUUID())))
                        .withPing(player.getPing())
                        .withNicknames(player.getNicknames())
                        .build()
                ).collect(Collectors.toList());
    }

    private List<ExtensionDescriptionDto> getExtensionDescriptors() {
        return extensionDescriptions.stream().map(ExtensionDescriptionDto::new).collect(Collectors.toList());
    }

    private Map<String, ExtensionValueDataDto> mapToExtensionValues(ExtensionTabData extensionTabData) {
        if (extensionTabData == null) return Collections.emptyMap();

        Map<String, ExtensionValueDataDto> values = new HashMap<>();
        List<ExtensionDescription> descriptions = extensionTabData.getDescriptions();
        for (ExtensionDescription description : descriptions) {
            String name = description.getName();
            ExtensionTabDataDto.mapToValue(extensionTabData, name).ifPresent(value -> values.put(name, value));
        }
        return values;
    }

    private List<Map<String, Object>> createData() {
        List<Map<String, Object>> dataJson = new ArrayList<>();

        ExtensionTabData emptyExtensionData = new ExtensionTabData.Builder(null).build();
        for (TablePlayer player : players) {
            UUID playerUUID = player.getPlayerUUID();
            if (playerUUID == null) {
                continue;
            }

            Map<String, Object> playerEntry = new HashMap<>();
            addPlayerData(playerEntry, player);
            addExtensionData(playerEntry, extensionData.getOrDefault(playerUUID, emptyExtensionData));
            dataJson.add(playerEntry);
        }
        return dataJson;
    }

    private void addPlayerData(Map<String, Object> dataJson, TablePlayer player) {
        String name = player.getName().orElse(player.getPlayerUUID().toString());
        String url = (playersPage ? "./player/" : "../player/") +
                Html.encodeToURL(player.getPlayerUUID().toString());

        int loginTimes = player.getSessionCount().orElse(0);
        long activePlaytime = player.getActivePlaytime().orElse(-1L);
        long registered = player.getRegistered().orElse(-1L);
        long lastSeen = player.getLastSeen().orElse(-1L);

        ActivityIndex activityIndex = player.getCurrentActivityIndex().orElseGet(() -> new ActivityIndex(0.0, 0));
        boolean isBanned = player.isBanned();
        String activityString = activityIndex.getFormattedValue(decimalFormatter)
                + (isBanned ? " (<b>" + locale.get(HtmlLang.LABEL_BANNED) + "</b>)" : " (" + activityIndex.getGroup() + ")");

        String geolocation = player.getGeolocation().orElse("-");

        Html link = openPlayerPageInNewTab ? Html.LINK_EXTERNAL : Html.LINK;

        /* Backslashes escaped to prevent json errors */
        String escapedName = Strings.CS.replace(StringEscapeUtils.escapeHtml4(name), "\\", "\\\\");
        putDataEntry(dataJson, link.create(url, escapedName, escapedName), "name");
        putDataEntry(dataJson, activityIndex.getValue(), activityString, "index");
        putDataEntry(dataJson, activePlaytime, numberFormatters.get(FormatType.TIME_MILLISECONDS).apply(activePlaytime), "activePlaytime");
        putDataEntry(dataJson, loginTimes, "sessions");
        putDataEntry(dataJson, registered, numberFormatters.get(FormatType.DATE_YEAR).apply(registered), "registered");
        putDataEntry(dataJson, lastSeen, numberFormatters.get(FormatType.DATE_YEAR).apply(lastSeen), "seen");
        putDataEntry(dataJson, geolocation, "geolocation");
    }

    private void putDataEntry(Map<String, Object> dataJson, Object data, String dataName) {
        dataJson.put(dataName, data.toString());
    }

    private void putDataEntry(Map<String, Object> dataJson, Object data, String formatted, String dataName) {
        dataJson.put(dataName, Maps.builder(String.class, Object.class)
                .put("v", data.toString())
                .put("d", formatted)
                .build());
    }

    private void addExtensionData(Map<String, Object> dataJson, ExtensionTabData tabData) {
        for (ExtensionDescription description : extensionDescriptions) {
            addValue(dataJson, tabData, description.getName());
        }
    }

    private void addValue(Map<String, Object> dataJson, ExtensionTabData tabData, String key) {
        // If it's a double, put a double
        Optional<ExtensionDoubleData> doubleValue = tabData.getDouble(key);
        if (doubleValue.isPresent()) {
            putDataEntry(dataJson, doubleValue.get().getRawValue(), doubleValue.get().getFormattedValue(decimalFormatter), key);
            return;
        }

        Optional<ExtensionNumberData> numberValue = tabData.getNumber(key);
        if (numberValue.isPresent()) {
            ExtensionNumberData numberData = numberValue.get();
            FormatType formatType = numberData.getFormatType();
            putDataEntry(dataJson, numberData.getRawValue(), numberData.getFormattedValue(numberFormatters.get(formatType)), key);
            return;
        }

        // If it's a String add a String, otherwise the player has no value for this extension provider.
        String stringValue = tabData.getString(key).map(ExtensionStringData::getValue).orElse("-");
        putDataEntry(dataJson, stringValue, stringValue, key);
    }

    private List<Map<String, Object>> createColumnHeaders() {
        List<Map<String, Object>> columnHeaders = new ArrayList<>();

        columnHeaders.add(makeColumnHeader(Icon.called("user") + " " + locale.get(HtmlLang.LABEL_NAME), "name"));
        columnHeaders.add(makeFColumnHeader(Icon.called("check") + " " + locale.get(HtmlLang.LABEL_ACTIVITY_INDEX), "index"));
        columnHeaders.add(makeFColumnHeader(Icon.called("clock").of(Family.REGULAR) + " " + locale.get(HtmlLang.LABEL_ACTIVE_PLAYTIME), "activePlaytime"));
        columnHeaders.add(makeColumnHeader(Icon.called("calendar-plus").of(Family.REGULAR) + " " + locale.get(HtmlLang.SIDE_SESSIONS), "sessions"));
        columnHeaders.add(makeFColumnHeader(Icon.called("user-plus") + " " + locale.get(HtmlLang.LABEL_REGISTERED), "registered"));
        columnHeaders.add(makeFColumnHeader(Icon.called("calendar-check").of(Family.REGULAR) + " " + locale.get(HtmlLang.LABEL_LAST_SEEN), "seen"));
        columnHeaders.add(makeColumnHeader(Icon.called("globe") + " " + locale.get(HtmlLang.TITLE_COUNTRY), "geolocation"));

        addExtensionHeaders(columnHeaders);

        return columnHeaders;
    }

    private Map<String, Object> makeColumnHeader(String title, String dataProperty) {
        return Maps.builder(String.class, Object.class)
                .put("title", title)
                .put("data", dataProperty)
                .build();
    }

    private Map<String, Object> makeFColumnHeader(String title, String dataProperty) {
        return Maps.builder(String.class, Object.class)
                .put("title", title)
                .put("data", Maps.builder(String.class, String.class)
                        .put("_", dataProperty + ".v")
                        .put("display", dataProperty + ".d")
                        .build()
                ).build();
    }

    private void addExtensionHeaders(List<Map<String, Object>> columnHeaders) {
        for (ExtensionDescription provider : extensionDescriptions) {
            String headerText = Icon.fromExtensionIcon(provider.getIcon().setColor(Color.NONE)).toHtml().replace('"', '\'') + ' ' + provider.getText();
            columnHeaders.add(makeFColumnHeader(headerText, provider.getName()));
        }
    }
}