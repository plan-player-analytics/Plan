package main.java.com.djrapitops.plan.data.plugin;

import com.djrapitops.plugin.api.config.ConfigNode;
import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;

import java.io.IOException;

/**
 * Class responsible for generating and generating settings for PluginData
 * objects to the config.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class PluginConfigSectionHandler {

    private final Plan plan;

    public PluginConfigSectionHandler(Plan plan) {
        this.plan = plan;
    }

    public boolean hasSection(PluginData dataSource) {
        ConfigNode section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();
        if (!section.getChildren().containsKey(pluginName)) {
            return false;
        }
        return section.getConfigNode(pluginName).getChildren().containsKey("Enabled");
    }

    private ConfigNode getPluginsSection() {
        return plan.getMainConfig().getConfigNode("Plugins");
    }

    public void createSection(PluginData dataSource) {
        ConfigNode section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();

        section.set(pluginName + ".Enabled", true);
        try {
            section.sort();
            section.save();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public boolean isEnabled(PluginData dataSource) {
        ConfigNode section = getPluginsSection();

        String pluginName = dataSource.getSourcePlugin();
        return section.getBoolean(pluginName + ".Enabled");
    }
}
