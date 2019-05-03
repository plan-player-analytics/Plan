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

import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.ExtensionDescriptive;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the annotation information provided on a method.
 *
 * @author Rsl1122
 */
public class ProviderInformation extends ExtensionDescriptive {

    private final String pluginName;
    private final String tab; // can be null
    private final Conditional condition; // can be null

    public ProviderInformation(
            String pluginName, String name, String text, String description, Icon icon, int priority, String tab, Conditional condition
    ) {
        super(name, text, description, icon, priority);
        this.pluginName = pluginName;
        this.tab = tab;
        this.condition = condition;
    }

    public String getPluginName() {
        return StringUtils.truncate(pluginName, 50);
    }

    public Optional<String> getTab() {
        return tab == null || tab.isEmpty()
                ? Optional.empty()
                : Optional.of(StringUtils.truncate(tab, 50));
    }

    public Optional<String> getCondition() {
        if (condition == null || condition.value().isEmpty()) {
            return Optional.empty();
        } else if (condition.negated()) {
            return Optional.of("not_" + getTruncatedConditionName());
        } else {
            return Optional.of(getTruncatedConditionName());
        }
    }

    private String getTruncatedConditionName() {
        return StringUtils.truncate(condition.value(), 50);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderInformation)) return false;
        if (!super.equals(o)) return false;
        ProviderInformation that = (ProviderInformation) o;
        return pluginName.equals(that.pluginName) &&
                Objects.equals(tab, that.tab) &&
                Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pluginName, tab, condition);
    }
}