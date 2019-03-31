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
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.extension.implementation.providers.gathering.ProviderValueGatherer;
import com.djrapitops.plugin.utilities.Verify;

import java.util.UUID;

/**
 * Implementation for {@link Caller} interface.
 *
 * @author Rsl1122
 */
public class CallerImplementation implements Caller {

    private final ProviderValueGatherer gatherer;
    private final ExtensionServiceImplementation extensionServiceImplementation;

    public CallerImplementation(ProviderValueGatherer gatherer, ExtensionServiceImplementation extensionServiceImplementation) {
        this.gatherer = gatherer;
        this.extensionServiceImplementation = extensionServiceImplementation;
    }

    @Override
    public void updatePlayerData(UUID playerUUID, String playerName) throws IllegalArgumentException {
        Verify.nullCheck(playerUUID, () -> new IllegalArgumentException("'playerUUID' can not be null!"));
        Verify.nullCheck(playerName, () -> new IllegalArgumentException("'playerName' can not be null!"));
        extensionServiceImplementation.updatePlayerValues(gatherer, playerUUID, playerName, CallEvents.MANUAL);
    }

    @Override
    public void updateServerData() {
        extensionServiceImplementation.updateServerValues(gatherer, CallEvents.MANUAL);
    }
}