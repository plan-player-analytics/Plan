package main.java.com.djrapitops.plan.data.additional;

import com.google.common.base.Objects;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.Collection;
import java.util.UUID;

/**
 * This is an abstract class that can be used to add data from a plugin to the
 * "Plugins"-sections of Analysis and Inspect pages.
 * <p>
 * API-section of documentation has examples on the usage of this class and how
 * to register objects extending this class.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
public abstract class PluginData {

    private final ContainerSize size;
    private final String sourcePlugin;

    private String pluginIcon;

    public PluginData(ContainerSize size, String sourcePlugin) {
        this.size = size;
        this.sourcePlugin = sourcePlugin;
    }

    public abstract InspectContainer getPlayerData(UUID uuid, InspectContainer fillThis) throws Exception;

    public abstract AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer fillThis) throws Exception;

    protected void setPluginIcon(String pluginIcon) {
        this.pluginIcon = pluginIcon;
    }

    public String parsePluginIcon() {
        return pluginIcon != null ? Html.FONT_AWESOME_ICON.parse(pluginIcon) : Html.FONT_AWESOME_ICON.parse("cube");
    }

    public ContainerSize getSize() {
        return size;
    }

    public String getSourcePlugin() {
        return sourcePlugin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginData that = (PluginData) o;
        return size == that.size &&
                Objects.equal(sourcePlugin, that.sourcePlugin) &&
                Objects.equal(pluginIcon, that.pluginIcon);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(size, sourcePlugin, pluginIcon);
    }
}
