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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.graph.HistoryStrategy;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.ExtensionDescription;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the annotation information provided on a method.
 *
 * @author AuroraLS3
 */
public class ProviderInformation extends ExtensionDescription {

    private final String pluginName;
    private final boolean showInPlayersTable;
    private final String tab;               // can be null
    private final Conditional condition;    // can be null
    private final boolean hidden;           // default false, BooleanProvider
    private final String providedCondition; // can be null, BooleanProvider
    private final FormatType formatType;    // can be null, NumberProvider
    private final boolean isPlayerName;     // default false, StringProvider
    private final Color tableColor;         // can be null, TableProvider
    private final boolean percentage;       // affects where doubles are stored
    private final boolean component;        // affects where strings are stored
    private final HistoryStrategy appendStrategy;

    private ProviderInformation(ProviderInformation.Builder builder) {
        super(
                builder.name,
                builder.text,
                builder.description,
                builder.icon != null ? builder.icon : Icon.called("cube").build(),
                builder.priority
        );
        pluginName = builder.pluginName;
        showInPlayersTable = builder.showInPlayersTable;
        tab = builder.tab;
        condition = builder.condition;
        hidden = builder.hidden;
        providedCondition = builder.providedCondition;
        formatType = builder.formatType;
        isPlayerName = builder.isPlayerName;
        tableColor = builder.tableColor;
        percentage = builder.percentage;
        component = builder.component;
        appendStrategy = builder.appendStrategy;
    }

    public static ProviderInformation.Builder builder(String pluginName) {
        return new ProviderInformation.Builder(pluginName);
    }

    public String getPluginName() {
        return StringUtils.truncate(pluginName, 50);
    }

    public boolean isShownInPlayersTable() {
        return showInPlayersTable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getProvidedCondition() {
        return providedCondition;
    }

    public Optional<FormatType> getFormatType() {
        return Optional.ofNullable(formatType);
    }

    public boolean isPlayerName() {
        return isPlayerName;
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

    public Optional<HistoryStrategy> getAppendStrategy() {
        return Optional.ofNullable(appendStrategy);
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

    public Color getTableColor() {
        return tableColor;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public boolean isComponent() {
        return component;
    }

    public static class Builder {
        private final String pluginName;
        private String name;
        private String text;
        private String description;
        private Icon icon;
        private int priority = 0;
        private boolean showInPlayersTable = false;
        private String tab;                   // can be null
        private Conditional condition;        // can be null
        private boolean hidden = false;       // default false, BooleanProvider
        private String providedCondition;     // can be null, BooleanProvider
        private FormatType formatType;        // can be null, NumberProvider
        private boolean isPlayerName = false; // default false, StringProvider
        private Color tableColor;             // can be null, TableProvider
        private boolean percentage;           // affects where doubles are stored
        private boolean component;            // affects where strings are stored
        private HistoryStrategy appendStrategy;

        public Builder(String pluginName) {
            this.pluginName = pluginName;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setShowInPlayersTable(boolean showInPlayersTable) {
            this.showInPlayersTable = showInPlayersTable;
            return this;
        }

        public Builder setTab(String tab) {
            this.tab = tab;
            return this;
        }

        public Builder setCondition(Conditional condition) {
            this.condition = condition;
            return this;
        }

        public Builder setHidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder setProvidedCondition(String providedCondition) {
            this.providedCondition = providedCondition;
            return this;
        }

        public Builder setFormatType(FormatType formatType) {
            this.formatType = formatType;
            return this;
        }

        public Builder setPlayerName(boolean playerName) {
            isPlayerName = playerName;
            return this;
        }

        public Builder setTableColor(Color tableColor) {
            this.tableColor = tableColor;
            return this;
        }

        public Builder setAsPercentage() {
            percentage = true;
            return this;
        }

        public Builder setAsComponent() {
            component = true;
            return this;
        }

        public ProviderInformation build() {
            return new ProviderInformation(this);
        }

        public Builder setAppendStrategy(HistoryStrategy appendStrategy) {
            this.appendStrategy = appendStrategy;
            return this;
        }
    }
}