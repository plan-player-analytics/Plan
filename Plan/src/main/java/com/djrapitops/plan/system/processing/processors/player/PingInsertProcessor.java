/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Processes 60s values of a Ping list.
 * <p>
 * Ping list contains 30 values as ping is updated every 2 seconds.
 *
 * @author Rsl1122
 */
public class PingInsertProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final List<DateObj<Integer>> pingList;

    public PingInsertProcessor(UUID uuid, List<DateObj<Integer>> pingList) {
        this.uuid = uuid;
        this.pingList = pingList;
    }

    @Override
    public void run() {
        List<DateObj<Integer>> history = pingList;
        long lastDate = history.get(history.size() - 1).getDate();
        OptionalInt max = history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i >= 0)
                .max();

        if (!max.isPresent()) {
            return;
        }

        int minValue = history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i >= 0)
                .min().orElse(-1);

        double avgValue = history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i >= 0)
                .average().orElse(-1);

        int maxValue = max.getAsInt();

        Ping ping = new Ping(lastDate, ServerInfo.getServerUUID(),
                minValue,
                maxValue,
                avgValue);

        Database.getActive().save().ping(uuid, ping);
    }
}
