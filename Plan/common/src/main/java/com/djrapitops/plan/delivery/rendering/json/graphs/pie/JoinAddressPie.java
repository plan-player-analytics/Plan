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
package com.djrapitops.plan.delivery.rendering.json.graphs.pie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinAddressPie extends Pie {

    JoinAddressPie(Map<String, Integer> joinAddresses) {
        super(turnToSlices(joinAddresses));
    }

    private static List<PieSlice> turnToSlices(Map<String, Integer> joinAddresses) {
        List<PieSlice> slices = new ArrayList<>();
        for (Map.Entry<String, Integer> address : joinAddresses.entrySet()) {
            String joinAddress = address.getKey();
            Integer total = address.getValue();
            slices.add(new PieSlice(joinAddress, total));
        }
        return slices;
    }
}
