package com.djrapitops.plan.common.utilities.html.graphs.line;

import com.djrapitops.plan.common.data.container.TPS;
import com.djrapitops.plan.common.data.store.mutators.TPSMutator;

import java.util.List;

/**
 * Graph about Player Counts gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.common.system.tasks.TPSCountTimer
 * @since 4.2.0
 */
public class OnlineActivityGraph extends AbstractLineGraph {

    public OnlineActivityGraph(List<TPS> tpsData) {
        this(new TPSMutator(tpsData));
    }

    public OnlineActivityGraph(TPSMutator mutator) {
        super(mutator.playersOnlinePoints());
    }
}
