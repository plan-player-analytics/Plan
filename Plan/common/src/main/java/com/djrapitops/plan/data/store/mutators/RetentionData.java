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
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.google.common.base.Objects;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for player retention calculations.
 * <p>
 * Previously known as StickyData.
 *
 * @author Rsl1122
 */
public class RetentionData {
    private final double activityIndex;
    private double onlineOnJoin;

    public static RetentionData average(Collection<RetentionData> stuck) {
        int size = stuck.size();

        double totalIndex = 0.0;
        double totalPlayersOnline = 0.0;

        for (RetentionData retentionData : stuck) {
            totalIndex += retentionData.getActivityIndex();
            totalPlayersOnline += retentionData.getOnlineOnJoin();
        }

        double averageIndex = totalIndex / (double) size;
        double averagePlayersOnline = totalPlayersOnline / (double) size;

        return new RetentionData(averageIndex, averagePlayersOnline);
    }

    public RetentionData(double activityIndex, double onlineOnJoin) {
        this.activityIndex = activityIndex;
        this.onlineOnJoin = onlineOnJoin;
    }

    public RetentionData(
            PlayerContainer player,
            PlayersOnlineResolver onlineOnJoin,
            long activityMsThreshold,
            int activityLoginThreshold
    ) {
        Optional<Long> registeredValue = player.getValue(PlayerKeys.REGISTERED);
        activityIndex = registeredValue
                .map(registered -> new ActivityIndex(
                        player,
                        registered + TimeUnit.DAYS.toMillis(1L),
                        activityMsThreshold,
                        activityLoginThreshold
                ).getValue())
                .orElse(0.0);
        this.onlineOnJoin = registeredValue
                .map(registered -> onlineOnJoin.getOnlineOn(registered).orElse(-1))
                .orElse(0);
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
                Objects.equal(onlineOnJoin, that.onlineOnJoin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityIndex, onlineOnJoin);
    }

    public double getOnlineOnJoin() {
        return onlineOnJoin;
    }

    public double getActivityIndex() {
        return activityIndex;
    }
}