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

import java.util.List;

/**
 * @author AuroraLS3
 */
public class JoinAddressCounts implements DateHolder {

    private final long date;
    private final List<JoinAddressCount> joinAddresses;

    public JoinAddressCounts(long date, List<JoinAddressCount> joinAddresses) {
        this.date = date;
        this.joinAddresses = joinAddresses;
    }

    @Override
    public long getDate() {
        return date;
    }

    public List<JoinAddressCount> getJoinAddresses() {
        return joinAddresses;
    }
}
