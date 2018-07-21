package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

import java.util.List;

/**
 * Graph about RAM Usage gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 4.2.0
 */
public class RamGraph extends AbstractLineGraph {

    public RamGraph(List<TPS> tpsData) {
        this(new TPSMutator(tpsData));
    }

    public RamGraph(TPSMutator mutator) {
        super(mutator.ramUsagePoints());
    }
}
