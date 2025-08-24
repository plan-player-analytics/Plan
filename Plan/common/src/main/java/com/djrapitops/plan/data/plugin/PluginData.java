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
package com.djrapitops.plan.data.plugin;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * This is an abstract class that can be used to add data from a plugin to the
 * "Plugins"-sections of Analysis and Inspect pages.
 * <p>
 * API-section of documentation has examples on the usage of this class and how
 * to register objects extending this class.
 *
 * @author AuroraLS3
 * @deprecated PluginData API has been deprecated - see <a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5---DataExtension-API">wiki</a> for new API.
 */
@Deprecated(since = "5.0")
public abstract class PluginData {

    private final ContainerSize size;
    private final String sourcePlugin;

    private Icon pluginIcon;

    private String helpText;

    protected com.djrapitops.plan.data.store.containers.AnalysisContainer analysisData = new com.djrapitops.plan.data.store.containers.AnalysisContainer();

    public PluginData(ContainerSize size, String sourcePlugin) {
        this.size = size;
        this.sourcePlugin = sourcePlugin;
    }

    public abstract InspectContainer getPlayerData(UUID uuid, InspectContainer fillThis) throws Exception;

    public abstract AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer fillThis) throws Exception;

    protected final void setPluginIcon(Icon pluginIcon) {
        this.pluginIcon = pluginIcon;
    }

    /**
     * @deprecated Use {@code setPluginIcon(Icon)} instead
     */
    @Deprecated
    protected final void setPluginIcon(String pluginIcon) {
        this.pluginIcon = Icon.called(pluginIcon != null ? pluginIcon : "cube").build();
    }

    /**
     * @deprecated Use {@code setPluginIcon(Icon)} instead
     */
    @Deprecated
    protected final void setIconColor(String iconColor) {
        pluginIcon.setColor(Color.matchString(iconColor));
    }

    public final String getHelpText() {
        return helpText;
    }

    public final String parsePluginIcon() {
        return (pluginIcon != null ? pluginIcon : Icon.called("cube").build()).toHtml();
    }

    public final ContainerSize getSize() {
        return size;
    }

    public final String getSourcePlugin() {
        return sourcePlugin;
    }

    protected final void setHelpText(String html) {
        // no-op
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginData that = (PluginData) o;
        return size == that.size &&
                Objects.equals(sourcePlugin, that.sourcePlugin) &&
                Objects.equals(pluginIcon, that.pluginIcon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, sourcePlugin, pluginIcon);
    }

    /**
     * @deprecated Use {@code getWithIcon(String, Icon)} instead
     */
    @Deprecated
    public final String getWithIcon(String text, String icon) {
        return getWithIcon(text, Icon.called(icon).build());
    }

    /**
     * @deprecated Use {@code getWithIcon(String, Icon)} instead
     */
    @Deprecated
    public final String getWithIcon(String text, String icon, String color) {
        return getWithIcon(text, Icon.called(icon).of(Color.matchString(color)).build());
    }

    public final String getWithIcon(String text, Icon.Builder builder) {
        return getWithIcon(text, builder.build());
    }

    public final String getWithIcon(String text, Icon icon) {
        return icon.toHtml() + " " + text;
    }

    public final void setAnalysisData(com.djrapitops.plan.data.store.containers.AnalysisContainer analysisData) {
        this.analysisData = analysisData;
    }
}
