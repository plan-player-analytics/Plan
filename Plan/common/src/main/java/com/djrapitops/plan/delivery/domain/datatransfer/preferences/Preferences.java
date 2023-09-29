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
package com.djrapitops.plan.delivery.domain.datatransfer.preferences;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class Preferences {

    private boolean recentDaysInDateFormat;
    private String dateFormatFull;
    private String dateFormatNoSeconds;
    private String dateFormatClock;
    private TimeFormat timeFormat;
    private String decimalFormat;
    private int firstDay;

    private String playerHeadImageUrl;

    private GraphThresholds tpsThresholds;
    private GraphThresholds diskThresholds;

    private Preferences() {
    }

    public static Builder builder() {
        return new Preferences.Builder();
    }

    public boolean isRecentDaysInDateFormat() {
        return recentDaysInDateFormat;
    }

    private void setRecentDaysInDateFormat(boolean recentDaysInDateFormat) {
        this.recentDaysInDateFormat = recentDaysInDateFormat;
    }

    public String getDateFormatFull() {
        return dateFormatFull;
    }

    private void setDateFormatFull(String dateFormatFull) {
        this.dateFormatFull = dateFormatFull;
    }

    public String getDateFormatNoSeconds() {
        return dateFormatNoSeconds;
    }

    private void setDateFormatNoSeconds(String dateFormatNoSeconds) {
        this.dateFormatNoSeconds = dateFormatNoSeconds;
    }

    public String getDateFormatClock() {
        return dateFormatClock;
    }

    private void setDateFormatClock(String dateFormatClock) {
        this.dateFormatClock = dateFormatClock;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    private void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getDecimalFormat() {
        return decimalFormat;
    }

    private void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public int getFirstDay() {
        return firstDay;
    }

    private void setFirstDay(int firstDay) {
        this.firstDay = firstDay;
    }

    public String getPlayerHeadImageUrl() {
        return playerHeadImageUrl;
    }

    private void setPlayerHeadImageUrl(String playerHeadImageUrl) {
        this.playerHeadImageUrl = playerHeadImageUrl;
    }

    public GraphThresholds getTpsThresholds() {
        return tpsThresholds;
    }

    private void setTpsThresholds(GraphThresholds tpsThresholds) {
        this.tpsThresholds = tpsThresholds;
    }

    public GraphThresholds getDiskThresholds() {
        return diskThresholds;
    }

    private void setDiskThresholds(GraphThresholds diskThresholds) {
        this.diskThresholds = diskThresholds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Preferences that = (Preferences) o;
        return isRecentDaysInDateFormat() == that.isRecentDaysInDateFormat() && getFirstDay() == that.getFirstDay() && Objects.equals(getDateFormatFull(), that.getDateFormatFull()) && Objects.equals(getDateFormatNoSeconds(), that.getDateFormatNoSeconds()) && Objects.equals(getDateFormatClock(), that.getDateFormatClock()) && Objects.equals(getTimeFormat(), that.getTimeFormat()) && Objects.equals(getDecimalFormat(), that.getDecimalFormat()) && Objects.equals(getPlayerHeadImageUrl(), that.getPlayerHeadImageUrl()) && Objects.equals(getTpsThresholds(), that.getTpsThresholds()) && Objects.equals(getDiskThresholds(), that.getDiskThresholds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRecentDaysInDateFormat(), getDateFormatFull(), getDateFormatNoSeconds(), getDateFormatClock(), getTimeFormat(), getDecimalFormat(), getFirstDay(), getPlayerHeadImageUrl(), getTpsThresholds(), getDiskThresholds());
    }

    @Override
    public String toString() {
        return "Preferences{" +
                "recentDaysInDateFormat=" + recentDaysInDateFormat +
                ", dateFormatFull='" + dateFormatFull + '\'' +
                ", dateFormatNoSeconds='" + dateFormatNoSeconds + '\'' +
                ", dateFormatClock='" + dateFormatClock + '\'' +
                ", timeFormat=" + timeFormat +
                ", decimalFormat='" + decimalFormat + '\'' +
                ", firstDay=" + firstDay +
                ", playerHeadImageUrl='" + playerHeadImageUrl + '\'' +
                ", tpsThresholds=" + tpsThresholds +
                ", diskThresholds=" + diskThresholds +
                '}';
    }

    public static final class Builder {
        private final Preferences preferences;

        private Builder() {preferences = new Preferences();}

        public Builder withRecentDaysInDateFormat(boolean recentDaysInDateFormat) {
            preferences.setRecentDaysInDateFormat(recentDaysInDateFormat);
            return this;
        }

        public Builder withDateFormatFull(String dateFormatFull) {
            preferences.setDateFormatFull(dateFormatFull);
            return this;
        }

        public Builder withDateFormatNoSeconds(String dateFormatNoSeconds) {
            preferences.setDateFormatNoSeconds(dateFormatNoSeconds);
            return this;
        }

        public Builder withDateFormatClock(String dateFormatClock) {
            preferences.setDateFormatClock(dateFormatClock);
            return this;
        }

        public Builder withTimeFormat(TimeFormat timeFormat) {
            preferences.setTimeFormat(timeFormat);
            return this;
        }

        public Builder withDecimalFormat(String decimalFormat) {
            preferences.setDecimalFormat(decimalFormat);
            return this;
        }

        public Builder withFirstDay(int firstDay) {
            preferences.setFirstDay(firstDay);
            return this;
        }

        public Builder withPlayerHeadImageUrl(String playerHeadImageUrl) {
            preferences.setPlayerHeadImageUrl(playerHeadImageUrl);
            return this;
        }

        public Builder withTpsThresholds(GraphThresholds tpsThresholds) {
            preferences.setTpsThresholds(tpsThresholds);
            return this;
        }

        public Builder withDiskThresholds(GraphThresholds diskThresholds) {
            preferences.setDiskThresholds(diskThresholds);
            return this;
        }

        public Preferences build() {return preferences;}
    }
}
