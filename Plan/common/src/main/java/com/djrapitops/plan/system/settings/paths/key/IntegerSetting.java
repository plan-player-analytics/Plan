package com.djrapitops.plan.system.settings.paths.key;

import com.djrapitops.plan.system.settings.config.ConfigNode;

import java.util.function.Predicate;

/**
 * Setting implementation for String value settings.
 *
 * @author Rsl1122
 */
public class IntegerSetting extends Setting<Integer> {

    public IntegerSetting(String path) {
        super(path, Integer.class);
    }

    public IntegerSetting(String path, Predicate<Integer> validator) {
        super(path, Integer.class, validator);
    }

    @Override
    public Integer getValueFrom(ConfigNode node) {
        return node.getInteger(path);
    }
}