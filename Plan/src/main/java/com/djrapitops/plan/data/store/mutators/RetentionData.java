/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plugin.api.TimeAmount;
import com.google.common.base.Objects;

import java.util.Collection;
import java.util.Optional;

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

    public RetentionData(PlayerContainer player, PlayersOnlineResolver onlineOnJoin) {
        Optional<Long> registeredValue = player.getValue(PlayerKeys.REGISTERED);
        activityIndex = registeredValue
                // TODO Thresholds from settings
                .map(registered -> new ActivityIndex(player, registered + TimeAmount.DAY.ms(), 1, 1).getValue())
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