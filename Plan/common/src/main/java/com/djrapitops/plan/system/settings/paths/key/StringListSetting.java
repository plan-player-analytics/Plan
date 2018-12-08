package com.djrapitops.plan.system.settings.paths.key;

import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plugin.config.ConfigNode;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Setting implementation for String value settings.
 *
 * @author Rsl1122
 */
public class StringListSetting extends Setting<List<String>> {

    public StringListSetting(String path) {
        super(path, new Type<List<String>>() {});
    }

    public StringListSetting(String path, Predicate<List<String>> validator) {
        super(path, new Type<List<String>>() {}, validator);
    }

    @Override
    public List<String> getValueFrom(ConfigNode node) {
        return node.contains(path) ? node.getStringList(path) : Collections.emptyList();
    }
}