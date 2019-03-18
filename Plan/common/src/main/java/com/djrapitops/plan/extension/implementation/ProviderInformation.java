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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.icon.Icon;

import java.util.Optional;

/**
 * Represents the annotation information provided on a method.
 *
 * @author Rsl1122
 */
public class ProviderInformation {

    private final String pluginName;
    private final String name;
    private final String text;
    private final String description;
    private final Icon icon;
    private final int priority;
    private final String tab; // can be null
    private final String condition; // can be null

    public ProviderInformation(
            String pluginName, String name, String text, String description, Icon icon, int priority, String tab, String condition
    ) {
        this.pluginName = pluginName;
        this.name = name;
        this.text = text;
        this.description = description;
        this.icon = icon;
        this.priority = priority;
        this.tab = tab;
        this.condition = condition;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public Icon getIcon() {
        return icon;
    }

    public int getPriority() {
        return priority;
    }

    public Optional<String> getTab() {
        return Optional.ofNullable(tab);
    }

    public Optional<String> getCondition() {
        return Optional.ofNullable(condition);
    }
}