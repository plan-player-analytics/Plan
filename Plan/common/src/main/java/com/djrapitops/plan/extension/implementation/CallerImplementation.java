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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.implementation.providers.gathering.ProviderValueGatherer;
import com.djrapitops.plan.processing.Processing;

import java.util.UUID;

/**
 * Implementation for {@link Caller} interface.
 *
 * @author AuroraLS3
 */
public class CallerImplementation implements Caller {

    private final ProviderValueGatherer gatherer;
    private final ExtensionSvc extensionService;
    private final Processing processing;

    public CallerImplementation(
            ProviderValueGatherer gatherer,
            ExtensionSvc extensionService,
            Processing processing
    ) {
        this.gatherer = gatherer;
        this.extensionService = extensionService;
        this.processing = processing;
    }

    @Override
    public void updatePlayerData(UUID playerUUID, String playerName) {
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(gatherer, playerUUID, playerName, CallEvents.MANUAL));
    }

    @Override
    public void updateServerData() {
        processing.submitNonCritical(() -> extensionService.updateServerValues(gatherer, CallEvents.MANUAL));
    }
}