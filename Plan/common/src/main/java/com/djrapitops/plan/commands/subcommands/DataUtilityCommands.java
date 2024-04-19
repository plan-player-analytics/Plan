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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.domain.mutators.GeoInfoMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.importing.importers.Importer;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.locale.lang.HelpLang;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public class DataUtilityCommands {

    private final Locale locale;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Identifiers identifiers;
    private final Formatters formatters;
    private final Exporter exporter;
    private final ImportSystem importSystem;
    private final Processing processing;

    @Inject
    public DataUtilityCommands(
            Locale locale,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Identifiers identifiers,
            Formatters formatters,
            Exporter exporter,
            ImportSystem importSystem,
            Processing processing
    ) {
        this.locale = locale;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.identifiers = identifiers;
        this.formatters = formatters;
        this.exporter = exporter;
        this.importSystem = importSystem;
        this.processing = processing;
    }

    private void ensureDatabaseIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
        }
    }

    public void onExport(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String exportKind = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_ACCEPTS_ARGUMENTS, locale.getString(HelpLang.ARG_EXPORT_KIND), "players, server_json")));

        ensureDatabaseIsOpen();

        getExportFunction(exportKind).accept(sender);
    }

    private Consumer<CMDSender> getExportFunction(@Untrusted String exportArg) {
        if ("players".equals(exportArg)) {
            return this::exportPlayers;
        } else if ("server_json".endsWith(exportArg)) {
            return this::exportServerJSON;
        }
        throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_EXPORTER_NOT_FOUND, exportArg));
    }

    private void exportServerJSON(CMDSender sender) {
        if (config.isFalse(ExportSettings.SERVER_JSON)) {
            throw new IllegalArgumentException("'" + ExportSettings.SERVER_JSON.getPath() + "': false");
        }
        processing.submitNonCritical(() -> {
            try {
                sender.send(locale.getString(CommandLang.PROGRESS_START));
                if (exporter.exportServerJSON(serverInfo.getServer())) {
                    sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
                } else {
                    sender.send(locale.getString(
                            CommandLang.PROGRESS_FAIL,
                            locale.getString(CommandLang.FAIL_SEE_CONFIG_SETTING, ExportSettings.SERVER_JSON.getPath())
                    ));
                }
            } catch (ExportException e) {
                sender.send(locale.get(CommandLang.PROGRESS_FAIL).toString(e.getMessage()));
            }
        });
    }

    private void exportPlayers(CMDSender sender) {
        boolean exportPlayerJSON = config.isTrue(ExportSettings.PLAYER_JSON);
        boolean exportPlayerHTML = config.isTrue(ExportSettings.PLAYER_PAGES);
        boolean exportPlayersHtml = config.isTrue(ExportSettings.PLAYERS_PAGE);
        if (!exportPlayerJSON && !exportPlayerHTML) {
            throw new IllegalArgumentException("'" + ExportSettings.PLAYER_JSON.getPath() + "' & '" + ExportSettings.PLAYER_PAGES.getPath() + "': false (config.yml)");
        }

        if (exportPlayersHtml) {
            processing.submitNonCritical(exporter::exportPlayersPage);
        }
        processing.submitNonCritical(() -> performExport(sender, exportPlayerJSON, exportPlayerHTML));
    }

    private void performExport(CMDSender sender, boolean exportPlayerJSON, boolean exportPlayerHTML) {
        sender.send(locale.getString(CommandLang.PROGRESS_START));

        Map<UUID, String> players = dbSystem.getDatabase().query(UserIdentifierQueries.fetchAllPlayerNames());
        int outOf = players.size();
        int failed = 0;

        int current = 1;
        for (Map.Entry<UUID, String> entry : players.entrySet()) {
            try {
                if (exportPlayerJSON) exporter.exportPlayerJSON(entry.getKey(), entry.getValue());
                if (exportPlayerHTML) exporter.exportPlayerPage(entry.getKey(), entry.getValue());
            } catch (ExportException e) {
                failed++;
            }
            current++;
            if (current % 1000 == 0) {
                sender.send(locale.getString(CommandLang.PROGRESS, current, outOf));
            }
        }
        sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
        if (failed != 0) {
            sender.send(locale.getString(CommandLang.PROGRESS_FAIL));
            sender.send(" §2✔: §f" + (current - failed));
            sender.send(" §c✕: §f" + failed);
        }
    }

    public void onImport(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String importKind = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_ACCEPTS_ARGUMENTS, locale.getString(HelpLang.ARG_IMPORT_KIND), importSystem.getImporterNames().toString())));

        ensureDatabaseIsOpen();

        findAndProcessImporter(sender, importKind);
    }

    private void findAndProcessImporter(CMDSender sender, @Untrusted String importKind) {
        Optional<Importer> foundImporter = importSystem.getImporter(importKind);
        if (foundImporter.isPresent()) {
            Importer importer = foundImporter.get();
            processing.submitNonCritical(() -> {
                sender.send(locale.getString(CommandLang.PROGRESS_START));
                importer.processImport();
                sender.send(locale.getString(CommandLang.PROGRESS_SUCCESS));
            });
        } else {
            sender.send(locale.getString(CommandLang.FAIL_IMPORTER_NOT_FOUND, importKind));
        }
    }

    public void onSearch(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String searchingFor = arguments.concatenate(" ");
        if (searchingFor.trim().isEmpty()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_EMPTY_SEARCH_STRING));
        }

        ensureDatabaseIsOpen();
        List<String> names = dbSystem.getDatabase().query(UserIdentifierQueries.fetchMatchingPlayerNames(searchingFor));
        Collections.sort(names);

        sender.send(locale.getString(CommandLang.HEADER_SEARCH, names.isEmpty() ? 0 : names.size(), searchingFor));

        StringBuilder asTableString = new StringBuilder();
        int i = 0;
        for (String name : names) {
            asTableString.append(name).append(i != 0 && i % 5 == 0 ? '\n' : "::");
            i++;
        }

        sender.send(sender.getFormatter().table(asTableString.toString(), "::"));
    }

    public void onInGame(CMDSender sender, @Untrusted Arguments arguments) {
        @Untrusted String identifier = arguments.concatenate(" ");
        UUID playerUUID = identifiers.getPlayerUUID(identifier);
        UUID senderUUID = sender.getUUID().orElse(null);
        if (playerUUID == null) playerUUID = senderUUID;
        if (playerUUID == null) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_PLAYER_NOT_FOUND, identifier));
        }

        PlayerContainer player = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        if (player.getValue(PlayerKeys.REGISTERED).isEmpty()) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_PLAYER_NOT_FOUND_REGISTER, identifier));
        }

        if (sender.hasPermission(Permissions.INGAME_OTHER) || playerUUID.equals(senderUUID)) {
            sendInGameMessages(sender, player);
        } else {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_NO_PERMISSION) + " (" + Permissions.INGAME_OTHER.get() + ')');
        }
    }

    private void sendInGameMessages(CMDSender sender, PlayerContainer player) {
        long now = System.currentTimeMillis();

        com.djrapitops.plan.delivery.formatting.Formatter<DateHolder> timestamp = formatters.year();
        Formatter<Long> length = formatters.timeAmount();

        String playerName = player.getValue(PlayerKeys.NAME).orElse(locale.getString(GenericLang.UNKNOWN));

        ActivityIndex activityIndex = player.getActivityIndex(now, config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD));
        Long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
        Long lastSeen = player.getValue(PlayerKeys.LAST_SEEN).orElse(0L);
        List<GeoInfo> geoInfo = player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>());
        Optional<GeoInfo> mostRecentGeoInfo = new GeoInfoMutator(geoInfo).mostRecent();
        String geolocation = mostRecentGeoInfo.isPresent() ? mostRecentGeoInfo.get().getGeolocation() : "-";
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
        String latestJoinAddress = sessionsMutator.latestSession()
                .flatMap(session -> session.getExtraData(JoinAddress.class))
                .map(JoinAddress::getAddress)
                .orElse("-");

        String table = locale.getString(CommandLang.HEADER_INSPECT, playerName) + '\n' +
                locale.getString(CommandLang.INGAME_ACTIVITY_INDEX, activityIndex.getFormattedValue(formatters.decimals()), activityIndex.getGroup()) + '\n' +
                locale.getString(CommandLang.INGAME_REGISTERED, timestamp.apply(() -> registered)) + '\n' +
                locale.getString(CommandLang.INGAME_LAST_SEEN, timestamp.apply(() -> lastSeen)) + '\n' +
                locale.getString(CommandLang.INGAME_GEOLOCATION, geolocation) + '\n' +
                "  §2" + locale.getString(HtmlLang.LABEL_LABEL_JOIN_ADDRESS) + ": §f" + latestJoinAddress + '\n' +
                locale.getString(CommandLang.INGAME_TIMES_KICKED, player.getValue(PlayerKeys.KICK_COUNT).orElse(0)) + '\n' +
                '\n' +
                locale.getString(CommandLang.INGAME_PLAYTIME, length.apply(sessionsMutator.toPlaytime())) + '\n' +
                locale.getString(CommandLang.INGAME_ACTIVE_PLAYTIME, length.apply(sessionsMutator.toActivePlaytime())) + '\n' +
                locale.getString(CommandLang.INGAME_AFK_PLAYTIME, length.apply(sessionsMutator.toAfkTime())) + '\n' +
                locale.getString(CommandLang.INGAME_LONGEST_SESSION, length.apply(sessionsMutator.toLongestSessionLength())) + '\n' +
                '\n' +
                locale.getString(CommandLang.INGAME_PLAYER_KILLS, sessionsMutator.toPlayerKillCount()) + '\n' +
                locale.getString(CommandLang.INGAME_MOB_KILLS, sessionsMutator.toMobKillCount()) + '\n' +
                locale.getString(CommandLang.INGAME_DEATHS, sessionsMutator.toDeathCount());
        sender.send(sender.getFormatter().table(table, ": "));
    }

}
