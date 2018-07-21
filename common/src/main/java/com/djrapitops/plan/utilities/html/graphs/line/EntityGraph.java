package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.system.tasks.TPSCountTimer;

import java.util.List;

/**
 * Graph about Entity Counts gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 4.2.0
 */
public class EntityGraph extends AbstractLineGraph {

    public EntityGraph(List<TPS> tpsData) {
        this(new TPSMutator(tpsData));
    }

    public EntityGraph(TPSMutator mutator) {
        super(mutator.entityPoints());
    }
}
