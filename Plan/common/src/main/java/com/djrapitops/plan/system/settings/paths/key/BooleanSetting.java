package com.djrapitops.plan.system.settings.paths.key;

import com.djrapitops.plan.system.settings.config.ConfigNode;

import java.util.function.Predicate;

/**
 * Setting implementation for String value settings.
 *
 * @author Rsl1122
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String path) {
        super(path, Boolean.class);
    }

    public BooleanSetting(String path, Predicate<Boolean> validator) {
        super(path, Boolean.class, validator);
    }

    @Override
    public Boolean getValueFrom(ConfigNode node) {
        return node.getNode(path).map(ConfigNode::getBoolean).orElse(false);
    }
}