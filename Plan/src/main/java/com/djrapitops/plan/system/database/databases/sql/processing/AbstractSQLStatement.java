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