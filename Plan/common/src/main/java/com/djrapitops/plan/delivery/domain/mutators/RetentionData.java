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
package com.djrapitops.plan.delivery.domain.mutators;

import java.util.Collection;

/**
 * Utility class for player retention calculations.
 * <p>
 * Previously known as StickyData.
 *
 * @author AuroraLS3
 */
public record RetentionData(double activityIndex, double onlineOnJoin) {

    public static RetentionData average(Collection<RetentionData> stuck) {
        int size = stuck.size();

        double totalIndex = 0.0;
        double totalPlayersOnline = 0.0;

        for (RetentionData retentionData : stuck) {
            totalIndex += retentionData.activityIndex();
            totalPlayersOnline += retentionData.onlineOnJoin();
        }

        double averageIndex = totalIndex / size;
        double averagePlayersOnline = totalPlayersOnline / size;

        return new RetentionData(averageIndex, averagePlayersOnline);
    }

    public static int countRetentionPrediction(
            Collection<ActivityIndex> newPlayers,
            ActivityIndex retained,
            ActivityIndex nonRetained
    ) {
        int count = 0;
        for (ActivityIndex player : newPlayers) {
            if (player.distance(retained) < player.distance(nonRetained)) count++;
        }
        return count;
    }

    public double distance(RetentionData data) {
        double num = 0;
        num += Math.abs(data.activityIndex - activityIndex) * 2.0;
        num += data.onlineOnJoin != -1 && onlineOnJoin != -1
                ? Math.abs(data.onlineOnJoin - onlineOnJoin) / 10.0
                : 0;

        return num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetentionData that = (RetentionData) o;
        return Double.compare(that.activityIndex, activityIndex) == 0 &&
                Double.compare(that.onlineOnJoin, onlineOnJoin) == 0;
    }

}