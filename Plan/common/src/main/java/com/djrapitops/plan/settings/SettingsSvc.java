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
package com.djrapitops.plan.settings;

import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Implementation for {@link SettingsService}.
 *
 * @author Rsl1122
 */
@Singleton
public class SettingsSvc implements SettingsService {

    private final PlanConfig config;
    private final ErrorLogger errorLogger;

    @Inject
    public SettingsSvc(
            PlanConfig config,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.errorLogger = errorLogger;
    }

    public void register() {
        Holder.set(this);
    }

    @Override
    public String getString(String path, Supplier<String> defaultValue) {
        String pluginPath = getPluginPath(path);
        Optional<ConfigNode> node = config.getNode(pluginPath);
        if (node.isPresent()) {
            return node.get().getString();
        } else {
            set(pluginPath, defaultValue);
            return config.getString(pluginPath);
        }
    }

    public <T> void set(String pluginPath, Supplier<T> defaultValue) {
        config.set(pluginPath, defaultValue.get());

        try {
            config.save();
        } catch (IOException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().whatToDo("Fix write permissions to " + config.getConfigFilePath()).build());
        }
    }

    @Override
    public Integer getInteger(String path, Supplier<Integer> defaultValue) {
        String pluginPath = getPluginPath(path);
        Optional<ConfigNode> node = config.getNode(pluginPath);
        if (node.isPresent()) {
            return node.get().getInteger();
        } else {
            set(pluginPath, defaultValue);
            return config.getInteger(pluginPath);
        }
    }

    @Override
    public List<String> getStringList(String path, Supplier<List<String>> defaultValue) {
        String pluginPath = getPluginPath(path);
        Optional<ConfigNode> node = config.getNode(pluginPath);
        if (node.isPresent()) {
            return node.get().getStringList();
        } else {
            set(pluginPath, defaultValue);
            return config.getStringList(pluginPath);
        }
    }

    private String getPluginPath(String path) {
        return "Plugins." + path;
    }
}