package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.store.mutators.TPSMutator;

/**
 * Graph about Disk Usage gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.tasks.TPSCountTimer
 * @since 4.5.0
 */
class DiskGraph extends LineGraph {

    DiskGraph(TPSMutator mutator, boolean displayGaps) {
        super(mutator.freeDiskPoints(), displayGaps);
    }
}
