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
package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

import java.text.DecimalFormat;

/**
 * Formatter for decimal points that depends on settings.
 *
 * @author Rsl1122
 */
public class DecimalFormatter implements Formatter<Double> {

    private final PlanConfig config;

    public DecimalFormatter(PlanConfig config) {
        this.config = config;
    }

    @Override
    public String apply(Double value) {
        return new DecimalFormat(config.getString(Settings.FORMAT_DECIMALS)).format(value);
    }
}