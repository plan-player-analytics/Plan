package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plugin.api.utility.log.Log;
import com.volmit.react.api.SampledType;

import java.sql.SQLException;
import java.util.List;

/**
 * Processor in charge of turning values into single numbers.
 *
 * @author Rsl1122
 */
public class ValueStoringProcessor implements Runnable {

    private final ReactDataTable table;
    private final SampledType type;
    private final List<ReactValue> values;

    public ValueStoringProcessor(ReactDataTable table, SampledType type, List<ReactValue> values) {
        this.table = table;
        this.type = type;
        this.values = values;
    }

    @Override
    public void run() {
        ReactValue average = avgValue(values);

        if (average == null) {
            return;
        }

        try {
            table.addData(average);
        } catch (SQLException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private ReactValue avgValue(List<ReactValue> values) {
        if (values.isEmpty()) {
            return null;
        }

        long date = values.get(0).getDate();
        double average = values.stream().mapToDouble(ReactValue::getDataValue).average().orElse(0.0);

        return new ReactValue(type, date, average);
    }
}