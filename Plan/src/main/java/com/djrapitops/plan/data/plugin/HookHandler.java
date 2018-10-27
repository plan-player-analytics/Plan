package com.djrapitops.plan.data.plugin;

import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.Bridge;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class responsible for hooking to other plugins and managing the %plugins%
 * placeholder on Analysis and Inspect pages.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
@Singleton
public class HookHandler implements SubSystem {

    private final List<PluginData> additionalDataSources;

    private final Bridge bridge;
    private PluginsConfigSection configHandler;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public HookHandler(
            Bridge bridge,
            PluginsConfigSection configHandler,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.bridge = bridge;
        this.configHandler = configHandler;
        this.logger = logger;
        this.errorHandler = errorHandler;

        additionalDataSources = new ArrayList<>();
    }

    @Override
    public void enable() {
        try {
            bridge.hook(this);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            logger.error("Plan Plugin Bridge not included in the plugin jar.");
        }
    }

    @Override
    public void disable() {
        // Nothing to disable
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
            if (!configHandler.hasSection(dataSource)) {
                configHandler.createSection(dataSource);
            }
            if (configHandler.isEnabled(dataSource)) {
                logger.debug("Registered a new datasource: " + dataSource.getSourcePlugin());
                additionalDataSources.add(dataSource);
            }
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
            logger.error("Attempting to register PluginDataSource caused an exception.");
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

    public List<BanData> getBanDataSources() {
        return additionalDataSources.stream()
                .filter(p -> p instanceof BanData)
                .map(p -> (BanData) p)
                .collect(Collectors.toList());
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
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                String sourcePlugin = pluginData.getSourcePlugin();
                logger.error("PluginData caused exception: " + sourcePlugin);
                errorHandler.log(L.WARN, pluginData.getClass(), e);
            }
        }
        return containers;
    }
}
