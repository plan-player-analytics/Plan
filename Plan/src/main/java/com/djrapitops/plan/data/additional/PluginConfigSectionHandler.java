package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.Plan;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        ConfigurationSection section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();
        if (!section.contains(pluginName)) {
            return false;
        }
        ConfigurationSection pluginSection = section.getConfigurationSection(pluginName);
        return pluginSection.contains(dataSource.getPlaceholder(""));
    }

    private ConfigurationSection getPluginsSection() {
        FileConfiguration config = plan.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Customization.Plugins");
        return section;
    }

    public void createSection(PluginData dataSource) {
        ConfigurationSection section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();
        String source = dataSource.placeholder;
        section.addDefault(pluginName + ".Enabled", true);
        section.addDefault(pluginName + ".Data." + source, true);
        FileConfiguration config = plan.getConfig();
        config.set("Customization.Plugins", section);
        plan.saveConfig();
    }

    public boolean isEnabled(PluginData dataSource) {
        ConfigurationSection section = getPluginsSection();
        String pluginName = dataSource.getSourcePlugin();
        if (!section.getBoolean(pluginName + ".Enabled")) {
            return false;
        }
        String source = dataSource.placeholder;
        return section.getBoolean(pluginName + ".Data." + source);
    }
}
