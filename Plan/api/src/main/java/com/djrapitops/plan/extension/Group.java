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
package com.djrapitops.plan.extension;

/**
 * Method parameter for providing values about a group with provider annotations.
 * <p>
 * Usage Example: {@code @StringProvider String provideStringAboutGroup(Group group)}
 * <p>
 * Group names of users are provided with {@code @GroupProvider String[] provideGroups(UUID playerUUID)}
 * {@code Group} parameter methods are not called without knowledge of a group name.
 * <p>
 * This method parameter is used since it is not possible to differentiate String playerName and String groupName.
 *
 * @author AuroraLS3
 */
public interface Group {

    /**
     * Get the name of the group.
     *
     * @return Name of the group given by a {@link com.djrapitops.plan.extension.annotation.GroupProvider}, e.g. "Miner"
     */
    String getGroupName();

}