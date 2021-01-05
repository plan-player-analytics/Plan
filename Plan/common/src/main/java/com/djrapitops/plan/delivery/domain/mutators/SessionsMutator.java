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

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.CommonKeys;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.utilities.analysis.Median;
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
 * @author Rsl1122
 */
public class SessionsMutator {

    private final List<Session> sessions;

    public static SessionsMutator forContainer(DataContainer container) {
        return new SessionsMutator(container.getValue(CommonKeys.SESSIONS).orElse(new ArrayList<>()));
    }

    public SessionsMutator(List<Session> sessions) {
        this.sessions = sessions;
    }

    public List<Session> all() {
        return sessions;
    }

    public SessionsMutator sort(Comparator<DateHolder> sessionComparator) {
        sessions.sort(sessionComparator);
        return this;
    }

    public static Map<UUID, List<Session>> sortByPlayers(List<Session> sessions) {
        Map<UUID, List<Session>> sorted = new HashMap<>();
        for (Session session : sessions) {
            UUID playerUUID = session.getUnsafe(SessionKeys.UUID);
            List<Session> playerSessions = sorted.computeIfAbsent(playerUUID, Lists::create);
            playerSessions.add(session);
            sorted.put(playerUUID, playerSessions);
        }
        return sorted;
    }

    public SessionsMutator filterSessionsBetween(long after, long before) {
        return filterBy(getBetweenPredicate(after, before));
    }

    public SessionsMutator filterPlayedOnServer(UUID serverUUID) {
        return filterBy(session ->
                session.getValue(SessionKeys.SERVER_UUID)
                        .map(uuid -> uuid.equals(serverUUID))
                        .orElse(false)
        );
    }

    public DateHoldersMutator<Session> toDateHoldersMutator() {
        return new DateHoldersMutator<>(sessions);
    }

    public WorldTimes toTotalWorldTimes() {
        WorldTimes total = new WorldTimes();

        for (Session session : sessions) {
            session.getValue(SessionKeys.WORLD_TIMES).ifPresent(total::add);
        }

        return total;
    }

    public List<PlayerKill> toPlayerKillList() {
        return sessions.stream()
                .map(session -> session.getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public int toMobKillCount() {
        return sessions.stream()
                .mapToInt(session -> session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0))
                .sum();
    }

    public int toDeathCount() {
        return sessions.stream()
                .mapToInt(session -> session.getValue(SessionKeys.DEATH_COUNT).orElse(0))
                .sum();
    }

    public long toPlaytime() {
        return sessions.stream()
                .mapToLong(Session::getLength)
                .sum();
    }

    public long toAfkTime() {
        return sessions.stream()
                .mapToLong(session -> session.getValue(SessionKeys.AFK_TIME).orElse(0L))
                .sum();
    }

    public long toActivePlaytime() {
        return sessions.stream()
                .mapToLong(session -> session.getValue(SessionKeys.ACTIVE_TIME).orElse(0L))
                .sum();
    }

    public long toLastSeen() {
        return sessions.stream()
                .mapToLong(session -> Math.max(session.getUnsafe(
                        SessionKeys.START),
                        session.getValue(SessionKeys.END).orElse(System.currentTimeMillis()))
                ).max().orElse(-1);
    }

    public long toLongestSessionLength() {
        OptionalLong longestSession = sessions.stream().mapToLong(Session::getLength).max();
        if (longestSession.isPresent()) {
            return longestSession.getAsLong();
        }
        return -1;
    }

    public long toAverageSessionLength() {
        OptionalDouble average = sessions.stream().map(Session::getLength)
                .mapToLong(i -> i)
                .average();
        if (average.isPresent()) {
            return (long) average.getAsDouble();
        }
        return 0L;
    }

    public static Map<UUID, List<Session>> sortByServers(List<Session> sessions) {
        Map<UUID, List<Session>> sorted = new HashMap<>();
        for (Session session : sessions) {
            UUID serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID);
            List<Session> serverSessions = sorted.computeIfAbsent(serverUUID, Lists::create);
            serverSessions.add(session);
        }
        return sorted;
    }

    public int toUniquePlayers() {
        return (int) sessions.stream()
                .map(session -> session.getUnsafe(SessionKeys.UUID))
                .distinct()
                .count();
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

    private Predicate<Session> getBetweenPredicate(long after, long before) {
        return session -> {
            Long start = session.getUnsafe(SessionKeys.START);
            long end = session.getValue(SessionKeys.END).orElse(System.currentTimeMillis());
            return after <= end && start <= before;
        };
    }

    public SessionsMutator filterBy(Predicate<Session> predicate) {
        return new SessionsMutator(Lists.filter(sessions, predicate));
    }

    public long toMedianSessionLength() {
        List<Long> sessionLengths = Lists.map(sessions, Session::getLength);
        return (long) Median.forList(sessionLengths).calculate();
    }

    public static Map<UUID, TreeMap<Long, Session>> sortByServersToMaps(List<Session> sessions) {
        Map<UUID, TreeMap<Long, Session>> sorted = new HashMap<>();
        for (Session session : sessions) {
            UUID serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID);
            TreeMap<Long, Session> serverSessions = sorted.getOrDefault(serverUUID, new TreeMap<>());
            serverSessions.put(session.getDate(), session);
            sorted.put(serverUUID, serverSessions);
        }
        return sorted;
    }

    public int toPlayerDeathCount() {
        return sessions.stream().mapToInt(session -> session.getValue(SessionKeys.DEATH_COUNT).orElse(0)).sum();
    }

    public List<Long> toSessionStarts() {
        return sessions.stream()
                .map(Session::getDate)
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
            String playerUUID = session.getUnsafe(SessionKeys.UUID).toString();
            String serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID).toString();
            String playerName = session.getValue(SessionKeys.NAME).orElse(playerUUID);
            String serverName = session.getValue(SessionKeys.SERVER_NAME).orElse(serverUUID);
            sessionMap.put("player_name", playerName);
            sessionMap.put("player_url_name", Html.encodeToURL(playerName));
            sessionMap.put("player_uuid", playerUUID);
            sessionMap.put("server_name", serverName);
            sessionMap.put("server_url_name", Html.encodeToURL(serverName));
            sessionMap.put("server_uuid", serverUUID);
            sessionMap.put("name", nameFunction.apply(sessionMap));
            sessionMap.put("start", session.getValue(SessionKeys.START).map(formatters.yearLong()).orElse("-") +
                    (session.supports(SessionKeys.END) ? "" : " (Online)"));
            sessionMap.put("end", session.getValue(SessionKeys.END).map(formatters.yearLong()).orElse("Online"));
            sessionMap.put("most_used_world", worldAliasSettings.getLongestWorldPlayed(session));
            sessionMap.put("length", session.getValue(SessionKeys.LENGTH).map(formatters.timeAmount()).orElse("-"));
            sessionMap.put("afk_time", session.getValue(SessionKeys.AFK_TIME).map(formatters.timeAmount()).orElse("-"));
            sessionMap.put("mob_kills", session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0));
            sessionMap.put("deaths", session.getValue(SessionKeys.DEATH_COUNT).orElse(0));
            sessionMap.put("player_kills", session.getPlayerKills().stream().map(
                    kill -> {
                        Map<String, Object> killMap = new HashMap<>();
                        killMap.put("date", formatters.secondLong().apply(kill.getDate()));
                        killMap.put("victim", kill.getVictimName().orElse(kill.getVictim().toString()));
                        killMap.put("killer", playerName);
                        killMap.put("weapon", kill.getWeapon());
                        return killMap;
                    }
            ).collect(Collectors.toList()));
            sessionMap.put("first_session", session.getValue(SessionKeys.FIRST_SESSION).orElse(false));
            WorldPie worldPie = graphs.pie().worldPie(session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes()));
            sessionMap.put("world_series", worldPie.getSlices());
            sessionMap.put("gm_series", worldPie.toHighChartsDrillDownMaps());

            session.getValue(SessionKeys.AVERAGE_PING).ifPresent(averagePing ->
                    sessionMap.put("avg_ping", formatters.decimals().apply(averagePing) + " ms")
            );
            return sessionMap;
        });
    }
}