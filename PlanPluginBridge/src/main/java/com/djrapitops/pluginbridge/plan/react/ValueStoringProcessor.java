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

import com.volmit.react.api.SampledType;

import java.util.List;

/**
 * Processor in charge of turning values into single numbers.
 *
 * @author Rsl1122
 */
class ValueStoringProcessor implements Runnable {

    private final ReactDataTable table;
    private final SampledType type;
    private final List<ReactValue> values;

    ValueStoringProcessor(ReactDataTable table, SampledType type, List<ReactValue> values) {
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

        table.addData(average);
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