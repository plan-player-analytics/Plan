/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.utilities.analysis.Median;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Processes 60s values of a Ping list.
 * <p>
 * Ping list contains 30 values as ping is updated every 2 seconds.
 *
 * @author Rsl1122
 */
public class PingInsertProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final UUID serverUUID;
    private final List<DateObj<Integer>> pingList;

    private final Database database;

    PingInsertProcessor(
            UUID uuid, UUID serverUUID, List<DateObj<Integer>> pingList,
            Database database
    ) {
        this.uuid = uuid;
        this.serverUUID = serverUUID;
        this.pingList = pingList;
        this.database = database;
    }

    @Override
    public void run() {
        List<DateObj<Integer>> history = pingList;
        long lastDate = history.get(history.size() - 1).getDate();
        OptionalInt max = history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i > 0 && i < 4000)
                .max();

        if (!max.isPresent()) {
            return;
        }

        int minValue = getMinValue(history);

        int meanValue = getMeanValue(history);

        int maxValue = max.getAsInt();

        Ping ping = new Ping(lastDate, serverUUID, minValue, maxValue, meanValue);

        database.save().ping(uuid, ping);
    }

    int getMinValue(List<DateObj<Integer>> history) {
        return history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i > 0 && i < 4000)
                .min().orElse(-1);
    }

    int getMeanValue(List<DateObj<Integer>> history) {
        return (int) Median.forInt(history.stream().map(DateObj::getValue).collect(Collectors.toList())).calculate();
    }
}
