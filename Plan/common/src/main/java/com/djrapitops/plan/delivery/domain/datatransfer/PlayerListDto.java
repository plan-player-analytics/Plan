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
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionDescriptionDto;

import java.util.List;

/**
 * @author AuroraLS3
 */
public class PlayerListDto {

    private final List<TablePlayerDto> players;
    private final List<ExtensionDescriptionDto> extensionDescriptors;

    public PlayerListDto(List<TablePlayerDto> players, List<ExtensionDescriptionDto> extensionDescriptors) {
        this.players = players;
        this.extensionDescriptors = extensionDescriptors;
    }

    public List<TablePlayerDto> getPlayers() {
        return players;
    }

    public List<ExtensionDescriptionDto> getExtensionDescriptors() {
        return extensionDescriptors;
    }

    @Override
    public String toString() {
        return "PlayerListDto{" +
                "players=" + players +
                ", extensionDescriptors=" + extensionDescriptors +
                '}';
    }
}
