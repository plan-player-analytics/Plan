package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.volmit.react.React;
import com.volmit.react.api.GraphSampleLine;
import com.volmit.react.api.SampledType;
import com.volmit.react.util.M;
import com.volmit.react.volume.lang.collections.GMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Task in charge of collecting data from React.
 *
 * @author Rsl1122
 */
public class ReactDataTask extends AbsRunnable {

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
    private final Map<SampledType, List<ReactValue>> history;

    public ReactDataTask(ReactDataTable table) {
        super(ReactDataTask.class.getSimpleName());
        this.table = table;
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
            Log.toLog(this.getClass(), e);
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

        if (storedValues.get(0).getDate() < System.currentTimeMillis() - TimeAmount.MINUTE.ms()) {
            Processing.submitNonCritical(new ValueStoringProcessor(table, type, storedValues));
            history.remove(type);
        } else {
            history.put(type, storedValues);
        }
    }
}