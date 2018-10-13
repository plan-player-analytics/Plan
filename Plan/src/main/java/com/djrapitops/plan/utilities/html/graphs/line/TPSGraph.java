package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.store.mutators.TPSMutator;

/**
 * Graph about TPS gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.tasks.TPSCountTimer
 * @since 4.2.0
 */
class TPSGraph extends LineGraph {

    TPSGraph(TPSMutator mutator, boolean displayGaps) {
        super(mutator.tpsPoints(), displayGaps);
    }
}
