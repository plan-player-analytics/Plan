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
package com.djrapitops.plan.settings.config.paths.key;

import com.djrapitops.plan.settings.config.ConfigNode;

import java.util.function.Predicate;

/**
 * Setting implementation for Double (floating point) value settings.
 */
public class DoubleSetting extends Setting<Double> {

    public DoubleSetting(String path) {
        super(path, Double.class);
    }

    public DoubleSetting(String path, Predicate<Double> validator) {
        super(path, Double.class, validator);
    }

    @Override
    public Double getValueFrom(ConfigNode node) {
        return node.getDouble(path);
    }
}