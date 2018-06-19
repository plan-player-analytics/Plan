package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mutator for a bunch of {@link com.djrapitops.plan.data.store.containers.PlayerContainer}s.
 *
 * @author Rsl1122
 */
public class PlayersMutator {

    private List<PlayerContainer> players;

    public PlayersMutator(List<PlayerContainer> players) {
        this.players = players;
    }

    public static PlayersMutator copyOf(PlayersMutator mutator) {
        return new PlayersMutator(new ArrayList<>(mutator.players));
    }

    public static PlayersMutator forContainer(DataContainer container) {
        return new PlayersMutator(container.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>()));
    }

    public PlayersMutator filterPlayedBetween(long after, long before) {
        players = players.stream().filter(player ->
                player.getValue(PlayerKeys.SESSIONS)
                        .map(sessions -> sessions.stream().anyMatch(session -> {
                            long start = session.getValue(SessionKeys.START).orElse(-1L);
                            long end = session.getValue(SessionKeys.END).orElse(-1L);
                            return (after <= start && start <= before) || (after <= end && end <= before);
                        })).orElse(false)
        ).collect(Collectors.toList());
        return this;
    }

    public PlayersMutator filterRegisteredBetween(long after, long before) {
        players = players.stream().filter(player ->
                player.getValue(PlayerKeys.REGISTERED).map(date -> after <= date && date <= before).orElse(false)
        ).collect(Collectors.toList());
        return this;
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
            mostRecent.ifPresent(geoInfo -> geolocations.add(geoInfo.getGeolocation()));
        }

        return geolocations;
    }

    public TreeMap<Long, Map<String, Set<UUID>>> toActivityDataMap(long date) {
        TreeMap<Long, Map<String, Set<UUID>>> activityData = new TreeMap<>();
        if (!players.isEmpty()) {
            for (PlayerContainer player : players) {
                for (long time = date; time >= time - TimeAmount.MONTH.ms() * 2L; time -= TimeAmount.WEEK.ms()) {
                    ActivityIndex activityIndex = new ActivityIndex(player, time);
                    String activityGroup = activityIndex.getGroup();

                    Map<String, Set<UUID>> map = activityData.getOrDefault(time, new HashMap<>());
                    Set<UUID> uuids = map.getOrDefault(activityGroup, new HashSet<>());
                    uuids.add(player.getUnsafe(PlayerKeys.UUID));
                    map.put(activityGroup, uuids);
                    activityData.put(time, map);
                }
            }
        }
        return activityData;
    }

    public int count() {
        return players.size();
    }

    public int newPerDay() {
        List<Long> registerDates = registerDates();
        int total = 0;
        Function<Long, Integer> formatter = Formatters.dayOfYear();
        Set<Integer> days = new HashSet<>();
        for (Long date : registerDates) {
            int day = formatter.apply(date);
            days.add(day);
            total++;
        }
        int numberOfDays = days.size();

        if (numberOfDays == 0) {
            return 0;
        }
        return total / numberOfDays;
    }
}