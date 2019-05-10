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
package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.SupplierDataContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.HealthInfoLang;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;

import java.util.*;

public class NetworkHealthInformation extends AbstractHealthInfo {

    private final NetworkContainer container;
    private final TimeZone timeZone;

    public NetworkHealthInformation(
            NetworkContainer container,
            Locale locale,
            long activeMsThreshold,
            int activeLoginThreshold,
            Formatter<Long> timeAmountFormatter,
            Formatter<Double> decimalFormatter,
            Formatter<Double> percentageFormatter,
            TimeZone timeZone
    ) {
        super(
                container.getUnsafe(NetworkKeys.REFRESH_TIME),
                container.getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO),
                locale,
                activeMsThreshold, activeLoginThreshold,
                timeAmountFormatter, decimalFormatter, percentageFormatter
        );
        this.container = container;
        this.timeZone = timeZone;
        calculate();
    }

    @Override
    protected void calculate() {
        perServerComparisonNotes(container.getUnsafe(NetworkKeys.PLAYERS_MUTATOR));

        activityChangeNote(container.getUnsafe(NetworkKeys.ACTIVITY_DATA));
        activePlayerPlaytimeChange(container.getUnsafe(NetworkKeys.PLAYERS_MUTATOR));
    }

    private void perServerComparisonNotes(PlayersMutator playersMutator) {
        Collection<Server> servers = container.getValue(NetworkKeys.BUKKIT_SERVERS)
                .orElse(Collections.emptyList());

        if (servers.isEmpty()) {
            addNote(Icons.HELP_RING + locale.getString(HealthInfoLang.NO_SERVERS_INACCURACY));
            return;
        }
        int serverCount = servers.size();
        if (serverCount == 1) {
            addNote(Icons.HELP_RING + locale.getString(HealthInfoLang.SINGLE_SERVER_INACCURACY));
            return;
        }

        Key<Server> serverKey = new Key<>(Server.class, "SERVER");

        List<DataContainer> perServerContainers = getPerServerContainers(playersMutator, servers, serverKey);

        uniquePlayersNote(serverCount, serverKey, perServerContainers);
        newPlayersNote(serverCount, serverKey, perServerContainers);
        playersNote(serverKey, perServerContainers);
    }

    private void uniquePlayersNote(int serverCount, Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon;
        String uniquePlayersNote = locale.getString(HealthInfoLang.PLAYER_VISIT_PER_SERVER);
        double average = perServerContainers.stream()
                .mapToInt(c -> c.getUnsafe(AnalysisKeys.AVG_PLAYERS_MONTH))
                .average().orElse(0.0);
        if (average < 1) {
            icon = Icons.RED_WARN;
            serverHealth -= 10.0;
        } else if (average < serverCount) {
            icon = Icons.YELLOW_FLAG;
            serverHealth -= 5.0;
        } else {
            icon = Icons.GREEN_THUMB;
        }
        StringBuilder subNotes = new StringBuilder();
        perServerContainers.stream()
                .sorted(Comparator.comparingInt(c -> 0 - c.getUnsafe(AnalysisKeys.AVG_PLAYERS_MONTH)))
                .map(c -> {
                    int playersPerMonth = c.getUnsafe(AnalysisKeys.AVG_PLAYERS_MONTH);
                    Server server = c.getUnsafe(serverKey);
                    return SUB_NOTE + (playersPerMonth >= average && playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon + " " + decimalFormatter.apply(average) + uniquePlayersNote + subNotes.toString());
    }

    private void newPlayersNote(int serverCount, Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon;
        String newPlayersNote = locale.getString(HealthInfoLang.PLAYER_REGISTER_PER_SERVER);
        double average = perServerContainers.stream()
                .mapToInt(c -> c.getUnsafe(AnalysisKeys.AVG_PLAYERS_NEW_MONTH))
                .average().orElse(0.0);
        if (average < 1) {
            icon = Icons.RED_WARN;
            serverHealth -= 10.0;
        } else if (average < serverCount) {
            icon = Icons.YELLOW_FLAG;
            serverHealth -= 5.0;
        } else {
            icon = Icons.GREEN_THUMB;
        }
        StringBuilder subNotes = new StringBuilder();
        perServerContainers.stream()
                .sorted(Comparator.comparingInt(c -> 0 - c.getUnsafe(AnalysisKeys.AVG_PLAYERS_NEW_MONTH)))
                .map(c -> {
                    int playersPerMonth = c.getUnsafe(AnalysisKeys.AVG_PLAYERS_NEW_MONTH);
                    Server server = c.getUnsafe(serverKey);
                    return SUB_NOTE + (playersPerMonth >= average && playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon + " " + decimalFormatter.apply(average) + newPlayersNote + subNotes.toString());
    }

    private List<DataContainer> getPerServerContainers(PlayersMutator playersMutator, Collection<Server> servers, Key<Server> serverKey) {
        List<DataContainer> perServerContainers = new ArrayList<>();

        for (Server server : servers) {
            UUID serverUUID = server.getUuid();
            DataContainer serverContainer = new SupplierDataContainer();
            serverContainer.putRawData(serverKey, server);

            PlayersMutator serverPlayers = playersMutator.filterPlayedOnServer(serverUUID);
            PlayersMutator serverRegistered = serverPlayers.filterRegisteredBetween(monthAgo, now);
            int averageNewPerDay = serverRegistered.averageNewPerDay(timeZone);
            serverContainer.putRawData(AnalysisKeys.AVG_PLAYERS_NEW_MONTH, averageNewPerDay);
            SessionsMutator serverSessions = new SessionsMutator(serverPlayers.getSessions())
                    .filterSessionsBetween(monthAgo, now)
                    .filterPlayedOnServer(serverUUID);
            int averageUniquePerDay = serverSessions.toAverageUniqueJoinsPerDay(timeZone);
            int uniquePlayers = serverSessions.toUniquePlayers();
            serverContainer.putRawData(AnalysisKeys.AVG_PLAYERS_MONTH, averageUniquePerDay);
            serverContainer.putRawData(AnalysisKeys.PLAYERS_MONTH, uniquePlayers);

            perServerContainers.add(serverContainer);
        }
        return perServerContainers;
    }

    private void playersNote(Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon = Icons.HELP_RING;
        String uniquePlayersNote = "${playersMonth}" + locale.getString(HealthInfoLang.PLAYER_PLAY_ON_NETWORK);
        StringBuilder subNotes = new StringBuilder();
        perServerContainers.stream()
                .sorted(Comparator.comparingInt(c -> 0 - c.getUnsafe(AnalysisKeys.PLAYERS_MONTH)))
                .map(c -> {
                    int playersPerMonth = c.getUnsafe(AnalysisKeys.PLAYERS_MONTH);
                    Server server = c.getUnsafe(serverKey);
                    return SUB_NOTE + (playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon.toHtml() + " " + uniquePlayersNote + subNotes.toString());
    }
}
