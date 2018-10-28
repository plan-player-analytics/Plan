/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
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
        return (int) Median.forList(history.stream().map(DateObj::getValue).collect(Collectors.toList())).calculate();
    }
}
