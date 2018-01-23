package com.djrapitops.plan.data.plugin;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.*;

/**
 * Class responsible for hooking to other plugins and managing the %plugins%
 * placeholder on Analysis and Inspect pages.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
public class HookHandler {

    private final List<PluginData> additionalDataSources;
    private final PluginsConfigSection configHandler;

    /**
     * Class constructor, hooks to plugins.
     *
     * @param plugin Current instance of plan.
     */
    public HookHandler(Plan plugin) {
        additionalDataSources = new ArrayList<>();
        configHandler = new PluginsConfigSection();
        try {
//            Bridge.hook(this);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Plan Plugin Bridge not included in the plugin jar.");
        }
    }

    public static HookHandler getInstance() {
        return null;// TODO
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
        if (dataSource == null) {
            return;
        }
        try {
            StaticHolder.saveInstance(dataSource.getClass(), Plan.class);
            if (!configHandler.hasSection(dataSource)) {
                configHandler.createSection(dataSource);
            }
            if (configHandler.isEnabled(dataSource)) {
                Log.debug("Registered a new datasource: " + dataSource.getSourcePlugin());
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

    public Map<PluginData, InspectContainer> getInspectContainersFor(UUID uuid) {
        List<PluginData> plugins = getAdditionalDataSources();
        Map<PluginData, InspectContainer> containers = new HashMap<>();
        for (PluginData pluginData : plugins) {
            InspectContainer inspectContainer = new InspectContainer();
            try {
                InspectContainer container = pluginData.getPlayerData(uuid, inspectContainer);
                if (container != null && !container.isEmpty()) {
                    containers.put(pluginData, container);
                }
            } catch (Exception e) {
                String sourcePlugin = pluginData.getSourcePlugin();
                Log.error("PluginData caused exception: " + sourcePlugin);
                Log.toLog(this.getClass().getName() + " " + sourcePlugin, e);
            }
        }
        return containers;
    }
}
