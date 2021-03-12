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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.keys.ServerKeys;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;
import net.playeranalytics.plugin.scheduling.TimeAmount;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mutator for a bunch of {@link PlayerContainer}s.
 *
 * @author AuroraLS3
 */
public class PlayersMutator {

    private final List<PlayerContainer> players;

    public PlayersMutator(List<PlayerContainer> players) {
        this.players = players;
    }

    public static PlayersMutator copyOf(PlayersMutator mutator) {
        return new PlayersMutator(new ArrayList<>(mutator.players));
    }

    public static PlayersMutator forContainer(DataContainer container) {
        return new PlayersMutator(container.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>()));
    }

    public <T extends Predicate<PlayerContainer>> PlayersMutator filterBy(T by) {
        return new PlayersMutator(Lists.filter(players, by));
    }

    public PlayersMutator filterPlayedBetween(long after, long before) {
        return filterBy(
                player -> player.getValue(PlayerKeys.SESSIONS)
                        .map(sessions -> sessions.stream().anyMatch(session -> {
                            long start = session.getStart();
                            long end = session.getEnd();
                            return (after <= start && start <= before) || (after <= end && end <= before);
                        })).orElse(false)
        );
    }

    public PlayersMutator filterRegisteredBetween(long after, long before) {
        return filterBy(
                player -> player.getValue(PlayerKeys.REGISTERED)
                        .map(date -> after <= date && date <= before).orElse(false)
        );
    }

    public PlayersMutator filterRetained(long after, long before) {
        return filterBy(
                player -> {
                    long backLimit = Math.max(after, player.getValue(PlayerKeys.REGISTERED).orElse(0L));
                    long half = backLimit + ((before - backLimit) / 2L);
                    SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
                    return sessionsMutator.playedBetween(backLimit, half) &&
                            sessionsMutator.playedBetween(half, before);
                }
        );
    }

    public PlayersMutator filterActive(long date, long msThreshold, double limit) {
        return filterBy(player -> player.getActivityIndex(date, msThreshold).getValue() >= limit);
    }

    public PlayersMutator filterPlayedOnServer(ServerUUID serverUUID) {
        return filterBy(player -> !SessionsMutator.forContainer(player)
                .filterPlayedOnServer(serverUUID)
                .all().isEmpty()
        );
    }

    public List<PlayerContainer> all() {
        return players;
    }

    public List<Long> registerDates() {
        List<Long> registerDates = new ArrayList<>();
        for (PlayerContainer player : players) {
            registerDates.add(player.getValue(PlayerKeys.REGISTERED).orElse(-1L));
        }
        return registerDates;
    }

    public List<String> getGeolocations() {
        List<String> geolocations = new ArrayList<>();

        for (PlayerContainer player : players) {
            Optional<GeoInfo> mostRecent = GeoInfoMutator.forContainer(player).mostRecent();
            geolocations.add(mostRecent.map(GeoInfo::getGeolocation).orElse("Unknown"));
        }

        return geolocations;
    }

    public Map<String, List<Ping>> getPingPerCountry(ServerUUID serverUUID) {
        Map<String, List<Ping>> pingPerCountry = new HashMap<>();
        for (PlayerContainer player : players) {
            Optional<GeoInfo> mostRecent = GeoInfoMutator.forContainer(player).mostRecent();
            if (!mostRecent.isPresent()) {
                continue;
            }
            List<Ping> pings = player.getValue(PlayerKeys.PING).orElse(new ArrayList<>());
            String country = mostRecent.get().getGeolocation();
            List<Ping> countryPings = pingPerCountry.computeIfAbsent(country, Lists::create);
            countryPings.addAll(new PingMutator(pings).filterByServer(serverUUID).all());
        }

        return pingPerCountry;
    }

    public TreeMap<Long, Map<String, Set<UUID>>> toActivityDataMap(long date, long msThreshold) {
        TreeMap<Long, Map<String, Set<UUID>>> activityData = new TreeMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            Map<String, Set<UUID>> map = activityData.computeIfAbsent(time, Maps::create);
            if (!players.isEmpty()) {
                for (PlayerContainer player : players) {
                    if (player.getValue(PlayerKeys.REGISTERED).orElse(0L) > time) {
                        continue;
                    }
                    ActivityIndex activityIndex = player.getActivityIndex(time, msThreshold);
                    String activityGroup = activityIndex.getGroup();

                    Set<UUID> uuids = map.computeIfAbsent(activityGroup, Maps::createSet);
                    uuids.add(player.getUnsafe(PlayerKeys.UUID));
                }
            }
        }
        return activityData;
    }

    public int count() {
        return players.size();
    }

    public int averageNewPerDay(TimeZone timeZone) {
        return MutatorFunctions.average(newPerDay(timeZone));
    }

    public TreeMap<Long, Integer> newPerDay(TimeZone timeZone) {
        List<DateObj<?>> registerDates = Lists.map(registerDates(), value -> new DateObj<>(value, value));
        // Adds timezone offset
        SortedMap<Long, List<DateObj<?>>> byDay = new DateHoldersMutator<>(registerDates).groupByStartOfDay(timeZone);
        TreeMap<Long, Integer> byDayCounts = new TreeMap<>();

        for (Map.Entry<Long, List<DateObj<?>>> entry : byDay.entrySet()) {
            byDayCounts.put(
                    entry.getKey(),
                    entry.getValue().size()
            );
        }

        return byDayCounts;
    }

    /**
     * Compares players in the mutator to other players in terms of player retention.
     *
     * @param compareTo           Players to compare to.
     * @param dateLimit           Epoch ms back limit, if the player registered after this their value is not used.
     * @param onlineResolver      Thing that figures out how many players were online at different dates.
     * @param activityMsThreshold Threshold for activity index calculation.
     * @return Mutator containing the players that are considered to be retained.
     * @throws IllegalStateException If all players are rejected due to dateLimit.
     */
    public PlayersMutator compareAndFindThoseLikelyToBeRetained(
            Iterable<PlayerContainer> compareTo,
            long dateLimit,
            PlayersOnlineResolver onlineResolver,
            long activityMsThreshold
    ) {
        Collection<PlayerContainer> retainedAfterMonth = new ArrayList<>();
        Collection<PlayerContainer> notRetainedAfterMonth = new ArrayList<>();

        for (PlayerContainer player : players) {
            long registered = player.getValue(PlayerKeys.REGISTERED).orElse(System.currentTimeMillis());

            // Discard uncertain data
            if (registered > dateLimit) {
                continue;
            }

            long monthAfterRegister = registered + TimeAmount.MONTH.toMillis(1L);
            long half = registered + (TimeAmount.MONTH.toMillis(1L) / 2L);
            if (player.playedBetween(registered, half) && player.playedBetween(half, monthAfterRegister)) {
                retainedAfterMonth.add(player);
            } else {
                notRetainedAfterMonth.add(player);
            }
        }

        if (retainedAfterMonth.isEmpty() || notRetainedAfterMonth.isEmpty()) {
            throw new IllegalStateException("No players to compare to after rejecting with dateLimit");
        }

        Function<PlayerContainer, RetentionData> mapper = player -> new RetentionData(player, onlineResolver, activityMsThreshold);
        List<RetentionData> retained = Lists.map(retainedAfterMonth, mapper);
        List<RetentionData> notRetained = Lists.map(notRetainedAfterMonth, mapper);

        RetentionData avgRetained = RetentionData.average(retained);
        RetentionData avgNotRetained = RetentionData.average(notRetained);

        List<PlayerContainer> toBeRetained = new ArrayList<>();
        for (PlayerContainer player : compareTo) {
            RetentionData retentionData = new RetentionData(player, onlineResolver, activityMsThreshold);
            if (retentionData.distance(avgRetained) < retentionData.distance(avgNotRetained)) {
                toBeRetained.add(player);
            }
        }
        return new PlayersMutator(toBeRetained);
    }

    public List<FinishedSession> getSessions() {
        return players.stream()
                .map(player -> player.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<UUID> uuids() {
        return players.stream()
                .map(player -> player.getValue(PlayerKeys.UUID).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<PlayerContainer> operators() {
        return Lists.filter(players, player -> player.getValue(PlayerKeys.OPERATOR).orElse(false));
    }

    public List<Ping> pings() {
        return players.stream()
                .map(player -> player.getValue(PlayerKeys.PING).orElse(new ArrayList<>()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}