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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.AveragePing;
import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.CommonKeys;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.utilities.analysis.Median;
import com.djrapitops.plan.utilities.comparators.DateHolderOldestComparator;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mutator for a list of Sessions.
 * <p>
 * Can be used to get properties of a large number of sessions easily.
 *
 * @author AuroraLS3
 */
public class SessionsMutator {

    private final List<FinishedSession> sessions;

    public static SessionsMutator forContainer(DataContainer container) {
        return new SessionsMutator(container.getValue(CommonKeys.SESSIONS).orElse(new ArrayList<>()));
    }

    public SessionsMutator(List<FinishedSession> sessions) {
        this.sessions = sessions;
    }

    public static Map<UUID, List<FinishedSession>> sortByPlayers(List<FinishedSession> sessions) {
        Map<UUID, List<FinishedSession>> sorted = new HashMap<>();
        for (FinishedSession session : sessions) {
            UUID playerUUID = session.getPlayerUUID();
            List<FinishedSession> playerSessions = sorted.computeIfAbsent(playerUUID, Lists::create);
            playerSessions.add(session);
            sorted.put(playerUUID, playerSessions);
        }
        return sorted;
    }

    public SessionsMutator sort(Comparator<DateHolder> sessionComparator) {
        sessions.sort(sessionComparator);
        return this;
    }

    public static Map<ServerUUID, List<FinishedSession>> sortByServers(List<FinishedSession> sessions) {
        Map<ServerUUID, List<FinishedSession>> sorted = new HashMap<>();
        for (FinishedSession session : sessions) {
            ServerUUID serverUUID = session.getServerUUID();
            List<FinishedSession> serverSessions = sorted.computeIfAbsent(serverUUID, Lists::create);
            serverSessions.add(session);
        }
        return sorted;
    }

    public SessionsMutator filterSessionsBetween(long after, long before) {
        return filterBy(getBetweenPredicate(after, before));
    }

    public static Map<ServerUUID, TreeMap<Long, FinishedSession>> sortByServersToMaps(List<FinishedSession> sessions) {
        Map<ServerUUID, TreeMap<Long, FinishedSession>> sorted = new HashMap<>();
        for (FinishedSession session : sessions) {
            ServerUUID serverUUID = session.getServerUUID();
            TreeMap<Long, FinishedSession> serverSessions = sorted.getOrDefault(serverUUID, new TreeMap<>());
            serverSessions.put(session.getDate(), session);
            sorted.put(serverUUID, serverSessions);
        }
        return sorted;
    }

    public List<FinishedSession> all() {
        return sessions;
    }

    public TimeSegmentsMutator<Integer> onlineTimeSegments() {
        return TimeSegmentsMutator.sessionClockSegments(sort(new DateHolderOldestComparator()).all());
    }

    public SessionsMutator filterPlayedOnServer(ServerUUID serverUUID) {
        return filterBy(session ->
                session.getServerUUID().equals(serverUUID)
        );
    }

    public DateHoldersMutator<FinishedSession> toDateHoldersMutator() {
        return new DateHoldersMutator<>(sessions);
    }

    public WorldTimes toTotalWorldTimes() {
        WorldTimes total = new WorldTimes();

        for (FinishedSession session : sessions) {
            session.getExtraData(WorldTimes.class).ifPresent(total::add);
        }

        return total;
    }

    public List<PlayerKill> toPlayerKillList() {
        return sessions.stream()
                .map(session -> session.getExtraData(PlayerKills.class).map(PlayerKills::asList).orElseGet(ArrayList::new))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public int toMobKillCount() {
        return sessions.stream()
                .mapToInt(FinishedSession::getMobKillCount)
                .sum();
    }

    public int toDeathCount() {
        return sessions.stream()
                .mapToInt(FinishedSession::getDeathCount)
                .sum();
    }

    public long toPlaytime() {
        return sessions.stream()
                .mapToLong(FinishedSession::getLength)
                .sum();
    }

    public long toAfkTime() {
        return sessions.stream()
                .mapToLong(FinishedSession::getAfkTime)
                .sum();
    }

    public long toActivePlaytime() {
        return sessions.stream()
                .mapToLong(FinishedSession::getActiveTime)
                .sum();
    }

    public long toLastSeen() {
        return sessions.stream()
                .mapToLong(session -> Math.max(session.getStart(), session.getEnd())).max()
                .orElse(-1);
    }

    public long toLongestSessionLength() {
        OptionalLong longestSession = sessions.stream().mapToLong(FinishedSession::getLength).max();
        if (longestSession.isPresent()) {
            return longestSession.getAsLong();
        }
        return -1;
    }

    public long toAverageSessionLength() {
        OptionalDouble average = sessions.stream().map(FinishedSession::getLength)
                .mapToLong(i -> i)
                .average();
        if (average.isPresent()) {
            return (long) average.getAsDouble();
        }
        return 0L;
    }

    public int count() {
        return sessions.size();
    }

    public int toPlayerKillCount() {
        return toPlayerKillList().size();
    }

    public boolean playedBetween(long after, long before) {
        return sessions.stream().anyMatch(getBetweenPredicate(after, before));
    }

    public int toUniquePlayers() {
        return (int) sessions.stream()
                .map(FinishedSession::getPlayerUUID)
                .distinct()
                .count();
    }

    private Predicate<FinishedSession> getBetweenPredicate(long after, long before) {
        return session -> {
            long start = session.getStart();
            long end = session.getEnd();
            return after <= end && start <= before;
        };
    }

    public SessionsMutator filterBy(Predicate<FinishedSession> predicate) {
        return new SessionsMutator(Lists.filter(sessions, predicate));
    }

    public long toMedianSessionLength() {
        List<Long> sessionLengths = Lists.map(sessions, FinishedSession::getLength);
        return (long) Median.forList(sessionLengths).calculate();
    }

    public int toPlayerDeathCount() {
        return sessions.stream().mapToInt(FinishedSession::getDeathCount).sum();
    }

    public List<Long> toSessionStarts() {
        return sessions.stream()
                .map(FinishedSession::getStart)
                .sorted()
                .collect(Collectors.toList());
    }

    public double toAveragePlayersOnline(PlayersOnlineResolver playersOnlineResolver) {
        return sessions.stream().map(session -> playersOnlineResolver.getOnlineOn(session.getDate()))
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .average().orElse(0.0);
    }

    public List<Map<String, Object>> toPlayerNameJSONMaps(
            Graphs graphs,
            WorldAliasSettings worldAliasSettings,
            Formatters formatters
    ) {
        return toJSONMaps(graphs, worldAliasSettings, formatters,
                sessionMap -> sessionMap.get("player_name"));
    }

    public List<Map<String, Object>> toServerNameJSONMaps(
            Graphs graphs,
            WorldAliasSettings worldAliasSettings,
            Formatters formatters
    ) {
        return toJSONMaps(graphs, worldAliasSettings, formatters,
                sessionMap -> sessionMap.get("server_name"));
    }

    private List<Map<String, Object>> toJSONMaps(
            Graphs graphs,
            WorldAliasSettings worldAliasSettings,
            Formatters formatters,
            Function<Map<String, Object>, Object> nameFunction
    ) {
        return Lists.map(sessions, session -> {
            Map<String, Object> sessionMap = new HashMap<>();
            String playerUUID = session.getPlayerUUID().toString();
            String serverUUID = session.getServerUUID().toString();
            String playerName = session.getExtraData(PlayerName.class).map(PlayerName::get).orElse(playerUUID);
            String serverName = session.getExtraData(ServerName.class).map(ServerName::get).orElse(serverUUID);
            sessionMap.put("player_name", playerName);
            sessionMap.put("player_url_name", Html.encodeToURL(playerName));
            sessionMap.put("player_uuid", playerUUID);
            sessionMap.put("server_name", serverName);
            sessionMap.put("server_url_name", Html.encodeToURL(serverName));
            sessionMap.put("server_uuid", serverUUID);
            sessionMap.put("name", nameFunction.apply(sessionMap));
            sessionMap.put("online", session.getExtraData(ActiveSession.class).isPresent());
            sessionMap.put("start", session.getStart());
            sessionMap.put("end", session.getEnd());
            sessionMap.put("most_used_world", worldAliasSettings.getLongestWorldPlayed(session));
            sessionMap.put("length", session.getLength());
            sessionMap.put("afk_time", session.getAfkTime());
            sessionMap.put("mob_kills", session.getMobKillCount());
            sessionMap.put("deaths", session.getDeathCount());
            sessionMap.put("player_kills", session.getExtraData(PlayerKills.class)
                    .map(PlayerKills::asMutator)
                    .map(killsMutator -> killsMutator.toJSONAsMap(formatters))
                    .orElseGet(ArrayList::new));
            sessionMap.put("first_session", session.isFirstSession());
            WorldPie worldPie = graphs.pie().worldPie(session.getExtraData(WorldTimes.class).orElseGet(WorldTimes::new));
            sessionMap.put("world_series", worldPie.getSlices());
            sessionMap.put("gm_series", worldPie.toHighChartsDrillDownMaps());
            sessionMap.put("join_address", session.getExtraData(JoinAddress.class)
                    .map(JoinAddress::getAddress).orElse("-"));

            session.getExtraData(AveragePing.class).ifPresent(averagePing ->
                    sessionMap.put("avg_ping", formatters.decimals().apply(averagePing.getValue()) + " ms")
            );
            return sessionMap;
        });
    }

    public Optional<FinishedSession> latestSession() {
        List<FinishedSession> orderedSessions = sort(new DateHolderRecentComparator()).all();
        return orderedSessions.isEmpty() ? Optional.empty() : Optional.of(orderedSessions.get(0));
    }

    public Optional<FinishedSession> previousSession() {
        List<FinishedSession> orderedSessions = sort(new DateHolderRecentComparator()).all();
        for (FinishedSession session : orderedSessions) {
            if (session.getExtraData(ActiveSession.class).isPresent()) {
                continue;
            }
            // First non-active session is previous one.
            return Optional.of(session);
        }

        return Optional.empty();
    }
}