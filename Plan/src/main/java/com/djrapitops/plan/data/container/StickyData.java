/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plugin.api.TimeAmount;
import com.google.common.base.Objects;

public class StickyData {
    private final double activityIndex;
    private Integer messagesSent;
    private Integer onlineOnJoin;

    public StickyData(PlayerProfile player) {
        activityIndex = player.getActivityIndex(player.getRegistered() + TimeAmount.DAY.ms()).getValue();
        for (Action action : player.getActions()) {
            if (messagesSent == null && action.getDoneAction() == Actions.FIRST_LOGOUT) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(": ");
                if (split.length == 2) {
                    try {
                        messagesSent = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (onlineOnJoin == null && action.getDoneAction() == Actions.FIRST_SESSION) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(" ");
                if (split.length == 3) {
                    try {
                        onlineOnJoin = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (messagesSent == null) {
            messagesSent = 0;
        }
        if (onlineOnJoin == null) {
            onlineOnJoin = 0;
        }
    }

    public double distance(StickyData data) {
        double num = 0;
        num += Math.abs(data.activityIndex - activityIndex) * 2.0;
        num += Math.abs(data.onlineOnJoin - onlineOnJoin) / 10.0;
        num += Math.abs(data.messagesSent - messagesSent) / 10.0;

        return num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StickyData that = (StickyData) o;
        return Double.compare(that.activityIndex, activityIndex) == 0 &&
                Objects.equal(messagesSent, that.messagesSent) &&
                Objects.equal(onlineOnJoin, that.onlineOnJoin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityIndex, messagesSent, onlineOnJoin);
    }

    public int getOnlineOnJoin() {
        return onlineOnJoin;
    }
}