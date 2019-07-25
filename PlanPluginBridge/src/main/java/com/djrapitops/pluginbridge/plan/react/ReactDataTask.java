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
package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.task.AbsRunnable;
import com.volmit.react.React;
import com.volmit.react.api.GraphSampleLine;
import com.volmit.react.api.SampledType;
import com.volmit.react.util.M;
import primal.lang.collection.GMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Task in charge of collecting data from React.
 *
 * @author Rsl1122
 */
class ReactDataTask extends AbsRunnable {

    private static final SampledType[] STORED_TYPES = new SampledType[]{
            SampledType.ENT,
            SampledType.ENTLIV,
            SampledType.ENTDROP,
            SampledType.ENTTILE,

            SampledType.TIU,
            SampledType.HOPPER_TICK_USAGE,
            SampledType.FLUID_TICK_USAGE,
            SampledType.REDSTONE_TICK_USAGE,

            SampledType.TICK,
            SampledType.TILE_DROPTICK,
            SampledType.FLUID_TICK,
            SampledType.HOPPER_TICK,
            SampledType.ENTITY_DROPTICK,
            SampledType.REDSTONE_TICK,

            SampledType.REACT_TASK_TIME
    };
    private final ReactDataTable table;
    private final Processing processing;
    private final Map<SampledType, List<ReactValue>> history;

    public ReactDataTask(ReactDataTable table, Processing processing) {
        this.table = table;
        this.processing = processing;
        history = new EnumMap<>(SampledType.class);
    }

    @Override
    public void run() {
        try {
            GMap<SampledType, GraphSampleLine> samplers = React.instance.graphController.getG();

            for (SampledType type : STORED_TYPES) {
                processType(samplers, type);
            }
        } catch (Exception e) {
            cancel();
        }
    }

    private void processType(GMap<SampledType, GraphSampleLine> samplers, SampledType type) {
        GMap<Long, Double> values = samplers.get(type).getPlotBoard().getBetween(M.ms() - 10000, M.ms());
        if (values.isEmpty()) {
            return;
        }
        List<ReactValue> storedValues = history.getOrDefault(type, new ArrayList<>());
        values.entrySet().stream()
                .map(entry -> new ReactValue(type, entry.getKey(), entry.getValue()))
                .sorted()
                .forEachOrdered(storedValues::add);

        if (storedValues.get(0).getDate() < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1L)) {
            processing.submitNonCritical(new ValueStoringProcessor(table, type, storedValues));
            history.remove(type);
        } else {
            history.put(type, storedValues);
        }
    }
}