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
package com.djrapitops.plan.delivery.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a single join address - number pair.
 *
 * @author AuroraLS3
 */
public class JoinAddressCount implements Comparable<JoinAddressCount> {

    private int count;
    private String joinAddress;

    public JoinAddressCount(Map.Entry<String, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public JoinAddressCount(String joinAddress, int count) {
        this.joinAddress = joinAddress;
        this.count = count;
    }

    public String getJoinAddress() {
        return joinAddress;
    }

    public void setJoinAddress(String joinAddress) {
        this.joinAddress = joinAddress;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int compareTo(@NotNull JoinAddressCount other) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.joinAddress, other.joinAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinAddressCount that = (JoinAddressCount) o;
        return getCount() == that.getCount() && Objects.equals(getJoinAddress(), that.getJoinAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJoinAddress(), getCount());
    }
}
