/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.aac;

import me.konsolas.aac.api.HackType;

import java.util.UUID;

/**
 * Data object for AAC data.
 *
 * @author Rsl1122
 */
public class HackObject {

    private final UUID uuid;
    private final long date;
    private final HackType hackType;
    private final int violationLevel;

    public HackObject(UUID uuid, long date, HackType hackType, int violationLevel) {
        this.uuid = uuid;
        this.date = date;
        this.hackType = hackType;
        this.violationLevel = violationLevel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getDate() {
        return date;
    }

    public HackType getHackType() {
        return hackType;
    }

    public int getViolationLevel() {
        return violationLevel;
    }
}