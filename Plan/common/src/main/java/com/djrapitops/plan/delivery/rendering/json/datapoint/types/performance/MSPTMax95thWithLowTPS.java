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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types.performance;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Datapoint for looking up Average MSPT within the timeframe when TPS is low.
 *
 * @author AuroraLS3
 */
@Singleton
public class MSPTMax95thWithLowTPS implements Datapoint<Double> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    @Inject
    public MSPTMax95thWithLowTPS(PlanConfig config, DBSystem dbSystem) {
        this.config = config;
        this.dbSystem = dbSystem;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.noPlayer();
    }

    @Override
    public Optional<Double> getValue(GenericFilter filter) {
        double average = dbSystem.getDatabase().query(TPSQueries.max95thMSPTWhenLowTps(
                filter.getAfter(), filter.getBefore(), filter.getServerUUIDs(),
                config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
        return average != -1.0 ? Optional.of(average) : Optional.empty();
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_MSPT_MAX_95TH_LOW_TPS;
        } else {
            return WebPermission.DATA_NETWORK_MSPT_MAX_95TH_LOW_TPS;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.MSPT_MAX_95TH_LOW_TPS;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.MILLIS;
    }
}
