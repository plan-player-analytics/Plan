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

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Setting implementation for settings that use {@link java.util.concurrent.TimeUnit} under the value.
 * <p>
 * All values return milliseconds.
 *
 * @author AuroraLS3
 */
public class TimeSetting extends Setting<Long> {

    public TimeSetting(String path) {
        super(path, Long.class, Setting::timeValidator);
    }

    public TimeSetting(String path, Long defaultValue) {
        super(path, Setting::timeValidator, defaultValue);
    }

    public TimeSetting(String path, Predicate<Long> validator) {
        super(path, Long.class, validator.and(Setting::timeValidator));
    }

    public TimeSetting(String path, Predicate<Long> validator, Long defaultValue) {
        super(path, validator.and(Setting::timeValidator), defaultValue);
    }

    @Override
    public Long getValueFrom(ConfigNode node) {
        Long duration = node.getLong(path);
        if (duration == null) {
            return null;
        }
        String unitName = node.getString(path + ".Unit");
        try {
            if (unitName == null) {
                throw new IllegalStateException(
                        "Config value for " + path + ".Unit has a bad value: 'null'"
                );
            }
            TimeUnit unit = TimeUnit.valueOf(unitName.toUpperCase());
            return unit.toMillis(duration);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Config value for " + path + ".Unit has a bad value: " + e.getMessage());
        }
    }
}
