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
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.system.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.system.delivery.domain.keys.PlayerKeys;

import java.util.Comparator;

/**
 * Comparator for PlayerContainer so that most recently seen is first.
 *
 * @author Rsl1122
 */
public class PlayerContainerLastPlayedComparator implements Comparator<PlayerContainer> {

    @Override
    public int compare(PlayerContainer playerOne, PlayerContainer playerTwo) {
        return Long.compare(
                playerTwo.getValue(PlayerKeys.LAST_SEEN).orElse(0L),
                playerOne.getValue(PlayerKeys.LAST_SEEN).orElse(0L)
        );
    }
}
