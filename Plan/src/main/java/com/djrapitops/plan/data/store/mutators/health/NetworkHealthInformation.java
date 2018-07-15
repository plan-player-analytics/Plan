package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;

import java.util.*;

public class NetworkHealthInformation extends AbstractHealthInfo {

    private final NetworkContainer container;

    public NetworkHealthInformation(NetworkContainer container) {
        super(
                container.getUnsafe(NetworkKeys.REFRESH_TIME),
                container.getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO)
        );
        this.container = container;
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
            addNote(Icons.HELP_RING + " No Bukkit/Sponge servers to gather session data - These measures are inaccurate.");
            return;
        }
        int serverCount = servers.size();
        if (serverCount == 1) {
            addNote(Icons.HELP_RING + " Single Bukkit/Sponge server to gather session data.");
            return;
        }

        Key<Server> serverKey = new Key<>(Server.class, "SERVER");

        List<DataContainer> perServerContainers = getPerServerContainers(playersMutator, servers, serverKey);

        uniquePlayersNote(serverCount, serverKey, perServerContainers);
        newPlayersNote(serverCount, serverKey, perServerContainers);
        playersNote(serverCount, serverKey, perServerContainers);
    }

    private void uniquePlayersNote(int serverCount, Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon;
        String uniquePlayersNote = " players visit on servers per day/server on average.";
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
                    return subNote + (playersPerMonth >= average && playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon + " " + FormatUtils.cutDecimals(average) + uniquePlayersNote + subNotes.toString());
    }

    private void newPlayersNote(int serverCount, Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon;
        String newPlayersNote = " players register on servers per day/server on average.";
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
                    return subNote + (playersPerMonth >= average && playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon + " " + FormatUtils.cutDecimals(average) + newPlayersNote + subNotes.toString());
    }

    private List<DataContainer> getPerServerContainers(PlayersMutator playersMutator, Collection<Server> servers, Key<Server> serverKey) {
        List<DataContainer> perServerContainers = new ArrayList<>();

        for (Server server : servers) {
            UUID serverUUID = server.getUuid();
            DataContainer serverContainer = new DataContainer();
            serverContainer.putRawData(serverKey, server);

            PlayersMutator serverPlayers = playersMutator.filterPlayedOnServer(serverUUID);
            PlayersMutator serverRegistered = serverPlayers.filterRegisteredBetween(monthAgo, now);
            int averageNewPerDay = serverRegistered.averageNewPerDay();
            serverContainer.putRawData(AnalysisKeys.AVG_PLAYERS_NEW_MONTH, averageNewPerDay);
            SessionsMutator serverSessions = new SessionsMutator(serverPlayers.getSessions())
                    .filterSessionsBetween(monthAgo, now)
                    .filterPlayedOnServer(serverUUID);
            int averageUniquePerDay = serverSessions.toAverageUniqueJoinsPerDay();
            int uniquePlayers = serverSessions.toUniquePlayers();
            serverContainer.putRawData(AnalysisKeys.AVG_PLAYERS_MONTH, averageUniquePerDay);
            serverContainer.putRawData(AnalysisKeys.PLAYERS_MONTH, uniquePlayers);

            perServerContainers.add(serverContainer);
        }
        return perServerContainers;
    }

    private void playersNote(int serverCount, Key<Server> serverKey, List<DataContainer> perServerContainers) {
        Icon icon = Icons.HELP_RING;
        String uniquePlayersNote = "${playersMonth} players played on the network:";
        StringBuilder subNotes = new StringBuilder();
        perServerContainers.stream()
                .sorted(Comparator.comparingInt(c -> 0 - c.getUnsafe(AnalysisKeys.PLAYERS_MONTH)))
                .map(c -> {
                    int playersPerMonth = c.getUnsafe(AnalysisKeys.PLAYERS_MONTH);
                    Server server = c.getUnsafe(serverKey);
                    return subNote + (playersPerMonth > 0 ? Icons.GREEN_PLUS : Icons.RED_MINUS) + " " +
                            server.getName() + ": " + playersPerMonth;
                }).forEach(subNotes::append);
        addNote(icon.toHtml() + " " + uniquePlayersNote + subNotes.toString());
    }
}
