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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.extension.icon.Icon;

import java.util.Optional;

/**
 * Abstract representation of all values a Provider annotation provides.
 *
 * @author Rsl1122
 */
public abstract class DataProvider<T, K> {

    private final String plugin;
    private final String condition; // can be null
    private final String tab; // can be null
    private final int priority;

    private final Icon icon;
    private final String text;
    private final String description;

    private final MethodWrapper<T, K> method;

    public DataProvider(
            String plugin,
            String condition,
            String tab,
            int priority,
            Icon icon,
            String text,
            String description,
            MethodWrapper<T, K> method
    ) {
        this.plugin = plugin;
        this.condition = condition;
        this.tab = tab;
        this.priority = priority;
        this.icon = icon;
        this.text = text;
        this.description = description;
        this.method = method;
    }

    public String getPlugin() {
        return plugin;
    }

    public int getPriority() {
        return priority;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getCondition() {
        return Optional.ofNullable(condition);
    }

    public Optional<String> getTab() {
        return Optional.ofNullable(tab);
    }

    public MethodWrapper<T, K> getMethod() {
        return method;
    }
}