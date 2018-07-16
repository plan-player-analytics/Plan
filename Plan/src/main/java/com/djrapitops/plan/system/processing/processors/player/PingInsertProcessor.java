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
 * Processes 60s average of a Ping list.
 * <p>
 * Ping list contains 30 values as ping is updated every 2 seconds.
 *
 * @author Rsl1122
 */
public class PingInsertProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final List<Ping> pingList;

    public PingInsertProcessor(UUID uuid, List<Ping> pingList) {
        this.uuid = uuid;
        this.pingList = pingList;
    }

    @Override
    public void run() {
        List<Ping> history = pingList;
        long lastDate = history.get(history.size() - 1).getDate();
        OptionalInt max = history.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i != -1)
                .max();

        if (!max.isPresent()) {
            return;
        }

        int maxValue = max.getAsInt();

        Ping ping = new Ping(lastDate, maxValue, ServerInfo.getServerUUID());

        Database.getActive().save().ping(uuid, ping);
    }
}
