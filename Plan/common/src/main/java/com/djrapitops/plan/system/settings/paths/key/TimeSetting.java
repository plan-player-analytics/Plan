package com.djrapitops.plan.system.settings.paths.key;

import com.djrapitops.plugin.config.ConfigNode;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Setting implementation for settings that use {@link java.util.concurrent.TimeUnit} under the value.
 * <p>
 * All values return milliseconds.
 *
 * @author Rsl1122
 */
public class TimeSetting extends Setting<Long> {

    public TimeSetting(String path) {
        super(path, Long.class, Setting::timeValidator);
    }

    public TimeSetting(String path, Predicate<Long> validator) {
        super(path, Long.class, validator.and(Setting::timeValidator));
    }

    @Override
    public Long getValueFrom(ConfigNode node) {
        long duration = node.getLong(path);
        String unitName = node.getString(path + ".Unit");
        try {
            TimeUnit unit = TimeUnit.valueOf(unitName.toUpperCase());
            return unit.toMillis(duration);
        } catch (IllegalArgumentException e) {
            return -1L;
        }
    }
}