package net.playeranalytics.plan;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.playeranalytics.plugin.FabricPlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.File;
import java.io.InputStream;

public class PlanFabric implements PlanPlugin, DedicatedServerModInitializer {

    private PluginLogger pluginLogger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @Override
    public InputStream getResource(String resource) {
        return null;
    }

    @Override
    public ColorScheme getColorScheme() {
        return null;
    }

    @Override
    public PlanSystem getSystem() {
        return null;
    }

    @Override
    public void registerCommand(Subcommand command) {

    }

    @Override
    public void onEnable() {
        PlanFabricComponent component = DaggerPlanFabricComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .build();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public void onInitializeServer() {
        abstractionLayer = new FabricPlatformLayer(this);
        pluginLogger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();
        onEnable();
    }
}
