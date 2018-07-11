package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;

public class NetworkHealthInformation extends AbstractHealthInfo {

    private final NetworkContainer container;

    public NetworkHealthInformation(NetworkContainer container) {
        super(
                container.getUnsafe(NetworkKeys.REFRESH_TIME),
                container.getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO)
        );
        this.container = container;
        calculate();
    }

    @Override
    protected void calculate() {
        activityChangeNote(container.getUnsafe(NetworkKeys.ACTIVITY_DATA));
        activePlayerPlaytimeChange(container.getUnsafe(NetworkKeys.PLAYERS_MUTATOR));

        perServerComparisonNote(container.getUnsafe(NetworkKeys.PLAYERS_MUTATOR));
    }

    private void perServerComparisonNote(PlayersMutator playersMutator) {

    }
}
