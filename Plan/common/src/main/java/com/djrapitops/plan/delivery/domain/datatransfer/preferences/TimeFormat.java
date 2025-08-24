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
public class TimeFormat {
    private String year;
    private String years;
    private String month;
    private String months;
    private String day;
    private String days;
    private String hours;
    private String minutes;
    private String seconds;
    private String zero;

    private TimeFormat() {
        // Constructor is private for builder
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getYear() {
        return year;
    }

    private void setYear(String year) {
        this.year = year;
    }

    public String getYears() {
        return years;
    }

    private void setYears(String years) {
        this.years = years;
    }

    public String getMonth() {
        return month;
    }

    private void setMonth(String month) {
        this.month = month;
    }

    public String getMonths() {
        return months;
    }

    private void setMonths(String months) {
        this.months = months;
    }

    public String getDay() {
        return day;
    }

    private void setDay(String day) {
        this.day = day;
    }

    public String getDays() {
        return days;
    }

    private void setDays(String days) {
        this.days = days;
    }

    public String getHours() {
        return hours;
    }

    private void setHours(String hours) {
        this.hours = hours;
    }

    public String getMinutes() {
        return minutes;
    }

    private void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public String getSeconds() {
        return seconds;
    }

    private void setSeconds(String seconds) {
        this.seconds = seconds;
    }

    public String getZero() {
        return zero;
    }

    private void setZero(String zero) {
        this.zero = zero;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFormat that = (TimeFormat) o;
        return Objects.equals(getYear(), that.getYear()) && Objects.equals(getYears(), that.getYears()) && Objects.equals(getMonth(), that.getMonth()) && Objects.equals(getMonths(), that.getMonths()) && Objects.equals(getDay(), that.getDay()) && Objects.equals(getDays(), that.getDays()) && Objects.equals(getHours(), that.getHours()) && Objects.equals(getMinutes(), that.getMinutes()) && Objects.equals(getSeconds(), that.getSeconds()) && Objects.equals(getZero(), that.getZero());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getYear(), getYears(), getMonth(), getMonths(), getDay(), getDays(), getHours(), getMinutes(), getSeconds(), getZero());
    }

    @Override
    public String toString() {
        return "TimeFormat{" +
                "year='" + year + '\'' +
                ", years='" + years + '\'' +
                ", month='" + month + '\'' +
                ", months='" + months + '\'' +
                ", day='" + day + '\'' +
                ", days='" + days + '\'' +
                ", hours='" + hours + '\'' +
                ", minutes='" + minutes + '\'' +
                ", seconds='" + seconds + '\'' +
                ", zero='" + zero + '\'' +
                '}';
    }

    public static final class Builder {
        private final TimeFormat timeFormat;

        private Builder() {timeFormat = new TimeFormat();}

        public Builder withYear(String year) {
            timeFormat.setYear(year);
            return this;
        }

        public Builder withYears(String years) {
            timeFormat.setYears(years);
            return this;
        }

        public Builder withMonth(String month) {
            timeFormat.setMonth(month);
            return this;
        }

        public Builder withMonths(String months) {
            timeFormat.setMonths(months);
            return this;
        }

        public Builder withDay(String day) {
            timeFormat.setDay(day);
            return this;
        }

        public Builder withDays(String days) {
            timeFormat.setDays(days);
            return this;
        }

        public Builder withHours(String hours) {
            timeFormat.setHours(hours);
            return this;
        }

        public Builder withMinutes(String minutes) {
            timeFormat.setMinutes(minutes);
            return this;
        }

        public Builder withSeconds(String seconds) {
            timeFormat.setSeconds(seconds);
            return this;
        }

        public Builder withZero(String zero) {
            timeFormat.setZero(zero);
            return this;
        }

        public TimeFormat build() {return timeFormat;}
    }
}
