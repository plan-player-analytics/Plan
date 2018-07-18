package com.djrapitops.plan.utilities.html.graphs.bar;

import com.djrapitops.plan.data.store.mutators.PlayersMutator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeolocationBarGraph extends BarGraph {

    public GeolocationBarGraph(PlayersMutator mutator) {
        this(mutator.getGeolocations());
    }

    public GeolocationBarGraph(List<String> geolocations) {
        super(turnToBars(geolocations));
    }

    private static List<Bar> turnToBars(List<String> geolocations) {
        Map<String, Integer> counts = new HashMap<>();

        for (String geolocation : geolocations) {
            counts.put(geolocation, counts.getOrDefault(geolocation, 0) + 1);
        }

        return counts.entrySet().stream()
                .map(entry -> new Bar(entry.getKey(), entry.getValue()))
                .sorted()
                .limit(20L)
                .collect(Collectors.toList());
    }
}
