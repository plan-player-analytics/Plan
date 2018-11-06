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
package com.djrapitops.plan.system.database.databases.sql.processing;

import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plugin.benchmarking.Timings;

/**
 * Abstract class that performs an SQL statement.
 * <p>
 * For Benchmarking purposes.
 *
 * @author Rsl1122
 */
public abstract class AbstractSQLStatement {

    protected final String sql;

    private Timings timings;

    protected AbstractSQLStatement(String sql) {
        this.sql = sql;
    }

    protected void startBenchmark() {
        if (timings != null) {
            timings.start(DebugChannels.SQL + ": " + sql);
        }
    }

    protected void startBatchBenchmark() {
        if (timings != null) {
            timings.start(DebugChannels.SQL + ": " + sql + " (Batch)");
        }
    }

    protected void stopBenchmark() {
        if (timings != null) {
            timings.end(DebugChannels.SQL, DebugChannels.SQL + ": " + sql);
        }
    }

    protected void stopBatchBenchmark() {
        if (timings != null) {
            timings.end(DebugChannels.SQL, DebugChannels.SQL + ": " + sql + " (Batch)");
        }
    }

    public void setTimings(Timings timings) {
        this.timings = timings;
    }
}