package com.djrapitops.plan.system.settings.paths.key;

import com.djrapitops.plugin.config.ConfigNode;

import java.util.function.Predicate;

/**
 * Setting implementation for String value settings.
 *
 * @author Rsl1122
 */
public class StringSetting extends Setting<String> {

    public StringSetting(String path) {
        super(path, String.class);
    }

    public StringSetting(String path, Predicate<String> validator) {
        super(path, String.class, validator);
    }

    @Override
    public String getValueFrom(ConfigNode node) {
        return node.contains(path) ? node.getString(path) : null;
    }
}