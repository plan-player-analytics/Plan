package com.djrapitops.plan.data.store.containers;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.AnalysisKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class AnalysisContainer extends DataContainer {

    private final ServerContainer serverContainer;

    public AnalysisContainer(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
        addAnalysisSuppliers();
    }

    private void addAnalysisSuppliers() {

    }
}