package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.mutators.PingMutator;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.*;

public class PingTable extends TableContainer {

    public PingTable(Map<String, List<Ping>> pingPerCountry) {
        super(
                Icon.called("globe") + " Country",
                Icon.called("signal") + " Average Ping",
                Icon.called("signal") + " Worst Ping",
                Icon.called("signal") + " Best Ping"
        );
        setColor("amber");

        addRows(pingPerCountry);
    }

    private void addRows(Map<String, List<Ping>> pingPerCountry) {
        Map<String, Double> avg = new HashMap<>();
        Map<String, Integer> max = new HashMap<>();
        Map<String, Integer> min = new HashMap<>();

        for (Map.Entry<String, List<Ping>> entry : pingPerCountry.entrySet()) {
            PingMutator pingMutator = new PingMutator(entry.getValue());
            String country = entry.getKey();
            avg.put(country, pingMutator.average());
            max.put(country, pingMutator.max());
            min.put(country, pingMutator.min());
        }

        List<String> sortedKeys = new ArrayList<>(avg.keySet());
        Collections.sort(sortedKeys);

        for (String country : sortedKeys) {
            Double average = avg.get(country);
            Integer maximum = max.get(country);
            Integer minimum = min.get(country);
            addRow(
                    country,
                    average >= 0 ? FormatUtils.cutDecimals(average) + " ms" : "-",
                    maximum >= 0 ? maximum + " ms" : "-",
                    minimum >= 0 ? minimum + " ms" : "-"
            );
        }
    }
}
