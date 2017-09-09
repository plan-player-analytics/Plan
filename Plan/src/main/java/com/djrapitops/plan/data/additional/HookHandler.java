package main.java.com.djrapitops.plan.data.additional;

import com.djrapitops.pluginbridge.plan.Bridge;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class responsible for hooking to other plugins and managing the %plugins%
 * placeholder on Analysis and Inspect pages.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
public class HookHandler {

    private final List<PluginData> additionalDataSources;
    private final PluginConfigSectionHandler configHandler;

    /**
     * Class constructor, hooks to plugins.
     *
     * @param plugin Current instance of plan.
     */
    public HookHandler(Plan plugin) {
        additionalDataSources = new ArrayList<>();
        configHandler = new PluginConfigSectionHandler(plugin);
        try {
            Bridge.hook(this);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Plan Plugin Bridge not included in the plugin jar.");
        }
    }

    /**
     * Adds a new PluginData source to the list.
     * <p>
     * The plugin data will appear on Analysis and/or Inspect pages depending on
     * how the extending object is set up.
     * <p>
     * Refer to documentation on GitHub for more information.
     *
     * @param dataSource an object extending the PluginData class.
     */
    public void addPluginDataSource(PluginData dataSource) {
        try {
            if (!configHandler.hasSection(dataSource)) {
                configHandler.createSection(dataSource);
            }
            if (configHandler.isEnabled(dataSource)) {
                Log.debug("Registered a new datasource: " + StringUtils.remove(dataSource.getPlaceholder(), '%'));
                additionalDataSources.add(dataSource);
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Attempting to register PluginDataSource caused an exception.");
        }
    }

    /**
     * Used to get all PluginData objects currently registered.
     *
     * @return List of PluginData objects.
     */
    public List<PluginData> getAdditionalDataSources() {
        return additionalDataSources;
    }

    private List<String> getPluginNamesAnalysis() {
        List<String> pluginNames = additionalDataSources.stream()
                .filter(source -> !source.getAnalysisTypes().isEmpty())
                .map(PluginData::getSourcePlugin)
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(pluginNames);
        return pluginNames;
    }

    private List<String> getPluginNamesInspect() {
        List<String> pluginNames = additionalDataSources.stream()
                .filter(source -> !source.analysisOnly())
                .map(PluginData::getSourcePlugin)
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(pluginNames);
        return pluginNames;
    }

    /**
     * Used to get the replaceMap for inspect page.
     *
     * @param uuid UUID of the player whose page is being inspected.
     * @return Map: key|value - %placeholder%|value
     */
    public Map<String, Serializable> getAdditionalInspectReplaceRules(UUID uuid) {
        Map<String, Serializable> addReplace = new HashMap<>();
        for (PluginData source : additionalDataSources) {
            if (source.analysisOnly()) {
                continue;
            }
            try {
                addReplace.put(source.getPlaceholderName(), source.getHtmlReplaceValue("", uuid));
            } catch (Exception e) {
                addReplace.put(source.getPlaceholderName(), "Error occurred: " + e);
                Log.error("PluginDataSource caused an exception: " + source.getSourcePlugin());
                Log.toLog("PluginDataSource " + source.getSourcePlugin(), e);
            }
        }
        return addReplace;
    }
}
